package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUser;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.AbstractSecurityMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

public class AppUserAuthorizationMongodbMapper extends AbstractSecurityMapper implements Function<OAuth2Authorization, AppUserAuthorizationMongodb> {
	@Override
	public AppUserAuthorizationMongodb apply(OAuth2Authorization oAuth2Authorization) {
		AppUserAuthorizationMongodb.AppUserAuthorizationMongodbBuilder builder = AppUserAuthorizationMongodb.builder();
		builder.tokenId(oAuth2Authorization.getId());

		Authentication accountAuthentication = (Authentication) oAuth2Authorization.getAttributes().get(Principal.class.getName());

		if (accountAuthentication == null || !(accountAuthentication.getPrincipal() instanceof CairoAuthAppUser)) {
			throw new RuntimeException("不合法的认证,无法转换成app user authorization");
		}
		CairoAuthAppUser user = (CairoAuthAppUser) accountAuthentication.getPrincipal();

		builder
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
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthAppUserAccessToken.class))
			.map(this::getAccessToken)
			.ifPresent(builder::accessToken);
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthAppUserRefreshToken.class))
			.map(this::getRefreshToken)
			.ifPresent(builder::refreshToken);


		return builder.build();
	}

	public <T extends AbstractOAuth2Token> AppUserAuthorizationMongodb.Token getToken(OAuth2Authorization.Token<T> token) {
		AppUserAuthorizationMongodb.Token.TokenBuilder<?, ?> builder = AppUserAuthorizationMongodb.Token.builder();
		if (token != null) {
			builder
				.tokenValue(token.getToken().getTokenValue())
				.issuedAt(token.getToken().getIssuedAt())
				.expiresAt(token.getToken().getExpiresAt())
				.metadata(writeMap(token.getMetadata()));

		}
		return builder.build();
	}

	public <T extends AbstractOAuth2Token> AppUserAuthorizationMongodb.AccessToken getAccessToken(OAuth2Authorization.Token<OAuthAppUserAccessToken> token) {
		AppUserAuthorizationMongodb.AccessToken.AccessTokenBuilder<?, ?> builder = AppUserAuthorizationMongodb.AccessToken.builder();
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

	public <T extends AbstractOAuth2Token> AppUserAuthorizationMongodb.RefreshToken getRefreshToken(OAuth2Authorization.Token<OAuthAppUserRefreshToken> token) {
		AppUserAuthorizationMongodb.RefreshToken.RefreshTokenBuilder<?, ?> builder = AppUserAuthorizationMongodb.RefreshToken.builder();
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
