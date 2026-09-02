package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoTenantAppUserVerifyCodeAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user.TenantAppUserAuthorizationService;
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
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.ACCOUNT_VERIFY_CODE;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.TENANT_APP_USER_REFRESH_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuthAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * 终端用户验证码模式 authentication provider
 */
@Slf4j
public final class OAuthTenantAppUserVerifyCodeAuthenticationProvider implements AuthenticationProvider {

	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	private final AuthenticationManager authenticationManager;
	private final TenantAppUserAuthorizationService authorizationService;

	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;


	/**
	 * Constructs an {@code OAuth2AuthorizationCodeAuthenticationProvider} using the provided parameters.
	 *
	 * @param authenticationManager authenticationManager
	 * @param authorizationService  the authorization service
	 * @since 0.2.3
	 */
	public OAuthTenantAppUserVerifyCodeAuthenticationProvider(AuthenticationManager authenticationManager, TenantAppUserAuthorizationService authorizationService, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuthTenantAppUserVerifyCodeAuthenticationToken token = (OAuthTenantAppUserVerifyCodeAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(token);
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

		if (!registeredClient.getAuthorizationGrantTypes().contains(ACCOUNT_VERIFY_CODE)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
					"client未拥有 \"" + ACCOUNT_VERIFY_CODE.getValue() + "\" grant_type",
					ERROR_URI
				)
			);
		}

		Set<String> authorizedScopes = Collections.emptySet();
		if (!CollectionUtils.isEmpty(token.getScopes())) {
			if (!registeredClient.getScopes().containsAll(token.getScopes())) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.INVALID_SCOPE,
						"scope 权限不足",
						ERROR_URI
					)
				);
			}
			authorizedScopes = new LinkedHashSet<>(token.getScopes());
		}

		CairoTenantAppUserVerifyCodeAuthenticationToken cairoTenantAppUserVerifyCodeAuthenticationToken = new CairoTenantAppUserVerifyCodeAuthenticationToken(
			token.getTenantId(),
			registeredClient.getAppId(),
			registeredClient.getEndpointId(),
			registeredClient.getClientId(),
			token.getPhoneNumber(),
			token.getVerifyCode()
		);

		Authentication userAuthentication = authenticationManager.authenticate(cairoTenantAppUserVerifyCodeAuthenticationToken);

		if (userAuthentication == null || !(userAuthentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"The authentication only support CairoAuthTenantAppUser Principal",
					ERROR_URI)
			);
		}
		CairoAuthTenantAppUser user = (CairoAuthTenantAppUser) userAuthentication.getPrincipal();

		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		OAuth2Authorization authorization = builder.authorizationGrantType(ACCOUNT_VERIFY_CODE)
			.principalName(userAuthentication.getName())
			.attribute(Principal.class.getName(), userAuthentication)
			.authorizedScopes(authorizedScopes)
			.build();

		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
			.registeredClient(registeredClient)
			.principal(authorization.getAttribute(Principal.class.getName()))
			.authorizationServerContext(AuthorizationServerContextHolder.getContext())
			.authorization(authorization)
			.authorizedScopes(authorization.getAuthorizedScopes())
			.authorizationGrantType(ACCOUNT_VERIFY_CODE)
			.authorizationGrant(userAuthentication);

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

		if (log.isTraceEnabled()) {
			log.trace("Generated tenant app endpoint user access token");
		}

		OAuthTenantAppUserAccessToken accessToken = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			user.getTenantId(),
			user.getAppId(),
			user.getEndpointId(),
			user.getUserId(),
			user.getId(),
			generatedAccessToken.getTokenValue(),
			generatedAccessToken.getIssuedAt(),
			generatedAccessToken.getExpiresAt(),
			tokenContext.getAuthorizedScopes()
		);
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.token(accessToken, (metadata) ->
				metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			authorizationBuilder.accessToken(accessToken);
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
						"The token generator failed to generate the tenant app endpoint user refresh token.",
						ERROR_URI)
				);
			}

			if (log.isTraceEnabled()) {
				log.trace("Generated tenant app endpoint user refresh token");
			}

			refreshToken = (OAuthTenantAppUserRefreshToken) generatedRefreshToken;
			authorizationBuilder.token(refreshToken);
		}

		authorization = authorizationBuilder.build();

		this.authorizationService.save(authorization);

		if (log.isTraceEnabled()) {
			log.trace("Saved tenant app endpoint user authorization");
		}

		if (log.isTraceEnabled()) {
			log.trace("Authenticated tenant app endpoint user token request");
		}

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuthTenantAppUserVerifyCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
