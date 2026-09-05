/*
 * Copyright 2020-2022 the original author or authors.
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

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuth2EndpointUtils;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication.SimpleAccountAuthenticationConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
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
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.TENANT_APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.ACCOUNT_ACCESS_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.TENANT_APP_USER_REFRESH_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuthAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * 用户账号授权模式 authentication provider
 */
@Slf4j
public final class OAuthTenantAppUserAccountAccessTokenAuthenticationProvider implements AuthenticationProvider {
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	private final AuthenticationManager authenticationManager;
	private final AccountAuthorizationService accountAuthorizationService;
	private final TenantAppUserAuthorizationService tenantAppUserAuthorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	private final SimpleAccountAuthenticationConverter simpleAccountAuthenticationConverter = new SimpleAccountAuthenticationConverter();

	/**
	 * Constructs an {@code OAuth2RefreshTokenAuthenticationProvider} using the provided parameters.
	 *
	 * @param authenticationManager                     the authentication manager
	 * @param tenantAppUserAuthorizationService the authorization service
	 * @param tokenGenerator                            the token generator
	 * @since 0.2.3
	 */
	public OAuthTenantAppUserAccountAccessTokenAuthenticationProvider(AuthenticationManager authenticationManager,
																			  AccountAuthorizationService accountAuthorizationService,
																			  TenantAppUserAuthorizationService tenantAppUserAuthorizationService,
																			  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator
	) {

		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(accountAuthorizationService, "accountAuthorizationService cannot be null");
		Assert.notNull(tenantAppUserAuthorizationService, "tenantAppUserAuthorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authenticationManager = authenticationManager;
		this.accountAuthorizationService = accountAuthorizationService;
		this.tenantAppUserAuthorizationService = tenantAppUserAuthorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuthTenantAppUserAccountAccessTokenAuthenticationToken token = (OAuthTenantAppUserAccountAccessTokenAuthenticationToken) authentication;
		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(token);
		CairoRegisteredClient registeredClient = (CairoRegisteredClient) clientPrincipal.getRegisteredClient();

		if (registeredClient == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_CLIENT,
					"客户端注册信息缺失",
					ERROR_URI
				)
			);
		}

		if (!registeredClient.getAuthorizationGrantTypes().contains(ACCOUNT_ACCESS_TOKEN)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
					"client未拥有 \"" + ACCOUNT_ACCESS_TOKEN.getValue() + "\" grant_type",
					ERROR_URI
				)
			);
		}

		// As per https://tools.ietf.org/html/rfc6749#section-6
		// The requested scope MUST NOT include any scope not originally granted by the resource owner,
		// and if omitted is treated as equal to the scope originally granted by the resource owner.
		Set<String> authorizedScopes = Collections.emptySet();
		if (!CollectionUtils.isEmpty(token.getScopes())) {
			if (!registeredClient.getScopes().containsAll(token.getScopes())) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.INVALID_SCOPE,
						"请求scope超出客户端许可范围",
						ERROR_URI
					)
				);
			}
			authorizedScopes = new LinkedHashSet<>(token.getScopes());
		}

		CairoAuthAccount accountPrincipal = null;
		try {
			String accountAccessTokenValue = token.getAccountAccessToken();
			if (accountAccessTokenValue == null) {
				OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN, OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
			}
			OAuth2Authorization oAuth2Authorization = accountAuthorizationService.findByToken(accountAccessTokenValue, CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN);
			Authentication accountAuthentication = Optional.ofNullable(oAuth2Authorization).<Authentication>map(x -> x.getAttribute(Principal.class.getName())).orElse(null);
			if (accountAuthentication == null || !(accountAuthentication.getPrincipal() instanceof CairoAuthAccount)) {
				OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN, OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
			}
			accountPrincipal = (CairoAuthAccount) accountAuthentication.getPrincipal();

		} catch (RuntimeException e) {
			log.warn("e", e);
			OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN, OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		if (accountPrincipal == null || accountPrincipal.getAccountId() == null) {
			OAuth2EndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CairoOAuthParameterNames.ACCOUNT_ACCESS_TOKEN, OAuth2EndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
		}

		CairoTenantAppUserAccountAuthenticationToken userAccountAuthenticationToken = new CairoTenantAppUserAccountAuthenticationToken(
			token.getTenantId(),
			registeredClient.getAppId(),
			registeredClient.getEndpointId(),
			registeredClient.getClientId(),
			accountPrincipal.getAccountId(),
			accountPrincipal.getLoginType()
		);
		Authentication newTenantAppUserAuthentication = authenticationManager.authenticate(userAccountAuthenticationToken);

		if (newTenantAppUserAuthentication == null || !(newTenantAppUserAuthentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"认证主体类型不符",
					ERROR_URI)
			);
		}
		CairoAuthTenantAppUser user = (CairoAuthTenantAppUser) newTenantAppUserAuthentication.getPrincipal();


		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		OAuth2Authorization newUserAuthorization = builder.authorizationGrantType(ACCOUNT_ACCESS_TOKEN)
			.principalName(newTenantAppUserAuthentication.getName())
			.attribute(Principal.class.getName(), newTenantAppUserAuthentication)
			.authorizedScopes(authorizedScopes)
			.build();

		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
			.registeredClient(registeredClient)
			.principal(newUserAuthorization.getAttribute(Principal.class.getName()))
			.authorizationServerContext(AuthorizationServerContextHolder.getContext())
			.authorization(newUserAuthorization)
			.authorizedScopes(authorizedScopes)
			.authorizationGrantType(ACCOUNT_ACCESS_TOKEN)
			.authorizationGrant(newTenantAppUserAuthentication);

		Map<String, Object> additionalParameters = new HashMap<>();
		additionalParameters.put(CairoOAuthParameterNames.AUTH_TYPE, TENANT_APP_USER.getValue());
		OAuth2TokenFormat accessTokenFormat = registeredClient.getTokenSettings().getSetting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT);
		String accessTokenFormatValue = Optional.ofNullable(accessTokenFormat).orElse(OAuth2TokenFormat.REFERENCE).getValue();
		additionalParameters.put(CairoOAuthParameterNames.ACCESS_TOKEN_FORMAT, accessTokenFormatValue);
		additionalParameters.put(CairoOAuthParameterNames.TENANT_ID, user.getTenantId());
		additionalParameters.put(CairoOAuthParameterNames.APP_ID, user.getAppId());
		additionalParameters.put(CairoOAuthParameterNames.ENDPOINT_ID, user.getEndpointId());
		additionalParameters.put(CairoOAuthParameterNames.USER_ID, user.getUserId());

		OAuth2Authorization.Builder newUserAuthorizationBuilder = OAuth2Authorization.from(newUserAuthorization);

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the tenant endpoint user access token.",
					ERROR_URI)
			);
		}

		if (log.isTraceEnabled()) {
			log.trace("Generated tenant app user access token");
		}

		OAuthTenantAppUserAccessToken accessToken = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			user.getTenantId(),
			user.getAppId(),
			user.getEndpointId(),
			user.getUserId(),
			user.getId(),
			generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
			generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
		if (generatedAccessToken instanceof ClaimAccessor) {
			newUserAuthorizationBuilder.token(accessToken, (metadata) ->
				metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			newUserAuthorizationBuilder.accessToken(accessToken);
		}

		// ----- Refresh token -----
		OAuthTenantAppUserRefreshToken refreshToken = null;

		if (registeredClient.getAuthorizationGrantTypes().contains(TENANT_APP_USER_REFRESH_TOKEN) &&
			// Do not issue refresh token to public client
			!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.TENANT_APP_USER_REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuthTenantAppUserRefreshToken)) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the tenant endpoint user refresh token.",
						ERROR_URI)
				);
			}

			if (log.isTraceEnabled()) {
				log.trace("Generated tenant app user refresh token");
			}

			refreshToken = (OAuthTenantAppUserRefreshToken) generatedRefreshToken;
			newUserAuthorizationBuilder.token(refreshToken);
		}

		newUserAuthorization = newUserAuthorizationBuilder.build();

		this.tenantAppUserAuthorizationService.save(newUserAuthorization);

		if (log.isTraceEnabled()) {
			log.trace("Saved tenant app user authorization");
		}

		if (log.isTraceEnabled()) {
			log.trace("Authenticated tenant app user token request");
		}

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuthTenantAppUserAccountAccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
