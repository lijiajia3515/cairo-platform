package io.github.lijiajia3515.cairo.auth.framework.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoAuthExceptionMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoOAuth2AuthenticationFailedHandler;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;

/**
 * token 端点失败处理器：输出<b>纯 RFC 6749</b> 错误响应（error / error_description / error_uri + 语义化 HTTP 状态），
 * 不做任何业务信封包装——/oauth2/token 是协议端点，标准 OAuth2 客户端依赖其响应契约。
 * 业务语义（BusinessResult 信封）由 /open_api 代理层（OAuth2OpenApiController）凭 {@link CairoOAuthErrorMapper} 转换补齐。
 * 扩展码：裸 AuthenticationException（Cairo 认证链业务失败，如密码错误）以业务码（Auth.* 前缀）作为 RFC 扩展 error 输出，
 * open 代理按前缀原样还原业务码，协议客户端按未知 error 码泛化处理，互不干扰。
 */
@Slf4j
public class CairoOAuthTokenFailureHandler extends CairoOAuth2AuthenticationFailedHandler implements AuthenticationFailureHandler {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public CairoOAuthTokenFailureHandler(List<HttpMessageConverter<?>> converters) {
		super(converters);
	}

	public CairoOAuthTokenFailureHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
		super(converters, contentNegotiationManager);
	}

	@Override
	@NewSpan
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		log.info("CairoOAuthTokenFailureHandler onAuthenticationFailure", exception);

		String errorCode;
		String description;
		String errorUri = null;
		HttpStatus httpStatus;
		if (exception instanceof OAuth2AuthenticationException oauthException) {
			OAuth2Error error = oauthException.getError();
			errorCode = error.getErrorCode();
			description = error.getDescription();
			errorUri = error.getUri();
			httpStatus = CairoOAuthErrorMapper.resolveStatus(errorCode);
		} else {
			// 裸 AuthenticationException（Cairo 认证链业务失败）：业务码作为 RFC 扩展 error 输出（RFC 6749 允许扩展 error 值），
			// /open_api 代理按 Auth.* 前缀原样还原业务码，标准客户端按未知 error 码泛化处理
			Business business = CairoAuthExceptionMapper.businessOf(exception);
			errorCode = business.code();
			description = Optional.ofNullable(exception.getMessage()).filter(s -> !s.isBlank()).orElse(business.getMessage());
			httpStatus = CairoAuthExceptionMapper.statusOf(exception);
		}

		response.setStatus(httpStatus.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		Map<String, String> body = new LinkedHashMap<>();
		body.put("error", errorCode);
		if (description != null && !description.isBlank()) {
			body.put("error_description", description);
		}
		if (errorUri != null && !errorUri.isBlank()) {
			body.put("error_uri", errorUri);
		}
		OBJECT_MAPPER.writeValue(response.getWriter(), body);
	}
}
