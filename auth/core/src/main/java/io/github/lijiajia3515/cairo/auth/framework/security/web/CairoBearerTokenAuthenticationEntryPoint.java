package io.github.lijiajia3515.cairo.auth.framework.security.web;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppUserNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenExpiredException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.VerifyCodeBadCredentialsException;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthBusiness;
import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.http.converter.AbstractHttpMessageHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
public class CairoBearerTokenAuthenticationEntryPoint extends AbstractHttpMessageHandler implements AuthenticationEntryPoint {


	@Setter
	@Accessors(fluent = true)
	private String realmName = "cairo";

	public CairoBearerTokenAuthenticationEntryPoint(List<HttpMessageConverter<?>> converters) {

		super(converters, null);
	}

	public CairoBearerTokenAuthenticationEntryPoint(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager) {
		super(converters, manager);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws HttpMediaTypeNotAcceptableException, IOException {
		log.debug("authentication entry point : {}, {}", request.getRequestURI(), exception.getMessage());
		log.debug("authenticationEntryPoint", exception);

		Map<String, String> parameters = new LinkedHashMap<>();
		if (this.realmName != null) {
			parameters.put("realm", this.realmName);
		}

		Business business = CairoAuthBusiness.ERROR;
		// http status
		HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
		if (exception instanceof OAuth2AuthenticationException) {
			OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
			parameters.put("error", error.getErrorCode());
			if (StringUtils.hasText(error.getDescription())) {
				parameters.put("error_description", error.getDescription());
			}
			if (StringUtils.hasText(error.getUri())) {
				parameters.put("error_uri", error.getUri());
			}
			if (error instanceof BearerTokenError) {
				BearerTokenError bearerTokenError = (BearerTokenError) error;
				if (StringUtils.hasText(bearerTokenError.getScope())) {
					parameters.put("scope", bearerTokenError.getScope());
				}
				httpStatus = ((BearerTokenError) error).getHttpStatus();
			}
		}

		// business status
		if (exception instanceof InsufficientAuthenticationException) {
			httpStatus = HttpStatus.FORBIDDEN;
			business = CairoAuthBusiness.DENIED;
		} else if (exception instanceof OAuth2AuthenticationException) {
			business = CairoOAuthBusiness.OAUTH_ERROR;
			httpStatus = HttpStatus.UNAUTHORIZED;
			if (exception instanceof InvalidBearerTokenException && exception.getMessage().contains("expired")) {
				business = CairoAuthBusiness.TOKEN_EXPIRED;
			}

			OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
			if (OAuth2ErrorCodes.INSUFFICIENT_SCOPE.equals(error.getErrorCode())) {
				httpStatus = HttpStatus.FORBIDDEN;
			}
		} else if (exception instanceof TokenInvalidException) {
			business = CairoAuthBusiness.TOKEN_INVALID;
		} else if (exception instanceof TokenExpiredException) {
			business = CairoAuthBusiness.TOKEN_EXPIRED;
		} else if (exception instanceof UsernameNotFoundException) {
			business = CairoAuthBusiness.ACCOUNT_NOT_FOUND;
		} else if (exception instanceof AccountNotFoundException) {
			business = CairoAuthBusiness.ACCOUNT_NOT_FOUND;
		} else if (exception instanceof AccountExpiredException) {
			business = CairoAuthBusiness.ACCOUNT_LOCKED;
		} else if (exception instanceof BadCredentialsException) {
			business = CairoAuthBusiness.PASSWORD_BAD;
		} else if (exception instanceof VerifyCodeBadCredentialsException) {
			business = CairoAuthBusiness.VERIFY_CODE_BAD;
		} else if (exception instanceof SnsCodeFailedException) {
			business = CairoAuthBusiness.SNS_CODE_BAD;
		} else if (exception instanceof DisabledException) {
			business = CairoAuthBusiness.ACCOUNT_DISABLED;
		} else if (exception instanceof LockedException) {
			business = CairoAuthBusiness.ACCOUNT_LOCKED;
		} else if (exception instanceof ClientNotFoundException) {
			business = CairoAuthBusiness.CLIENT_NOT_FOUND;
		} else if (exception instanceof ClientDisabledException) {
			business = CairoAuthBusiness.CLIENT_DISABLED;
		} else if (exception instanceof AppNotFoundException) {
			business = CairoAuthBusiness.APP_NOT_FOUND;
		} else if (exception instanceof AppDisabledException) {
			business = CairoAuthBusiness.APP_DISABLED;
		} else if (exception instanceof EndpointNotFoundException) {
			business = CairoAuthBusiness.ENDPOINT_NOT_FOUND;
		} else if (exception instanceof EndpointDisabledException) {
			business = CairoAuthBusiness.ENDPOINT_DISABLED;
		} else if (exception instanceof SubappNotFoundException) {
			business = CairoAuthBusiness.SUBAPP_NOT_FOUND;
		} else if (exception instanceof SubappNotApplyException) {
			business = CairoAuthBusiness.SUBAPP_NOT_APPLY;
		}else if (exception instanceof SubappDisabledException) {
			business = CairoAuthBusiness.SUBAPP_DISABLED;
		} else if (exception instanceof AppUserNotFoundException) {
			business = CairoAuthBusiness.APP_USER_NOT_FOUND;
		} else if (exception instanceof AppUserDisabledException) {
			business = CairoAuthBusiness.APP_USER_DISABLED;
		} else if (exception instanceof TenantNotFoundException) {
			business = CairoAuthBusiness.TENANT_NOT_FOUND;
		} else if (exception instanceof TenantDisabledException) {
			business = CairoAuthBusiness.TENANT_DISABLED;
		} else if (exception instanceof TenantAppNotApplyException) {
			business = CairoAuthBusiness.TENANT_APP_NOT_APPLY;
		} else if (exception instanceof TenantAppDisabledException) {
			business = CairoAuthBusiness.TENANT_APP_DISABLED;
		} else if (exception instanceof TenantEndpointNotApplyException) {
			business = CairoAuthBusiness.TENANT_ENDPOINT_NOT_APPLY;
		} else if (exception instanceof TenantEndpointDisabledException) {
			business = CairoAuthBusiness.TENANT_ENDPOINT_DISABLED;
		} else if (exception instanceof TenantSubappNotApplyException) {
			business = CairoAuthBusiness.TENANT_SUBAPP_NOT_APPLY;
		} else if (exception instanceof TenantSubappDisabledException) {
			business = CairoAuthBusiness.TENANT_SUBAPP_DISABLED;
		} else if (exception instanceof TenantAppUserNotFoundException) {
			business = CairoAuthBusiness.TENANT_APP_USER_NOT_FOUND;
		} else if (exception instanceof TenantAppUserDisabledException) {
			business = CairoAuthBusiness.TENANT_APP_USER_DISABLED;
		} else if (exception instanceof ProviderNotFoundException) {
			business = CairoAuthBusiness.NOT_SUPPORTED;
		} else if (exception instanceof AuthenticationServiceException) {
			business = CairoAuthBusiness.ERROR;
		}

		String message = business.getMessage();
		if (!(exception instanceof AuthenticationServiceException)) {
			message = exception.getMessage();
		}

		String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
		response.addHeader(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
		response.setStatus(httpStatus.value());

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		BusinessResult<?> returnValue = BusinessResult.builder()
			.code(business.getCode())
			.message(message)
			.build();

		writeWithMessageConverters(returnValue, new ServletServerHttpRequest(request), new ServletServerHttpResponse(response));
	}

	private static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
		StringBuilder wwwAuthenticate = new StringBuilder();
		wwwAuthenticate.append("Bearer");
		if (!parameters.isEmpty()) {
			wwwAuthenticate.append(" ");
			int i = 0;
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				wwwAuthenticate.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
				if (i != parameters.size() - 1) {
					wwwAuthenticate.append(", ");
				}
				i++;
			}
		}
		return wwwAuthenticate.toString();
	}
}
