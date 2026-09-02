package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAppUserAccountAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user.AppUserAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuth2EndpointUtils;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.APP_USER;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAuthorizationGrantTypes.ACCOUNT_ACCESS_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAuthorizationGrantTypes.APP_USER_REFRESH_TOKEN;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuthAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient;


/**
 * 应用用户账号授权模式 authentication provider
 * <p>
 * 使用账号级 access_token 置换应用用户 access_token
 */
@Slf4j
public final class OAuthAppUserAccountAccessTokenAuthenticationProvider implements AuthenticationProvider {
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	private final AuthenticationManager authenticationManager;
	private final AccountAuthorizationService accountAuthorizationService;
	private final AppUserAuthorizationService appUserAuthorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	/**
	 * Constructs an {@code OAuth2RefreshTokenAuthenticationProvider} using the provided parameters.
	 *
	 * @param authenticationManager          the authentication manager
	 * @param accountAuthorizationService    the account authorization service
	 * @param appUserAuthorizationService    the app user authorization service
	 * @param tokenGenerator                 the token generator
	 */
	public OAuthAppUserAccountAccessTokenAuthenticationProvider(AuthenticationManager authenticationManager,
																 AccountAuthorizationService accountAuthorizationService,
																 AppUserAuthorizationService appUserAuthorizationService,
																 OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator
	) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(accountAuthorizationService, "accountAuthorizationService cannot be null");
		Assert.notNull(appUserAuthorizationService, "appUserAuthorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authenticationManager = authenticationManager;
		this.accountAuthorizationService = accountAuthorizationService;
		this.appUserAuthorizationService = appUserAuthorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuthAppUserAccountAccessTokenAuthenticationToken token = (OAuthAppUserAccountAccessTokenAuthenticationToken) authentication;
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
						"scope 权限不足",
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

		CairoAppUserAccountAuthenticationToken userAccountAuthenticationToken = new CairoAppUserAccountAuthenticationToken(
			registeredClient.getAppId(),
			registeredClient.getEndpointId(),
			registeredClient.getClientId(),
			accountPrincipal.getAccountId(),
			accountPrincipal.getLoginType()
		);
		Authentication newUserAuthentication = authenticationManager.authenticate(userAccountAuthenticationToken);

		if (newUserAuthentication == null || !(newUserAuthentication.getPrincipal() instanceof CairoAuthAppUser)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"认证身份错误",
					ERROR_URI)
			);
		}
		CairoAuthAppUser user = (CairoAuthAppUser) newUserAuthentication.getPrincipal();

		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		OAuth2Authorization newUserAuthorization = builder.authorizationGrantType(ACCOUNT_ACCESS_TOKEN)
			.principalName(newUserAuthentication.getName())
			.attribute(Principal.class.getName(), newUserAuthentication)
			.authorizedScopes(authorizedScopes)
			.build();

		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
			.registeredClient(registeredClient)
			.principal(newUserAuthorization.getAttribute(Principal.class.getName()))
			.authorizationServerContext(AuthorizationServerContextHolder.getContext())
			.authorization(newUserAuthorization)
			.authorizedScopes(authorizedScopes)
			.authorizationGrantType(ACCOUNT_ACCESS_TOKEN)
			.authorizationGrant(newUserAuthentication);

		Map<String, Object> additionalParameters = new HashMap<>();
		additionalParameters.put(CairoOAuthParameterNames.AUTH_TYPE, APP_USER.getValue());
		OAuth2TokenFormat accessTokenFormat = registeredClient.getTokenSettings().getSetting(CairoSettingNames.Token.APP_USER_ACCESS_TOKEN_FORMAT);
		String accessTokenFormatValue = Optional.ofNullable(accessTokenFormat).orElse(OAuth2TokenFormat.REFERENCE).getValue();
		additionalParameters.put(CairoOAuthParameterNames.ACCESS_TOKEN_FORMAT, accessTokenFormatValue);
		additionalParameters.put(CairoOAuthParameterNames.APP_ID, user.getAppId());
		additionalParameters.put(CairoOAuthParameterNames.ENDPOINT_ID, user.getEndpointId());
		additionalParameters.put(CairoOAuthParameterNames.USER_ID, user.getUserId());

		OAuth2Authorization.Builder newUserAuthorizationBuilder = OAuth2Authorization.from(newUserAuthorization);

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the app user access token.",
					ERROR_URI)
			);
		}

		if (log.isTraceEnabled()) {
			log.trace("Generated app user access token");
		}

		OAuthAppUserAccessToken accessToken = new OAuthAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
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
			newUserAuthorizationBuilder.token(accessToken, (metadata) ->
				metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			newUserAuthorizationBuilder.accessToken(accessToken);
		}

		// ----- Refresh token -----
		OAuthAppUserRefreshToken refreshToken = null;

		if (registeredClient.getAuthorizationGrantTypes().contains(APP_USER_REFRESH_TOKEN) &&
			// Do not issue refresh token to public client
			!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.APP_USER_REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuthAppUserRefreshToken)) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the app user refresh token.",
						ERROR_URI)
				);
			}

			if (log.isTraceEnabled()) {
				log.trace("Generated app user refresh token");
			}

			refreshToken = (OAuthAppUserRefreshToken) generatedRefreshToken;
			newUserAuthorizationBuilder.token(refreshToken);
		}

		newUserAuthorization = newUserAuthorizationBuilder.build();

		this.appUserAuthorizationService.save(newUserAuthorization);

		if (log.isTraceEnabled()) {
			log.trace("Saved app user authorization");
		}

		if (log.isTraceEnabled()) {
			log.trace("Authenticated app user token request");
		}

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuthAppUserAccountAccessTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
