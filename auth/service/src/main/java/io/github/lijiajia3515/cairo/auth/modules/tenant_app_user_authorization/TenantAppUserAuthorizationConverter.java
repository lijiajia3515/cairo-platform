package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_authorization.TenantAppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;


/**
 * TenantAppUserAuthorization converter
 */
public class TenantAppUserAuthorizationConverter {


	public static TenantAppUserAuthorization convertTenantAppUserAuthorization(Map<String, App> appMap,
																					Map<String, Endpoint> endpointMap,
																					Map<String, BasicClient> clientMap,
																					TenantAppUserAuthorizationMongodb m) {
		return TenantAppUserAuthorization.builder()
			.tokenId(m.getTokenId())
			.tenantId(m.getTenantId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.userId(m.getUserId())
			.userName(m.getUserName())
			.loginType(m.getLoginType())
			.snsType(m.getSnsType())
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.registeredClientId(m.getRegisteredClientId())
			.authorizationGrantType(m.getAuthorizationGrantType())
			.authorizedScopes(m.getAuthorizedScopes())
			.accessTokenType(Optional.ofNullable(m.getAccessToken()).map(TenantAppUserAuthorizationMongodb.AccessToken::getTokenType).orElse(null))
			.accessTokenScopes(Optional.ofNullable(m.getAccessToken()).map(TenantAppUserAuthorizationMongodb.AccessToken::getScopes).orElse(null))
			.accessTokenValue(Optional.ofNullable(m.getAccessToken()).map(TenantAppUserAuthorizationMongodb.AccessToken::getTokenValue).orElse(null))
			.accessTokenIssuedAt(Optional.ofNullable(m.getAccessToken()).map(TenantAppUserAuthorizationMongodb.AccessToken::getIssuedAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime())
			.accessTokenExpiresAt(Optional.ofNullable(m.getAccessToken()).map(TenantAppUserAuthorizationMongodb.AccessToken::getExpiresAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime())
			.refreshTokenValue(Optional.ofNullable(m.getRefreshToken()).map(TenantAppUserAuthorizationMongodb.RefreshToken::getTokenValue).orElse(null))
			.refreshTokenIssuedAt((Optional.ofNullable(m.getRefreshToken()).map(TenantAppUserAuthorizationMongodb.RefreshToken::getIssuedAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
			.refreshTokenExpiresAt((Optional.ofNullable(m.getRefreshToken()).map(TenantAppUserAuthorizationMongodb.RefreshToken::getExpiresAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
			.attributes(m.getAttributes())
			.status(m.getStatus())
			.deviceId(m.getDeviceId())
			.deviceTime(m.getDeviceTime())
			.ip(m.getIp())
			.region(m.getRegion())
			.agent(m.getAgent())
			.os(m.getOs())
			.platform(m.getPlatform())
			.engine(m.getEngine())
			.app(m.getApp())
			.loginTime(m.getLoginTime())
			.logoutTime(m.getLogoutTime())
			.onlineDuration(Optional.ofNullable(m.getLogoutTime()).orElse(LocalDateTime.now()).toEpochSecond(ZoneOffset.ofHours(8))-m.getLoginTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.build();
	}


}
