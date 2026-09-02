package io.github.lijiajia3515.cairo.auth.framework.security.web;

import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CairoBearerTokenAccessDeniedHandler extends AbstractHttpMessageHandler implements AccessDeniedHandler {


	@Getter
	@Setter
	@Accessors(fluent = true)
	private String realmName;

	public CairoBearerTokenAccessDeniedHandler(List<HttpMessageConverter<?>> converters) {
		super(converters, null);
	}

	public CairoBearerTokenAccessDeniedHandler(List<HttpMessageConverter<?>> converters, ContentNegotiationManager contentNegotiationManager) {
		super(converters, contentNegotiationManager);
	}

	/**
	 * Collect error details from the provided parameters and format according to RFC
	 * 6750, specifically {@code error}, {@code error_description}, {@code error_uri}, and
	 * {@code scope}.
	 *
	 * @param request               that resulted in an <code>AccessDeniedException</code>
	 * @param response              so that the user agent can be advised of the failure
	 * @param accessDeniedException that caused the invocation
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
					   AccessDeniedException accessDeniedException) throws HttpMediaTypeNotAcceptableException, IOException {
		log.debug("authentication failed ", accessDeniedException);
		Map<String, String> parameters = new LinkedHashMap<>();
		if (this.realmName != null) {
			parameters.put("realm", this.realmName);
		}
		if (request.getUserPrincipal() instanceof AbstractOAuth2TokenAuthenticationToken) {
			parameters.put("error", BearerTokenErrorCodes.INSUFFICIENT_SCOPE);
			parameters.put("error_description",
				"The request requires higher privileges than provided by the access token.");
			parameters.put("error_uri", "https://tools.ietf.org/html/rfc6750#section-3.1");
		}
		String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);
		response.addHeader(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
		response.setStatus(HttpStatus.FORBIDDEN.value());

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		BusinessResult<?> returnValue = BusinessResult.builder()
			.business(CairoAuthBusiness.DENIED)
			.message(accessDeniedException.getMessage())
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
