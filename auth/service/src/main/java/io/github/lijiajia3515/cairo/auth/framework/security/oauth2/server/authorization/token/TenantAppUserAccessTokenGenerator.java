package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.token;

import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

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
public final class TenantAppUserAccessTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {
	private final StringKeyGenerator accessTokenGenerator = new TokenKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 16, "taeuat_");
	private OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer;

	@Nullable
	@Override
	public OAuth2AccessToken generate(OAuth2TokenContext context) {
		if (!CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN.equals(context.getTokenType())) {
			return null;
		}

		OAuth2TokenFormat tokenFormat = (OAuth2TokenFormat) context.getRegisteredClient().getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT, OAuth2TokenFormat.SELF_CONTAINED);
		if (!OAuth2TokenFormat.REFERENCE.equals(tokenFormat)) {
			return null;
		}

		String issuer = null;
		if (context.getAuthorizationServerContext() != null) {
			issuer = context.getAuthorizationServerContext().getIssuer();
		}

		Authentication authentication = context.getPrincipal();
		if (!(authentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			return null;
		}

		CairoAuthTenantAppUser user = (CairoAuthTenantAppUser) authentication.getPrincipal();
		String tenantId = user.getTenantId();
		String appId = user.getAppId();
		String endpointId = user.getEndpointId();
		String userId = user.getUserId();
		String tokenId = user.getId();

		RegisteredClient registeredClient = context.getRegisteredClient();

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus((Duration) registeredClient.getTokenSettings().getSettings().getOrDefault(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, Duration.ofMinutes(30)));


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
			.id(UUID.randomUUID().toString());
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
				.authorizationGrantType(context.getAuthorizationGrantType());
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

		OAuthTenantAppUserAccessToken endpointUserAccessToken = new OAuthTenantAppUserAccessToken(
			OAuth2AccessToken.TokenType.BEARER,
			tenantId,
			appId,
			endpointId,
			userId,
			tokenId,
			this.accessTokenGenerator.generateKey(),
			accessTokenClaimsSet.getIssuedAt(),
			accessTokenClaimsSet.getExpiresAt(),
			context.getAuthorizedScopes(),
			accessTokenClaimsSet.getClaims()
		);

		return endpointUserAccessToken;
	}

}
