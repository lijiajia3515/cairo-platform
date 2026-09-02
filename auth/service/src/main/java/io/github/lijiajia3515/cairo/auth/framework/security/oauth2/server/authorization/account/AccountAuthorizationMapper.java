package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.AbstractSecurityMapper;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.function.Function;

import static org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token.INVALIDATED_METADATA_NAME;

public class AccountAuthorizationMapper extends AbstractSecurityMapper implements Function<AccountAuthorizationMongodb, OAuth2Authorization> {
	private final RegisteredClientRepository registeredClientRepository;

	public AccountAuthorizationMapper(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public OAuth2Authorization apply(AccountAuthorizationMongodb mongodb) {
		String registeredClientId = mongodb.getRegisteredClientId();
		RegisteredClient registeredClient = registeredClientRepository.findById(registeredClientId);
		if (registeredClient == null) {
			throw new DataRetrievalFailureException("The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
		}
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
			.id(mongodb.getTokenId())
			.principalName(mongodb.getAccountName())
			.authorizationGrantType(new AuthorizationGrantType(mongodb.getAuthorizationGrantType()))
			.authorizedScopes(mongodb.getAuthorizedScopes())
			.attributes(attr -> {
				attr.putAll(parseMap(mongodb.getAttributes()));
				attr.put("accountId", mongodb.getAccountId());
				attr.put("loginType", mongodb.getLoginType());
				attr.put("snsType", mongodb.getSnsType());
				attr.put("appId", mongodb.getAppId());
				attr.put("clientId", mongodb.getClientId());
			});
		;


		// account
		if (mongodb.getAccessToken() != null) {
			AccountAuthorizationMongodb.AccessToken accessToken = mongodb.getAccessToken();
			OAuthAccountAccessToken token = new OAuthAccountAccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				mongodb.getAccountId(),
				mongodb.getTokenId(),
				accessToken.getTokenValue(),
				accessToken.getIssuedAt(),
				accessToken.getExpiresAt(),
				accessToken.getScopes()
			);
			builder.token(token, metadata -> metadata.putAll(parseMap(accessToken.getMetadata())));
			// 覆盖 attribute 属性
			builder.token(token, metadata -> metadata.put(INVALIDATED_METADATA_NAME, AccountAuthorizationStatus.isInvalidated(mongodb.getStatus())));
		}

		if (mongodb.getRefreshToken() != null) {
			AccountAuthorizationMongodb.RefreshToken refreshToken = mongodb.getRefreshToken();
			OAuthAccountRefreshToken token = new OAuthAccountRefreshToken(
				refreshToken.getTokenValue(),
				refreshToken.getIssuedAt(),
				refreshToken.getExpiresAt()
			);
			builder.token(token, metadata -> metadata.putAll(parseMap(refreshToken.getMetadata())));
			// 覆盖 attribute 属性
			builder.token(token, metadata -> metadata.put(INVALIDATED_METADATA_NAME, AccountAuthorizationStatus.isInvalidated(mongodb.getStatus())));
		}


		return builder.build();
	}


}
