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
		HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
		Business business = CairoOAuthBusiness.OAUTH_ERROR;

		OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();

		if (OAuth2ErrorCodes.INSUFFICIENT_SCOPE.equals(error.getErrorCode())) {
			httpStatus = HttpStatus.FORBIDDEN;
		}

		CairoOAuth2Error result = CairoOAuth2Error.builder()
			.business(business)
			.data(error)
			.build();

		response.setStatus(httpStatus.value());

		writeWithMessageConverters(result, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
	}
}
