package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.AbstractSecurityMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

public class AccountAuthorizationMongodbMapper extends AbstractSecurityMapper implements Function<OAuth2Authorization, AccountAuthorizationMongodb> {
	private final RegisteredClientRepository registeredClientRepository;

	public AccountAuthorizationMongodbMapper(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public AccountAuthorizationMongodb apply(OAuth2Authorization oAuth2Authorization) {
		AccountAuthorizationMongodb.AccountAuthorizationMongodbBuilder builder = AccountAuthorizationMongodb.builder();
		builder.tokenId(oAuth2Authorization.getId());

		Authentication accountAuthentication = (Authentication) oAuth2Authorization.getAttributes().get(Principal.class.getName());

		if (accountAuthentication == null || !(accountAuthentication.getPrincipal() instanceof CairoAuthAccount)) {
			throw new RuntimeException("不合法的认证,无法转换成account authorization");
		}

		CairoRegisteredClient registeredClient = (CairoRegisteredClient) registeredClientRepository.findById(oAuth2Authorization.getRegisteredClientId());
		if (registeredClient == null) {
			throw new DataRetrievalFailureException("The RegisteredClient with id '" + oAuth2Authorization.getRegisteredClientId() + "' was not found in the RegisteredClientRepository.");
		}

		CairoAuthAccount account = (CairoAuthAccount) accountAuthentication.getPrincipal();

		builder
			.accountId(account.getAccountId())
			.accountName(account.getNickname())
			.loginType(account.getLoginType().getValue())
			.snsType(account.getSnsType())
			.appId(registeredClient.getAppId())
			.clientId(registeredClient.getClientId())
			.registeredClientId(oAuth2Authorization.getRegisteredClientId())
			.authorizationGrantType(oAuth2Authorization.getAuthorizationGrantType().getValue())
			.authorizedScopes(oAuth2Authorization.getAuthorizedScopes())
			.attributes(writeMap(oAuth2Authorization.getAttributes()))
		;


		// account
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthAccountAccessToken.class))
			.map(this::getAccountAccessToken)
			.ifPresent(builder::accessToken);
		Optional.ofNullable(oAuth2Authorization.getToken(OAuthAccountRefreshToken.class))
			.map(this::getAccountRefreshToken)
			.ifPresent(builder::refreshToken);

		return builder.build();
	}

	public <T extends AbstractOAuth2Token> AccountAuthorizationMongodb.Token getToken(OAuth2Authorization.Token<T> token) {
		AccountAuthorizationMongodb.Token.TokenBuilder<?, ?> builder = AccountAuthorizationMongodb.Token.builder();
		if (token != null) {
			builder
				.tokenValue(token.getToken().getTokenValue())
				.issuedAt(token.getToken().getIssuedAt())
				.expiresAt(token.getToken().getExpiresAt())
				.metadata(writeMap(token.getMetadata()));

		}
		return builder.build();
	}

	public <T extends AbstractOAuth2Token> AccountAuthorizationMongodb.AccessToken getAccountAccessToken(OAuth2Authorization.Token<OAuthAccountAccessToken> token) {
		AccountAuthorizationMongodb.AccessToken.AccessTokenBuilder<?, ?> builder = AccountAuthorizationMongodb.AccessToken.builder();
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

	public <T extends AbstractOAuth2Token> AccountAuthorizationMongodb.RefreshToken getAccountRefreshToken(OAuth2Authorization.Token<OAuthAccountRefreshToken> token) {
		AccountAuthorizationMongodb.RefreshToken.RefreshTokenBuilder<?, ?> builder = AccountAuthorizationMongodb.RefreshToken.builder();
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
