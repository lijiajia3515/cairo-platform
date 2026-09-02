package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAccountPasswordAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account.AccountAuthorizationService;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.OAuthAuthenticationProviderUtils;
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

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.ACCOUNT;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_PASSWORD;
import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_REFRESH_TOKEN;

/**
 * 账号密码模式 authentication provider
 */
@Slf4j
public final class OAuthAccountPasswordAuthenticationProvider implements AuthenticationProvider {

	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

	private final AuthenticationManager authenticationManager;
	private final AccountAuthorizationService authorizationService;

	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;


	/**
	 * Constructs an {@code OAuth2AuthorizationCodeAuthenticationProvider} using the provided parameters.
	 *
	 * @param authenticationManager authenticationManager
	 * @param authorizationService  the authorization service
	 * @since 0.2.3
	 */
	public OAuthAccountPasswordAuthenticationProvider(AuthenticationManager authenticationManager,
													  AccountAuthorizationService authorizationService,
													  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuthAccountPasswordAuthenticationToken token = (OAuthAccountPasswordAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = OAuthAuthenticationProviderUtils.getAuthenticatedClientElseThrowInvalidClient(token);
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

		if (!registeredClient.getAuthorizationGrantTypes().contains(ACCOUNT_PASSWORD)) {

			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
					"client未拥有 \"" + ACCOUNT_PASSWORD.getValue() + "\" grant_type",
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

		CairoAccountPasswordAuthenticationToken accountPasswordAuthenticationToken = new CairoAccountPasswordAuthenticationToken(
			token.getUsername(),
			token.getPassword()
		);

		Authentication accountAuthentication = authenticationManager.authenticate(accountPasswordAuthenticationToken);

		if (accountAuthentication == null || !(accountAuthentication.getPrincipal() instanceof CairoAuthAccount)) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.INVALID_GRANT,
					"认证身份错误",
					ERROR_URI)
			);
		}
		CairoAuthAccount user = (CairoAuthAccount) accountAuthentication.getPrincipal();

		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		OAuth2Authorization authorization = builder.authorizationGrantType(ACCOUNT_PASSWORD)
			.principalName(accountAuthentication.getName())
			.attribute(Principal.class.getName(), accountAuthentication)
			.authorizedScopes(authorizedScopes)
			.build();

		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
			.registeredClient(registeredClient)
			.principal(authorization.getAttribute(Principal.class.getName()))
			.authorizationServerContext(AuthorizationServerContextHolder.getContext())
			.authorization(authorization)
			.authorizedScopes(authorization.getAuthorizedScopes())
			.authorizationGrantType(ACCOUNT_PASSWORD)
			.authorizationGrant(accountAuthentication);

		Map<String, Object> additionalParameters = new HashMap<>();
		additionalParameters.put(CairoOAuthParameterNames.AUTH_TYPE, ACCOUNT.getValue());
		OAuth2TokenFormat accessTokenFormat = registeredClient.getTokenSettings().getSetting(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_FORMAT);
		String accessTokenFormatValue = Optional.ofNullable(accessTokenFormat).orElse(OAuth2TokenFormat.REFERENCE).getValue();
		additionalParameters.put(CairoOAuthParameterNames.ACCESS_TOKEN_FORMAT, accessTokenFormatValue);
		additionalParameters.put(CairoOAuthParameterNames.ACCOUNT_ID, user.getAccountId());

		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization);

		// ----- Account Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			throw new OAuth2AuthenticationException(
				new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the account access token.",
					ERROR_URI)
			);
		}

		if (log.isTraceEnabled()) {
			log.trace("Generated account access token");
		}
		OAuthAccountAccessToken accountAccessToken = new OAuthAccountAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			user.getAccountId(),
			user.getId(),
			generatedAccessToken.getTokenValue(),
			generatedAccessToken.getIssuedAt(),
			generatedAccessToken.getExpiresAt(),
			tokenContext.getAuthorizedScopes()
		);
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.token(accountAccessToken, (metadata) ->
				metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			authorizationBuilder.accessToken(accountAccessToken);
		}

		// -----Account Refresh token -----
		OAuthAccountRefreshToken accountRefreshToken = null;

		if (registeredClient.getAuthorizationGrantTypes().contains(ACCOUNT_REFRESH_TOKEN) &&
			// Do not issue refresh token to public client
			!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			tokenContext = tokenContextBuilder.tokenType(CairoOAuthTokenTypeConstants.ACCOUNT_REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuthAccountRefreshToken)) {
				throw new OAuth2AuthenticationException(
					new OAuth2Error(
						OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the account refresh token.",
						ERROR_URI)
				);
			}

			if (log.isTraceEnabled()) {
				log.trace("Generated account refresh token");
			}

			accountRefreshToken = (OAuthAccountRefreshToken) generatedRefreshToken;
			authorizationBuilder.token(accountRefreshToken);
		}


		authorization = authorizationBuilder.build();

		this.authorizationService.save(authorization);

		if (log.isTraceEnabled()) {
			log.trace("Saved authorization");
		}

		if (log.isTraceEnabled()) {
			log.trace("Authenticated token request");
		}

		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accountAccessToken, accountRefreshToken, additionalParameters);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuthAccountPasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
