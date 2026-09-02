/*
 * Copyright 2020-2021 the original author or authors.
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
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuth2EndpointUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_VERIFY_CODE;


/**
 * 账号验证码模式 authentication converter
 */
public final class OAuthAccountVerifyCodeAuthenticationConverter implements AuthenticationConverter {

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		// grant_type (REQUIRED)
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (!ACCOUNT_VERIFY_CODE.getValue().equals(grantType)) {
			return null;
		}

		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

		// phoneNumber (REQUIRED)
		String phoneNumber = parameters.getFirst(CairoOAuthParameterNames.PHONE_NUMBER);
		if (!StringUtils.hasText(phoneNumber) ||
			parameters.get(CairoOAuthParameterNames.PHONE_NUMBER).size() != 1) {
			OAuth2EndpointUtils.throwError(
				OAuth2ErrorCodes.INVALID_REQUEST,
				CairoOAuthParameterNames.PHONE_NUMBER,
				OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		// verifyCode (REQUIRED)
		String verifyCode = parameters.getFirst(CairoOAuthParameterNames.VERIFY_CODE);
		if (!StringUtils.hasText(verifyCode) ||
			parameters.get(CairoOAuthParameterNames.VERIFY_CODE).size() != 1) {
			OAuth2EndpointUtils.throwError(
				OAuth2ErrorCodes.INVALID_REQUEST,
				CairoOAuthParameterNames.VERIFY_CODE,
				OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		// scope (OPTIONAL)
		Set<String> scopes = null;
		String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
		if (StringUtils.hasText(scope) &&
			parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
			OAuth2EndpointUtils.throwError(
				OAuth2ErrorCodes.INVALID_REQUEST,
				OAuth2ParameterNames.SCOPE,
				OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}
		if (StringUtils.hasText(scope)) {
			scopes = new HashSet<>(
				Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
		}

		Map<String, Object> additionalParameters = new HashMap<>();
		parameters.forEach((key, value) -> {
			if (!key.equals(OAuth2ParameterNames.CLIENT_ID)
				&& !key.equals(OAuth2ParameterNames.CLIENT_SECRET)
				&& !key.equals(CairoOAuthParameterNames.PHONE_NUMBER)
				&& !key.equals(CairoOAuthParameterNames.VERIFY_CODE)
				&& !key.equals(OAuth2ParameterNames.SCOPE)
			) {
				additionalParameters.put(key, value.get(0));
			}
		});

		return new OAuthAccountVerifyCodeAuthenticationToken(phoneNumber, verifyCode, scopes, clientPrincipal, additionalParameters);
	}

}
