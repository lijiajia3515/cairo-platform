/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class AppUserAuthenticationTokenResolver {
	public static final String APP_ID = "appId";
	public static final String ENDPOINT_ID = "endpointId";
	public static final String TOKEN = "token";


	private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^app_user (?<appId>[a-zA-Z0-9-._~+/]+=*)/(?<endpointId>[a-zA-Z0-9-._~+/]+=*)/(?<token>[a-zA-Z0-9-._~+/]+=*)$",
			Pattern.CASE_INSENSITIVE);


	private static final String ACCESS_TOKEN_PARAMETER_NAME = "app_user_access_token";

	private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("^(?<appId>[a-zA-Z0-9-._~+/]+=*)/(?<endpointId>[a-zA-Z0-9-._~+/]+=*)/(?<token>[a-zA-Z0-9-._~+/]+=*)$",
		Pattern.CASE_INSENSITIVE);

	private boolean allowFormEncodedBodyParameter = false;

	private boolean allowUriQueryParameter = false;

	private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

	public AppUserAuthenticationTokenRequest resolve(final HttpServletRequest request) {
		final AppUserAuthenticationTokenRequest authorizationHeaderToken = resolveFromAuthorizationHeader(request);
		final AppUserAuthenticationTokenRequest parameterToken = isParameterTokenSupportedForRequest(request)
				? resolveFromRequestParameters(request) : null;
		if (authorizationHeaderToken != null) {
			if (parameterToken != null) {
				throw new TokenInvalidException("Found multiple app user tokens in the request");
			}
			return authorizationHeaderToken;
		}
		if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
			return parameterToken;
		}
		return null;
	}

	/**
	 * Set if transport of access token using form-encoded body parameter is supported.
	 * Defaults to {@code false}.
	 * @param allowFormEncodedBodyParameter if the form-encoded body parameter is
	 * supported
	 */
	public void setAllowFormEncodedBodyParameter(boolean allowFormEncodedBodyParameter) {
		this.allowFormEncodedBodyParameter = allowFormEncodedBodyParameter;
	}

	/**
	 * Set if transport of access token using URI query parameter is supported. Defaults
	 * to {@code false}.
	 *
	 * The spec recommends against using this mechanism for sending bearer tokens, and
	 * even goes as far as stating that it was only included for completeness.
	 * @param allowUriQueryParameter if the URI query parameter is supported
	 */
	public void setAllowUriQueryParameter(boolean allowUriQueryParameter) {
		this.allowUriQueryParameter = allowUriQueryParameter;
	}

	/**
	 * Set this value to configure what header is checked when resolving a Bearer Token.
	 * This value is defaulted to {@link org.springframework.http.HttpHeaders#AUTHORIZATION}.
	 *
	 * This allows other headers to be used as the Bearer Token source such as
	 * {@link org.springframework.http.HttpHeaders#PROXY_AUTHORIZATION}
	 * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
	 * @since 5.4
	 */
	public void setBearerTokenHeaderName(String bearerTokenHeaderName) {
		this.bearerTokenHeaderName = bearerTokenHeaderName;
	}

	private AppUserAuthenticationTokenRequest resolveFromAuthorizationHeader(HttpServletRequest request) {
		String authorization = request.getHeader(this.bearerTokenHeaderName);
		if (!StringUtils.startsWithIgnoreCase(authorization, "app_user")) {
			return null;
		}
		Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
		if (!matcher.matches()) {
			throw new TokenInvalidException("app_user 认证参数格式错误");
		}
		return new AppUserAuthenticationTokenRequest(
			matcher.group(APP_ID),
			matcher.group(ENDPOINT_ID),
			matcher.group(TOKEN)
		);
	}

	private static AppUserAuthenticationTokenRequest resolveFromRequestParameters(HttpServletRequest request) {
		String[] values = request.getParameterValues(ACCESS_TOKEN_PARAMETER_NAME);
		if (values == null || values.length == 0) {
			return null;
		}
		if (values.length == 1) {
			String accessToken = values[0];
			Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(accessToken);
			if (!matcher.matches()) {
				throw new TokenInvalidException("app_user 认证参数格式错误");
			}
			return new AppUserAuthenticationTokenRequest(
				matcher.group(APP_ID),
				matcher.group(ENDPOINT_ID),
				matcher.group(TOKEN)
			);
		}

		throw new TokenInvalidException("Found multiple account tokens in the request");
	}

	private boolean isParameterTokenSupportedForRequest(final HttpServletRequest request) {
		return isFormEncodedRequest(request) || isGetRequest(request);
	}

	private static boolean isGetRequest(HttpServletRequest request) {
		return HttpMethod.GET.name().equals(request.getMethod());
	}

	private static boolean isFormEncodedRequest(HttpServletRequest request) {
		return MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType());
	}

	private static boolean hasAccessTokenInQueryString(HttpServletRequest request) {
		return (request.getQueryString() != null) && request.getQueryString().contains(ACCESS_TOKEN_PARAMETER_NAME);
	}

	private boolean isParameterTokenEnabledForRequest(HttpServletRequest request) {
		return ((this.allowFormEncodedBodyParameter && isFormEncodedRequest(request) && !isGetRequest(request)
				&& !hasAccessTokenInQueryString(request)) || (this.allowUriQueryParameter && isGetRequest(request)));
	}

}
