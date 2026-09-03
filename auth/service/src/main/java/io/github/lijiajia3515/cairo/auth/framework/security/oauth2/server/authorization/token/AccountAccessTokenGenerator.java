package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token;

import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;

/**
 * An {@link OAuth2TokenGenerator} that generates a
 * {@link OAuth2TokenFormat#REFERENCE "reference"} (opaque) {@link OAuth2AccessToken}.
 *
 * @author Joe Grandja
 * @see OAuth2TokenGenerator
 * @see OAuth2AccessToken
 * @see OAuth2TokenCustomizer
 * @see OAuth2TokenClaimsContext
 * @see OAuth2TokenClaimsSet
 * @since 0.2.3
 */
public final class AccountAccessTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {
	private final StringKeyGenerator accessTokenGenerator = new TokenKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 16, "account_at_");

	private OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer;

	@Nullable
	@Override
	public OAuthAccountAccessToken generate(OAuth2TokenContext context) {
		if (!CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN.equals(context.getTokenType())) {
			return null;
		}

		OAuth2TokenFormat tokenFormat = (OAuth2TokenFormat) context.getRegisteredClient().getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_FORMAT, OAuth2TokenFormat.SELF_CONTAINED);
		if (!OAuth2TokenFormat.REFERENCE.equals(tokenFormat)) {
			return null;
		}

		if (!(context.getPrincipal() instanceof UsernamePasswordAuthenticationToken)) {
			return null;
		}

		Authentication authentication = context.getPrincipal();
		if (!(authentication.getPrincipal() instanceof CairoAuthAccount)) {
			return null;
		}

		CairoAuthAccount authAccount = (CairoAuthAccount) authentication.getPrincipal();
		String accountId = authAccount.getAccountId();

		String issuer = null;
		if (context.getAuthorizationServerContext() != null) {
			issuer = context.getAuthorizationServerContext().getIssuer();
		}
		CairoRegisteredClient registeredClient = (CairoRegisteredClient) context.getRegisteredClient();

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus((Duration) registeredClient.getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(30)));

		OAuth2TokenClaimsSet.Builder claimsBuilder = OAuth2TokenClaimsSet.builder();
		if (StringUtils.hasText(issuer)) {
			claimsBuilder.issuer(issuer);
		}
		claimsBuilder
			.subject(context.getAuthorization().getId())
			.audience(Collections.singletonList(registeredClient.getClientId()))
			.issuedAt(issuedAt)
			.expiresAt(expiresAt)
			.notBefore(issuedAt)
			.claim("access_token_format", OAuth2TokenFormat.REFERENCE.getValue())
		;

		if (!CollectionUtils.isEmpty(context.getAuthorizedScopes())) {
			claimsBuilder.claim(OAuth2ParameterNames.SCOPE, context.getAuthorizedScopes());
		}

		if (this.accessTokenCustomizer != null) {
			OAuth2TokenClaimsContext.Builder accessTokenContextBuilder = OAuth2TokenClaimsContext.with(claimsBuilder)
				.registeredClient(context.getRegisteredClient())
				.principal(context.getPrincipal())
				.authorizationServerContext(context.getAuthorizationServerContext())
				.authorizedScopes(context.getAuthorizedScopes())
				.tokenType(context.getTokenType())
				.authorizationGrantType(context.getAuthorizationGrantType())
				;
			if (context.getAuthorization() != null) {
				accessTokenContextBuilder.authorization(context.getAuthorization());
			}
			if (context.getAuthorizationGrant() != null) {
				accessTokenContextBuilder.authorizationGrant(context.getAuthorizationGrant());
			}

			OAuth2TokenClaimsContext accessTokenContext = accessTokenContextBuilder.build();
			this.accessTokenCustomizer.customize(accessTokenContext);
		}

		OAuth2TokenClaimsSet accessTokenClaimsSet = claimsBuilder.build();

		OAuthAccountAccessToken accountAccessToken = new OAuthAccountAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			accountId,
			context.getAuthorization().getId(),
			this.accessTokenGenerator.generateKey(),
			accessTokenClaimsSet.getIssuedAt(),
			accessTokenClaimsSet.getExpiresAt(),
			context.getAuthorizedScopes(),
			accessTokenClaimsSet.getClaims()
		);

		return accountAccessToken;
	}

	/**
	 * Sets the {@link OAuth2TokenCustomizer} that customizes the
	 * {@link OAuth2TokenClaimsContext#getClaims() claims} for the {@link OAuth2AccessToken}.
	 *
	 * @param accessTokenCustomizer the {@link OAuth2TokenCustomizer} that customizes the claims for the {@code OAuth2AccessToken}
	 */
	public void setAccessTokenCustomizer(OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer) {
		Assert.notNull(accessTokenCustomizer, "accessTokenCustomizer cannot be null");
		this.accessTokenCustomizer = accessTokenCustomizer;
	}


}
