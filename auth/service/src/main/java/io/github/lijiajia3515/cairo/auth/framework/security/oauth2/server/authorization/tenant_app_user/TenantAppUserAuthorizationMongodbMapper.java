package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user.CairoAuthTenantAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.AbstractSecurityMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

public class TenantAppUserAuthorizationMongodbMapper extends AbstractSecurityMapper implements Function<OAuth2Authorization, TenantAppUserAuthorizationMongodb> {
	@Override
	public TenantAppUserAuthorizationMongodb apply(OAuth2Authorization oAuth2Authorization) {
		TenantAppUserAuthorizationMongodb.TenantAppUserAuthorizationMongodbBuilder builder = TenantAppUserAuthorizationMongodb.builder();
		builder.tokenId(oAuth2Authorization.getId());

		Authentication accountAuthentication = (Authentication) oAuth2Authorization.getAttributes().get(Principal.class.getName());

		if (accountAuthentication == null || !(accountAuthentication.getPrincipal() instanceof CairoAuthTenantAppUser)) {
			throw new RuntimeException("不合法的认证,无法转换成tenant app endpoint user authorization");
		}
		CairoAuthTenantAppUser user = (CairoAuthTenantAppUser) accountAuthentication.getPrincipal();

		builder
			.tenantId(user.getTenantId())
			.appId(user.getAppId())
			.endpointId(user.getEndpointId())
			.userId(user.getUserId())
			.userName(user.getUsername())
			.loginType(user.getLoginType().getValue())
			.snsType(user.getSnsType())
			.clientId(user.getClientId())
			.registeredClientId(oAuth2Authorization.getRegisteredClientId())
			.authorizationGrantType(oAuth2Authorization.getAuthorizationGrantType().getValue())
			.authorizedScopes(oAuth2Authorization.getAuthorizedScopes())
			.attributes(writeMap(oAuth2Authorization.getAttributes()))
		;


		// user
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthTenantAppUserAccessToken.class))
			.map(this::getAccessToken)
			.ifPresent(builder::accessToken);
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthTenantAppUserRefreshToken.class))
			.map(this::getRefreshToken)
			.ifPresent(builder::refreshToken);


		return builder.build();
	}

	public <T extends AbstractOAuth2Token> TenantAppUserAuthorizationMongodb.Token getToken(OAuth2Authorization.Token<T> token) {
		TenantAppUserAuthorizationMongodb.Token.TokenBuilder<?, ?> builder = TenantAppUserAuthorizationMongodb.Token.builder();
		if (token != null) {
			builder
				.tokenValue(token.getToken().getTokenValue())
				.issuedAt(token.getToken().getIssuedAt())
				.expiresAt(token.getToken().getExpiresAt())
				.metadata(writeMap(token.getMetadata()));

		}
		return builder.build();
	}

	public <T extends AbstractOAuth2Token> TenantAppUserAuthorizationMongodb.AccessToken getAccessToken(OAuth2Authorization.Token<OAuthTenantAppUserAccessToken> token) {
		TenantAppUserAuthorizationMongodb.AccessToken.AccessTokenBuilder<?, ?> builder = TenantAppUserAuthorizationMongodb.AccessToken.builder();
		if (token != null) {
			builder
				.tokenType(token.getToken().getTokenType().getValue())
				.tokenValue(token.getToken().getTokenValue())
				.issuedAt(token.getToken().getIssuedAt())
				.expiresAt(token.getToken().getExpiresAt())
				.scopes(token.getToken().getScopes())
				.metadata(writeMap(token.getMetadata()))
			;

		}
		return builder.build();
	}

	public <T extends AbstractOAuth2Token> TenantAppUserAuthorizationMongodb.RefreshToken getRefreshToken(OAuth2Authorization.Token<OAuthTenantAppUserRefreshToken> token) {
		TenantAppUserAuthorizationMongodb.RefreshToken.RefreshTokenBuilder<?, ?> builder = TenantAppUserAuthorizationMongodb.RefreshToken.builder();
		if (token != null) {
			builder
				.tokenValue(token.getToken().getTokenValue())
				.issuedAt(token.getToken().getIssuedAt())
				.expiresAt(token.getToken().getExpiresAt())
				.metadata(writeMap(token.getMetadata()));

		}
		return builder.build();
	}

}
