package io.github.lijiajia3515.cairo.auth.framework.security.web;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthBusiness;
import io.github.lijiajia3515.cairo.core.business.Business;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.accept.ContentNegotiationManager;

import java.io.IOException;
import java.util.List;

/**
 * OAuth2 认证异常类
 */
@Slf4j
public class CairoOAuth2AuthenticationFailedHandler extends CairoAuthenticationFailedHandler implements AuthenticationFailureHandler {

	public CairoOAuth2AuthenticationFailedHandler(List<HttpMessageConverter<?>> converters) {
		super(converters, null);
	}

	public CairoOAuth2AuthenticationFailedHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
		super(converters, contentNegotiationManager);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

		if (exception instanceof OAuth2AuthenticationException) {
			log.debug("authentication failed ", exception);
			HttpStatus httpStatus;
			Business status = CairoOAuthBusiness.OAUTH_ERROR;

			OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();

			if (OAuth2ErrorCodes.INSUFFICIENT_SCOPE.equals(error.getErrorCode())) {
				httpStatus = HttpStatus.FORBIDDEN;
			} else if (OAuth2ErrorCodes.SERVER_ERROR.equals(error.getErrorCode())) {
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			} else if (OAuth2ErrorCodes.TEMPORARILY_UNAVAILABLE.equals(error.getErrorCode())) {
				httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				httpStatus = HttpStatus.UNAUTHORIZED;
			}

			write(httpStatus, status, exception, request, response);
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}

	}
}
