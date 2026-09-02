package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.AbstractSecurityMapper;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationStatus;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.function.Function;

import static org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token.INVALIDATED_METADATA_NAME;

public class TenantAppUserAuthorizationMapper extends AbstractSecurityMapper implements Function<TenantAppUserAuthorizationMongodb, OAuth2Authorization> {
	private final RegisteredClientRepository registeredClientRepository;

	public TenantAppUserAuthorizationMapper(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public OAuth2Authorization apply(TenantAppUserAuthorizationMongodb mongodb) {
		String registeredClientId = mongodb.getRegisteredClientId();
		RegisteredClient registeredClient = registeredClientRepository.findById(registeredClientId);
		if (registeredClient == null) {
			throw new DataRetrievalFailureException("The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
		}
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
			.id(mongodb.getTokenId())
			.principalName(mongodb.getUserName())
			.authorizationGrantType(new AuthorizationGrantType(mongodb.getAuthorizationGrantType()))
			.authorizedScopes(mongodb.getAuthorizedScopes())
			.attributes(attr -> {
				attr.putAll(parseMap(mongodb.getAttributes()));
				attr.put("tenantId", mongodb.getTenantId());
				attr.put("appId", mongodb.getAppId());
				attr.put("endpointId", mongodb.getEndpointId());
				attr.put("userId", mongodb.getUserId());
				attr.put("loginType", mongodb.getLoginType());
				attr.put("snsType", mongodb.getSnsType());
				attr.put("clientId", mongodb.getClientId());
			});
		;


		// tenant app endpoint user
		if (mongodb.getAccessToken() != null) {
			TenantAppUserAuthorizationMongodb.AccessToken accessToken = mongodb.getAccessToken();
			OAuthTenantAppUserAccessToken token = new OAuthTenantAppUserAccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				mongodb.getTenantId(),
				mongodb.getAppId(),
				mongodb.getEndpointId(),
				mongodb.getUserId(),
				mongodb.getTokenId(),
				accessToken.getTokenValue(),
				accessToken.getIssuedAt(),
				accessToken.getExpiresAt(),
				accessToken.getScopes()
			);
			builder.token(token, metadata -> metadata.putAll(parseMap(accessToken.getMetadata())));
			// 覆盖 attribute 属性
			builder.token(token, metadata -> metadata.put(INVALIDATED_METADATA_NAME, TenantAppUserAuthorizationStatus.isInvalidated(mongodb.getStatus())));
		}

		if (mongodb.getRefreshToken() != null) {
			TenantAppUserAuthorizationMongodb.RefreshToken refreshToken = mongodb.getRefreshToken();
			OAuthTenantAppUserRefreshToken token = new OAuthTenantAppUserRefreshToken(
				refreshToken.getTokenValue(),
				refreshToken.getIssuedAt(),
				refreshToken.getExpiresAt()
			);
			builder.token(token, metadata -> metadata.putAll(parseMap(refreshToken.getMetadata())));
			// 覆盖 attribute 属性
			builder.token(token, metadata -> metadata.put(INVALIDATED_METADATA_NAME, TenantAppUserAuthorizationStatus.isInvalidated(mongodb.getStatus())));
		}


		return builder.build();
	}


}
