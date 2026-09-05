package io.github.lijiajia3515.cairo.auth.framework.security.oauth2;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuth2Error;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.web.CairoOAuth2AuthenticationFailedHandler;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.accept.ContentNegotiationManager;

import java.io.IOException;
import java.util.List;

@Slf4j
public class CairoOAuthTokenFailureHandler extends CairoOAuth2AuthenticationFailedHandler implements AuthenticationFailureHandler {

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
		// 防御：本处理器只承接 OAuth2AuthenticationException（OAuth2TokenEndpointFilter 仅捕获该类型）；
		// 裸 AuthenticationException 一律走父类富映射（BadCredentials→Auth.PasswordBad 等），避免强转 ClassCastException
		if (!(exception instanceof OAuth2AuthenticationException oauthException)) {
			super.onAuthenticationFailure(request, response, exception);
			return;
		}

		OAuth2Error error = oauthException.getError();

		HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
		if (OAuth2ErrorCodes.INSUFFICIENT_SCOPE.equals(error.getErrorCode())) {
			httpStatus = HttpStatus.FORBIDDEN;
		} else if (OAuth2ErrorCodes.SERVER_ERROR.equals(error.getErrorCode())) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		} else if (OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE.equals(error.getErrorCode())) {
			httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
		}

		// 协议层失败按 RFC 6749 错误码细分为业务码，调用方只判 code 即可区分失败原因
		CairoOAuth2Error result = CairoOAuth2Error.builder()
			.business(resolveBusiness(error))
			.data(error)
			.build();

		response.setStatus(httpStatus.value());

		writeWithMessageConverters(result, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
	}

	/**
	 * RFC 6749 错误码 → 业务码映射：不再统一坍缩为 Auth.OAuthError"认证错误"。
	 * 未识别错误码回落 OAUTH_ERROR 兜底。
	 */
	static Business resolveBusiness(OAuth2Error error) {
		return switch (error.getErrorCode()) {
			case OAuth2ErrorCodes.INVALID_CLIENT -> CairoOAuthBusiness.CLIENT_INVALID;
			case OAuth2ErrorCodes.INVALID_REQUEST -> CairoOAuthBusiness.PARAMS_BAD;
			case OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE -> CairoOAuthBusiness.GRANT_NOT_SUPPORTED;
			case OAuth2ErrorCodes.INVALID_GRANT -> CairoOAuthBusiness.GRANT_INVALID;
			case OAuth2ErrorCodes.INVALID_SCOPE, OAuth2ErrorCodes.INSUFFICIENT_SCOPE -> CairoOAuthBusiness.SCOPE_INSUFFICIENT;
			default -> CairoOAuthBusiness.OAUTH_ERROR;
		};
	}
}
