package io.github.lijiajia3515.cairo.auth.modules.account_authorization;

import io.github.lijiajia3515.cairo.auth.domain.dto.account_authorization.AccountAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;


/**
 * AccountAuthorization converter
 */
public class AccountAuthorizationConverter {


	public static AccountAuthorization convertAccountAuthorization(Map<String, BasicClient> clientMap, AccountAuthorizationMongodb m) {
		return AccountAuthorization.builder()
			.tokenId(m.getTokenId())
			.appId(m.getAppId())
			.accountId(m.getAccountId())
			.accountName(m.getAccountName())
			.loginType(m.getLoginType())
			.snsType(m.getSnsType())
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.registeredClientId(m.getRegisteredClientId())
			.authorizationGrantType(m.getAuthorizationGrantType())
			.authorizedScopes(m.getAuthorizedScopes())
			.accessTokenType(Optional.ofNullable(m.getAccessToken()).map(AccountAuthorizationMongodb.AccessToken::getTokenType).orElse(null))
			.accessTokenScopes(Optional.ofNullable(m.getAccessToken()).map(AccountAuthorizationMongodb.AccessToken::getScopes).orElse(null))
			.accessTokenValue(Optional.ofNullable(m.getAccessToken()).map(AccountAuthorizationMongodb.AccessToken::getTokenValue).orElse(null))
			.accessTokenIssuedAt(Optional.ofNullable(m.getAccessToken()).map(AccountAuthorizationMongodb.AccessToken::getIssuedAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime())
			.accessTokenExpiresAt(Optional.ofNullable(m.getAccessToken()).map(AccountAuthorizationMongodb.AccessToken::getExpiresAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime())
			.refreshTokenValue(Optional.ofNullable(m.getRefreshToken()).map(AccountAuthorizationMongodb.RefreshToken::getTokenValue).orElse(null))
			.refreshTokenIssuedAt((Optional.ofNullable(m.getRefreshToken()).map(AccountAuthorizationMongodb.RefreshToken::getIssuedAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
			.refreshTokenExpiresAt((Optional.ofNullable(m.getRefreshToken()).map(AccountAuthorizationMongodb.RefreshToken::getExpiresAt).orElse(Instant.now()).atZone(ZoneId.systemDefault()).toLocalDateTime()))
			.attributes(m.getAttributes())
			.status(m.getStatus())
			.ip(m.getIp())
			.agent(m.getAgent())
			.region(m.getRegion())
			.os(m.getOs())
			.platform(m.getPlatform())
			.engine(m.getEngine())
			.app(m.getApp())
			.loginTime(m.getLoginTime())
			.logoutTime(m.getLogoutTime())
			.onlineDuration((!AccountAuthorizationStatus.OK.getStatusValue().equals(m.getStatus()) && m.getLogoutTime() == null) ? 0 : Optional.ofNullable(m.getLogoutTime()).orElse(LocalDateTime.now()).toEpochSecond(ZoneOffset.ofHours(8)) - m.getLoginTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.build();
	}


}
