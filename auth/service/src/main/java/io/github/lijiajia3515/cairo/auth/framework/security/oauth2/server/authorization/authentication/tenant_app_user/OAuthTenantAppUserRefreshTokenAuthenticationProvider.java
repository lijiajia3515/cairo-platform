/*
 * Copyright 2020-2023 the original author or authors.
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
package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.TENANT_APP_USER_REFRESH_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuthAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * 终端用户刷新令牌模式 authentication provider
 */
public final class OAuthTenantAppUserRefreshTokenAuthenticationProvider implements AuthenticationProvider {
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
	private final Log logger = LogFactory.getLog(getClass());
	private final TenantAppUserAuthorizationService authorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	/**
	 * Constructs an {@code OAuth2RefreshTokenAuthenticationProvider} using the provided parameters.
	 *
	 * @param authorizationService the authorization service
	 * @param tokenGenerator       the token generator
	 * @since 0.2.3
	 */
	public OAuthTenantAppUserRefreshTokenAuthenticationProvider(TenantAppUserAuthorizationService authorizationService,
																		OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuthTenantAppUserRefreshTokenAuthenticationToken tokenRequest = (OAuthTenantAppUserRefreshTokenAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(tokenRequest);
		CairoRegisteredClient registeredClient = (CairoRegisteredClient) clientPrincipal.getRegisteredClient();

		if (registeredClient == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_CLIENT,
					"client出错",
					ERROR_URI
				)
			);
		}

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Retrieved registered client");
		}

		OAuth2Authorization authorization = this.authorizationService.findByToken(
			tokenRequest.getTenantId(),
			registeredClient.getAppId(),
			registeredClient.getEndpointId(),
			tokenRequest.getRefreshToken(),
			CairoOAuthTokenTypeConstants.TENANT_APP_USER_REFRESH_TOKEN);

		if (authorization == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"tenantAppUserRefreshToken无效",
					ERROR_URI)
			);
		}

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Retrieved authorization with tenant app endpoint user refresh token");
		}
		if (registeredClient == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_CLIENT,
					"client出错",
					ERROR_URI
				)
			);
		}

		if (!registeredClient.getId().equals(authorization.getRegisteredClientId())) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"clientId错误",
					ERROR_URI)
			);
		}

		if (!registeredClient.getAuthorizationGrantTypes().contains(TENANT_APP_USER_REFRESH_TOKEN)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
					"client未拥有 \"" + TENANT_APP_USER_REFRESH_TOKEN.getValue() + "\" grant_type",
					ERROR_URI
				)
			);
		}

		// As per https://tools.ietf.org/html/rfc6749#section-6
		// The requested scope MUST NOT include any scope not originally granted by the resource owner,
		// and if omitted is treated as equal to the scope originally granted by the resource owner.
		Set<String> scopes = tokenRequest.getScopes();
		Set<String> authorizedScopes = authorization.getAuthorizedScopes();
		if (!authorizedScopes.containsAll(scopes)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_SCOPE,
					"scope错误",
					ERROR_URI)
			);
		}

		if (scopes.isEmpty()) {
			scopes = authorizedScopes;
		}

		OAuth2Authorization.Token<OAuthTenantAppUserRefreshToken> endpointUserRefreshTokenToken = authorization.getToken(OAuthTenantAppUserRefreshToken.class);
		if (endpointUserRefreshTokenToken == null || !endpointUserRefreshTokenToken.isActive()) {
			// As per https://tools.ietf.org/html/rfc6749#section-5.2
			// invalid_grant: The provided authorization grant (e.g., authorization code,
			// resource owner credentials) or refresh token is invalid, expired, revoked [...].
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"tenantAppUserRefreshToken无效",
					ERROR_URI)
			);
		}


		Authentication endpointUserAuthentication = authorization.getAttribute(Principal.class.getName());
		if (endpointUserAuthentication == null || !(endpointUserAuthentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"认证身份错误",
					ERROR_URI)
			);
		}
		CairoAuthTenantAppUser user = (CairoAuthTenantAppUser) endpointUserAuthentication.getPrincipal();

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Validated token request parameters");
		}

		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
			.registeredClient(registeredClient)
			.principal(authorization.getAttribute(Principal.class.getName()))
			.authorizationServerContext(AuthorizationServerContextHolder.getContext())
			.authorization(authorization)
			.authorizedScopes(scopes)
			.authorizationGrantType(TENANT_APP_USER_REFRESH_TOKEN)
			.authorizationGrant(endpointUserAuthentication);


		Map<String, Object> additionalParameters = new HashMap<>();
		additionalParameters.put(CairoOAuthParameterNames.AUTH_TYPE, TENANT_APP_USER.getValue());
		OAuth2TokenFormat accessTokenFormat = registeredClient.getTokenSettings().getSetting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT);
		String accessTokenFormatValue = Optional.ofNullable(accessTokenFormat).orElse(OAuth2TokenFormat.REFERENCE).getValue();
		additionalParameters.put(CairoOAuthParameterNames.ACCESS_TOKEN_FORMAT, accessTokenFormatValue);
		additionalParameters.put(CairoOAuthParameterNames.TENANT_ID, user.getTenantId());
		additionalParameters.put(CairoOAuthParameterNames.APP_ID, user.getAppId());
		additionalParameters.put(CairoOAuthParameterNames.ENDPOINT_ID, user.getEndpointId());
		additionalParameters.put(CairoOAuthParameterNames.USER_ID, user.getUserId());

		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the tenant app endpoint user access token.",
					ERROR_URI)
			);
		}

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Generated tenant app endpoint user access token");
		}

		OAuthTenantAppUserAccessToken accessToken = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			user.getTenantId(),
			user.getAppId(),
			user.getEndpointId(),
			user.getUserId(),
			user.getId(),
			generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
			generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes()
		);
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.token(accessToken, (metadata) -> {
				metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims());
				metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false);
			});
		} else {
			authorizationBuilder.token(accessToken);
		}

		// ----- Refresh token -----
		OAuthTenantAppUserRefreshToken currentRefreshToken = endpointUserRefreshTokenToken.getToken();
		if (!(Boolean) registeredClient.getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.REUSE_TENANT_APP_USER_REFRESH_TOKENS, false)) {
			tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.TENANT_APP_USER_REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuthTenantAppUserRefreshToken)) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the tenant app endpoint user refresh token.",
						ERROR_URI)
				);
			}

			if (this.logger.isTraceEnabled()) {
				this.logger.trace("Generated tenant app endpoint user refresh token");
			}

			currentRefreshToken = (OAuthTenantAppUserRefreshToken) generatedRefreshToken;
			authorizationBuilder.token(currentRefreshToken);
		}

		authorization = authorizationBuilder.build();

		this.authorizationService.save(authorization);

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Saved tenant app endpoint user authorization");
		}

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Authenticated tenant app endpoint user token request");
		}

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, currentRefreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuthTenantAppUserRefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
