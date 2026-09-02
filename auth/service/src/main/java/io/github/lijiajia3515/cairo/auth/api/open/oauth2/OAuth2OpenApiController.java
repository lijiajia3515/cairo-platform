package io.github.lijiajia3515.cairo.auth.api.open.oauth2;

import io.github.lijiajia3515.cairo.auth.modules.oauth2.OAuth2TokenOpenApiResponseErrorHandler;
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
			return ResponseEntity
				.status(exchange.getStatusCode())
				.body(exchange.getBody());
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
