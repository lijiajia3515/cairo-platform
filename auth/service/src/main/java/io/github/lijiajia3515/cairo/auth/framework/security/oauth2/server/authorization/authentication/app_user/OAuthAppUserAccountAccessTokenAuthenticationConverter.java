package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user;

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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAuthorizationGrantTypes.ACCOUNT_ACCESS_TOKEN;


/**
 * 应用用户账号授权模式 authentication converter
 */
public final class OAuthAppUserAccountAccessTokenAuthenticationConverter implements AuthenticationConverter {

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		// grant_type (REQUIRED)
		String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
		if (!ACCOUNT_ACCESS_TOKEN.getValue().equals(grantType)) {
			return null;
		}

		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);

		// account_access_token (REQUIRED)
		String accountAccessToken = parameters.getFirst(CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN);
		if (!StringUtils.hasText(accountAccessToken) ||
			parameters.get(CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN).size() != 1) {
			OAuth2EndpointUtils.throwError(
				OAuth2ErrorCodes.INVALID_REQUEST,
				CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN,
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
				&& !key.equals(OAuth2ParameterNames.SCOPE)
				&& !key.equals(CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN)
			) {
				additionalParameters.put(key, value.get(0));
			}
		});

		return new OAuthAppUserAccountAccessTokenAuthenticationToken(accountAccessToken, scopes, clientPrincipal, additionalParameters);
	}

}
