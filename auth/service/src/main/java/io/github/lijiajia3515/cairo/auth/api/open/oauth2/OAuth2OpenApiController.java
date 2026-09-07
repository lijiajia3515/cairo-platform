package io.github.lijiajia3515.cairo.auth.api.open.oauth2;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.CairoOAuthErrorMapper;
import io.github.lijiajia3515.cairo.auth.modules.oauth2.OAuth2TokenOpenApiResponseErrorHandler;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [open/api] oauth2 controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/open_api/oauth2")
public class OAuth2OpenApiController {
	private final RestTemplate restTemplate;
	private final ServerProperties serverProperties;

	public OAuth2OpenApiController(ServerProperties serverProperties) {
		this.serverProperties = serverProperties;
		restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		restTemplate.setErrorHandler(new OAuth2TokenOpenApiResponseErrorHandler());
	}


	@RequestMapping("/{path}")
	public ResponseEntity<?> proxy(@PathVariable String path, HttpServletRequest request) {
		String url = String.format("http://localhost:%s/oauth2/%s", serverProperties.getPort(), path);
		HttpMethod httpMethod = Objects.requireNonNull(HttpMethod.valueOf(request.getMethod()));
		HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(getRequestParameterMap(request), getRequestHeaderMap(request));
		ResponseEntity<Map> exchange = restTemplate.exchange(url, httpMethod, multiValueMapHttpEntity, Map.class);
		if (exchange.getStatusCode().is2xxSuccessful()) {
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(BusinessResult
					.builder()
					.business(DefaultBusiness.SUCCESS)
					.data(exchange.getBody())
					.build()
				);
		} else {
			Map<?, ?> raw = exchange.getBody();
			// 双形态识别：Cairo 认证链 BusinessException（密码错等）不经 token 失败处理器，
			// 在标准端点直接走全局异常处理器，返回的已是业务信封（含 code 键）——原样透传保真
			if (raw.containsKey("code")) {
				return ResponseEntity
					.status(exchange.getStatusCode())
					.body(raw);
			}
			// 标准端点失败响应为纯 RFC 6749（或 Auth.* 扩展码），业务信封在本代理层补齐——协议纯净与业务语义各归其位
			String errorCode = String.valueOf(Optional.ofNullable(raw.get("error")).orElse("invalid_request"));
			Business business = CairoOAuthErrorMapper.resolve(errorCode);
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("errorCode", errorCode);
			Object description = raw.get("error_description");
			if (description != null) {
				data.put("description", description);
			}
			Object errorUri = raw.get("error_uri");
			if (errorUri != null) {
				data.put("uri", errorUri);
			}
			return ResponseEntity
				.status(exchange.getStatusCode())
				.body(BusinessResult
					.builder()
					.business(business)
					.data(data)
					.build()
				);
		}
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<Object> exceptionHandler(HttpClientErrorException ex) {
		return ResponseEntity.status(ex.getStatusCode())
			.contentType(Objects.requireNonNull(ex.getResponseHeaders().getContentType()))
			.body(ex.getResponseBodyAsString());
	}

	protected MultiValueMap<String, String> getRequestHeaderMap(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>(8);
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			List<String> values = new ArrayList<>(1);
			Enumeration<String> headerValues = request.getHeaders(name);
			while (headerValues.hasMoreElements()) {
				String value = headerValues.nextElement();
				values.add(value);
			}

			headerMap.put(name, values);
		}
		return headerMap;
	}

	protected MultiValueMap<String, String> getRequestParameterMap(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		parameterMap.forEach((k, v) -> {
			map.put(k, Arrays.stream(v).collect(Collectors.toList()));
		});
		return map;
	}

}
