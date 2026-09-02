package io.github.lijiajia3515.cairo.auth.modules.client;

import io.github.lijiajia3515.cairo.auth.domain.dto.client.AppUserMetadataClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.MetadataClient;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings.CairoSettingNames;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientConverter {

	public static MetadataClient convertMetadataClient(ClientMongodb client, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, AppUser> metadataUserMap) {
		return MetadataClient.builder()
			.id(client.getId())
			.appId(client.getAppId())
			.appName(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getAppName).orElse(client.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(client.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getEndpointName).orElse(client.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(client.getClientId())
			.clientName(client.getClientName())
			.authorizationGrantTypes(client.getAuthorizationGrantTypes())
			.clientAuthenticationMethods(client.getClientAuthenticationMethods())
			.scopes(Optional.ofNullable(client.getScopes()).orElse(Collections.emptyList()))
			.redirectUris(Optional.ofNullable(client.getRedirectUris()).orElse(Collections.emptyList()))
			.enabled(client.getEnabled())
			.authenticationTypes(Optional.ofNullable(client.getAuthenticationTypes()).orElse(Collections.emptyList()))
			.accountSnsProviderIds(Optional.ofNullable(client.getAccountSnsProviderIds()).orElse(Collections.emptyList()))
			.clientSettings(Optional.ofNullable(client.getClientSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder()
					.requireProofKey(settings.getRequireProofKey())
					.requireAuthorizationConsent(settings.getRequireAuthorizationConsent())
					.jwkSetUrl(settings.getJwkSetUrl())
					.tokenEndpointAuthenticationSigningAlgorithm(settings.getTokenEndpointAuthenticationSigningAlgorithm())
					.build())
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder().build())
			)
			.tokenSettings(Optional.ofNullable(client.getTokenSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder()
					.idTokenSignatureAlgorithm(settings.getIdTokenSignatureAlgorithm())
					.idTokenFormat(settings.getIdTokenFormat())
					.idTokenTimeToLive(settings.getIdTokenTimeToLive())

					.accessTokenFormat(settings.getAccessTokenFormat())
					.accessTokenTimeToLive(settings.getAccessTokenTimeToLive())
					.refreshTokenTimeToLive(settings.getRefreshTokenTimeToLive())
					.reuseRefreshTokens(settings.getReuseRefreshTokens())

					.accountAccessTokenFormat(settings.getAccountAccessTokenFormat())
					.accountAccessTokenTimeToLive(settings.getAccountAccessTokenTimeToLive())
					.accountRefreshTokenTimeToLive(settings.getAccountRefreshTokenTimeToLive())
					.reuseAccountRefreshTokens(settings.getReuseAccountRefreshTokens())


					.appUserAccessTokenFormat(settings.getAppUserAccessTokenFormat())
					.appUserAccessTokenTimeToLive(settings.getAppUserAccessTokenTimeToLive())
					.appUserRefreshTokenTimeToLive(settings.getAppUserRefreshTokenTimeToLive())
					.reuseAppUserRefreshTokens(settings.getReuseAppUserRefreshTokens())


					.tenantAppUserAccessTokenFormat(settings.getTenantAppUserAccessTokenFormat())
					.tenantAppUserAccessTokenTimeToLive(settings.getTenantAppUserAccessTokenTimeToLive())
					.tenantAppUserRefreshTokenTimeToLive(settings.getTenantAppUserRefreshTokenTimeToLive())
					.reuseTenantAppUserRefreshTokens(settings.getReuseTenantAppUserRefreshTokens())
					.build()
				)
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder().build())
			)
			.metadata(CairoAppUserConverter.convertAppUser(client.getMetadata(), metadataUserMap))
			.build();
	}

	public static AppUserMetadataClient convertAppUserMetadataClient(ClientMongodb client, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, AppUser> metadataUserMap) {
		return AppUserMetadataClient.builder()
			.id(client.getId())
			.appId(client.getAppId())
			.appName(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getAppName).orElse(client.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(client.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getEndpointName).orElse(client.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(client.getClientId())
			.clientName(client.getClientName())
			.authorizationGrantTypes(client.getAuthorizationGrantTypes())
			.clientAuthenticationMethods(client.getClientAuthenticationMethods())
			.scopes(Optional.ofNullable(client.getScopes()).orElse(Collections.emptyList()))
			.redirectUris(Optional.ofNullable(client.getRedirectUris()).orElse(Collections.emptyList()))
			.enabled(client.getEnabled())
			.authenticationTypes(Optional.ofNullable(client.getAuthenticationTypes()).orElse(Collections.emptyList()))
			.accountSnsProviderIds(Optional.ofNullable(client.getAccountSnsProviderIds()).orElse(Collections.emptyList()))
			.clientSettings(Optional.ofNullable(client.getClientSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder()
					.requireProofKey(settings.getRequireProofKey())
					.requireAuthorizationConsent(settings.getRequireAuthorizationConsent())
					.jwkSetUrl(settings.getJwkSetUrl())
					.tokenEndpointAuthenticationSigningAlgorithm(settings.getTokenEndpointAuthenticationSigningAlgorithm())
					.build())
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder().build())
			)
			.tokenSettings(Optional.ofNullable(client.getTokenSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder()
					.idTokenSignatureAlgorithm(settings.getIdTokenSignatureAlgorithm())
					.idTokenFormat(settings.getIdTokenFormat())
					.idTokenTimeToLive(settings.getIdTokenTimeToLive())

					.accessTokenFormat(settings.getAccessTokenFormat())
					.accessTokenTimeToLive(settings.getAccessTokenTimeToLive())
					.refreshTokenTimeToLive(settings.getRefreshTokenTimeToLive())
					.reuseRefreshTokens(settings.getReuseRefreshTokens())

					.accountAccessTokenFormat(settings.getAccountAccessTokenFormat())
					.accountAccessTokenTimeToLive(settings.getAccountAccessTokenTimeToLive())
					.accountRefreshTokenTimeToLive(settings.getAccountRefreshTokenTimeToLive())
					.reuseAccountRefreshTokens(settings.getReuseAccountRefreshTokens())


					.appUserAccessTokenFormat(settings.getAppUserAccessTokenFormat())
					.appUserAccessTokenTimeToLive(settings.getAppUserAccessTokenTimeToLive())
					.appUserRefreshTokenTimeToLive(settings.getAppUserRefreshTokenTimeToLive())
					.reuseAppUserRefreshTokens(settings.getReuseAppUserRefreshTokens())


					.tenantAppUserAccessTokenFormat(settings.getTenantAppUserAccessTokenFormat())
					.tenantAppUserAccessTokenTimeToLive(settings.getTenantAppUserAccessTokenTimeToLive())
					.tenantAppUserRefreshTokenTimeToLive(settings.getTenantAppUserRefreshTokenTimeToLive())
					.reuseTenantAppUserRefreshTokens(settings.getReuseTenantAppUserRefreshTokens())

					.build()
				)
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder().build())
			)
			.metadata(CairoAppUserConverter.convertAppUser(client.getMetadata(), metadataUserMap))
			.build();
	}


	public static CairoRegisteredClient convert(ClientMongodb client) {
		return CairoRegisteredClient.cairoWithId(client.getId())
			.appId(client.getAppId())
			.endpointId(client.getEndpointId())
			.clientId(client.getClientId())
			.clientSecret(client.getClientSecret())
			.clientAuthenticationMethods(clientAuthenticationMethods ->
				Optional.ofNullable(client.getClientAuthenticationMethods())
					.ifPresent(methods ->
						clientAuthenticationMethods.addAll(
							methods.stream()
								.map(ClientAuthenticationMethod::new)
								.collect(Collectors.toSet())
						)
					)
			)

			.authorizationGrantTypes(authorizationGrantTypes ->
				Optional.ofNullable(client.getAuthorizationGrantTypes())
					.ifPresent(types ->
						authorizationGrantTypes.addAll(
							types.stream()
								.map(AuthorizationGrantType::new)
								.collect(Collectors.toSet())
						)
					)
			)
			.scopes(scopes -> Optional.ofNullable(client.getScopes()).ifPresent(scopes::addAll))
			.redirectUris(redirectUris -> Optional.ofNullable(client.getRedirectUris()).ifPresent(redirectUris::addAll))
			.clientSettings(
				Optional.ofNullable(client.getClientSettings())
					.map(x -> {
						final ClientSettings.Builder builder = ClientSettings.builder();
						Optional.ofNullable(x.getRequireProofKey()).ifPresent(builder::requireProofKey);
						Optional.ofNullable(x.getRequireAuthorizationConsent()).ifPresent(builder::requireAuthorizationConsent);
						Optional.ofNullable(x.getTokenEndpointAuthenticationSigningAlgorithm()).ifPresent(z -> builder.tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.from(z)));
						Optional.ofNullable(x.getJwkSetUrl()).ifPresent(builder::jwkSetUrl);

						return builder.build();
					}).orElse(ClientSettings.builder().build())
			)
			.tokenSettings(Optional.ofNullable(client.getTokenSettings())
				.map(x -> {
					final TokenSettings.Builder builder = TokenSettings.builder();
					Optional.ofNullable(x.getIdTokenSignatureAlgorithm()).map(SignatureAlgorithm::from).ifPresent(builder::idTokenSignatureAlgorithm);
					Optional.ofNullable(x.getIdTokenFormat()).map(OAuth2TokenFormat::new).ifPresent(g -> builder.setting(CairoSettingNames.Token.ID_TOKEN_FORMAT, g));
					Optional.ofNullable(x.getIdTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.ID_TOKEN_TIME_TO_LIVE, g));

					Optional.ofNullable(x.getAccessTokenFormat()).map(OAuth2TokenFormat::new).ifPresent(builder::accessTokenFormat);
					Optional.ofNullable(x.getAccessTokenTimeToLive()).ifPresent(builder::accessTokenTimeToLive);

					Optional.ofNullable(x.getRefreshTokenTimeToLive()).ifPresent(builder::refreshTokenTimeToLive);
					Optional.ofNullable(x.getReuseRefreshTokens()).ifPresent(builder::reuseRefreshTokens);

					Optional.ofNullable(x.getAccountAccessTokenFormat()).map(OAuth2TokenFormat::new).ifPresent(g -> builder.setting(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_FORMAT, g));
					Optional.ofNullable(x.getAccountAccessTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getAccountRefreshTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getReuseAccountRefreshTokens()).ifPresent(g -> builder.setting(CairoSettingNames.Token.REUSE_ACCOUNT_REFRESH_TOKENS, g));

					Optional.ofNullable(x.getAppUserAccessTokenFormat()).map(OAuth2TokenFormat::new).ifPresent(g -> builder.setting(CairoSettingNames.Token.APP_USER_ACCESS_TOKEN_FORMAT, g));
					Optional.ofNullable(x.getAppUserAccessTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getAppUserRefreshTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.APP_USER_REFRESH_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getReuseAppUserRefreshTokens()).ifPresent(g -> builder.setting(CairoSettingNames.Token.REUSE_APP_USER_REFRESH_TOKENS, g));

					Optional.ofNullable(x.getTenantAppUserAccessTokenFormat()).map(OAuth2TokenFormat::new).ifPresent(g -> builder.setting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT, g));
					Optional.ofNullable(x.getTenantAppUserAccessTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getTenantAppUserRefreshTokenTimeToLive()).ifPresent(g -> builder.setting(CairoSettingNames.Token.TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE, g));
					Optional.ofNullable(x.getReuseTenantAppUserRefreshTokens()).ifPresent(g -> builder.setting(CairoSettingNames.Token.REUSE_TENANT_APP_USER_REFRESH_TOKENS, g));

					return builder.build();
				})
				.orElse(TokenSettings.builder().build())
			)
			.build();
	}


	public static Client convertClient(ClientMongodb client, Map<String, App> appMap, Map<String, Endpoint> endpointMap) {
		return Client.builder()
			.id(client.getId())
			.appId(client.getAppId())
			.appName(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getAppName).orElse(client.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(client.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(client.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getEndpointName).orElse(client.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(client.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(client.getClientId())
			.clientName(client.getClientName())
			.authorizationGrantTypes(client.getAuthorizationGrantTypes())
			.clientAuthenticationMethods(client.getClientAuthenticationMethods())
			.scopes(Optional.ofNullable(client.getScopes()).orElse(Collections.emptyList()))
			.redirectUris(Optional.ofNullable(client.getRedirectUris()).orElse(Collections.emptyList()))
			.enabled(client.getEnabled())
			.authenticationTypes(Optional.ofNullable(client.getAuthenticationTypes()).orElse(Collections.emptyList()))
			.accountSnsProviderIds(Optional.ofNullable(client.getAccountSnsProviderIds()).orElse(Collections.emptyList()))
			.clientSettings(Optional.ofNullable(client.getClientSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder()
					.requireProofKey(settings.getRequireProofKey())
					.requireAuthorizationConsent(settings.getRequireAuthorizationConsent())
					.jwkSetUrl(settings.getJwkSetUrl())
					.tokenEndpointAuthenticationSigningAlgorithm(settings.getTokenEndpointAuthenticationSigningAlgorithm())
					.build())
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.ClientSettings.builder().build())
			)
			.tokenSettings(Optional.ofNullable(client.getTokenSettings())
				.map(settings -> io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder()
					.idTokenSignatureAlgorithm(settings.getIdTokenSignatureAlgorithm())
					.idTokenFormat(settings.getIdTokenFormat())
					.idTokenTimeToLive(settings.getIdTokenTimeToLive())

					.accessTokenFormat(settings.getAccessTokenFormat())
					.accessTokenTimeToLive(settings.getAccessTokenTimeToLive())
					.refreshTokenTimeToLive(settings.getRefreshTokenTimeToLive())
					.reuseRefreshTokens(settings.getReuseRefreshTokens())

					.accountAccessTokenFormat(settings.getAccountAccessTokenFormat())
					.accountAccessTokenTimeToLive(settings.getAccountAccessTokenTimeToLive())
					.accountRefreshTokenTimeToLive(settings.getAccountRefreshTokenTimeToLive())
					.reuseAccountRefreshTokens(settings.getReuseAccountRefreshTokens())


					.appUserAccessTokenFormat(settings.getAppUserAccessTokenFormat())
					.appUserAccessTokenTimeToLive(settings.getAppUserAccessTokenTimeToLive())
					.appUserRefreshTokenTimeToLive(settings.getAppUserRefreshTokenTimeToLive())
					.reuseAppUserRefreshTokens(settings.getReuseAppUserRefreshTokens())


					.tenantAppUserAccessTokenFormat(settings.getTenantAppUserAccessTokenFormat())
					.tenantAppUserAccessTokenTimeToLive(settings.getTenantAppUserAccessTokenTimeToLive())
					.tenantAppUserRefreshTokenTimeToLive(settings.getTenantAppUserRefreshTokenTimeToLive())
					.reuseTenantAppUserRefreshTokens(settings.getReuseTenantAppUserRefreshTokens())
					.build()
				)
				.orElse(io.github.lijiajia3515.cairo.auth.domain.dto.client.TokenSettings.builder().build())
			)
			.build();
	}

	public static ClientMongodb convert(CairoRegisteredClient client) {
		return ClientMongodb.builder()
			.id(client.getId())
			.appId(client.getAppId())
			.endpointId(client.getEndpointId())
			.clientId(client.getClientId())
			.clientSecret(client.getClientSecret())
			.clientAuthenticationMethods(
				Optional.ofNullable(client.getClientAuthenticationMethods()).orElse(Collections.emptySet())
					.parallelStream()
					.map(ClientAuthenticationMethod::getValue)
					.sorted()
					.collect(Collectors.toList())
			)
			.authorizationGrantTypes(
				Optional.ofNullable(client.getAuthorizationGrantTypes()).orElse(Collections.emptySet())
					.stream()
					.map(AuthorizationGrantType::getValue)
					.sorted()
					.collect(Collectors.toList())
			)
			.scopes(Optional.ofNullable(client.getScopes()).orElse(Collections.emptySet()).stream().distinct().collect(Collectors.toList()))
			.redirectUris(Optional.ofNullable(client.getRedirectUris()).orElse(Collections.emptySet()).stream().distinct().collect(Collectors.toList()))
			.clientSettings(
				Optional.ofNullable(client.getClientSettings())
					.map(x ->
						ClientMongodb.ClientSettings.builder()
							.requireProofKey(x.isRequireProofKey())
							.requireAuthorizationConsent(x.isRequireAuthorizationConsent())
							.build()
					).orElse(new ClientMongodb.ClientSettings())
			)
			.tokenSettings(
				Optional.ofNullable(client.getTokenSettings())
					.map(x ->
						ClientMongodb.TokenSettings.builder()
							.idTokenSignatureAlgorithm(x.getIdTokenSignatureAlgorithm().getName())
							.idTokenFormat(x.getSetting(CairoSettingNames.Token.ID_TOKEN_FORMAT))
							.idTokenTimeToLive(x.getSetting(CairoSettingNames.Token.ID_TOKEN_TIME_TO_LIVE))

							.accessTokenFormat(Optional.ofNullable(x.getAccessTokenFormat()).map(OAuth2TokenFormat::getValue).orElse(null))
							.accessTokenTimeToLive(x.getAccessTokenTimeToLive())

							.refreshTokenTimeToLive(x.getRefreshTokenTimeToLive())
							.reuseRefreshTokens(x.isReuseRefreshTokens())

							.accountAccessTokenFormat(Optional.ofNullable((OAuth2TokenFormat) x.getSetting(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_FORMAT)).map(OAuth2TokenFormat::getValue).orElse(null))
							.accountAccessTokenTimeToLive(x.getSetting(CairoSettingNames.Token.ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE))
							.accountRefreshTokenTimeToLive(x.getSetting(x.getSetting(CairoSettingNames.Token.ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE)))
							.reuseAccountRefreshTokens(x.getSetting(x.getSetting(CairoSettingNames.Token.ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE)))

							.appUserAccessTokenFormat(Optional.ofNullable((OAuth2TokenFormat) x.getSetting(CairoSettingNames.Token.APP_USER_ACCESS_TOKEN_FORMAT)).map(OAuth2TokenFormat::getValue).orElse(null))
							.appUserAccessTokenTimeToLive(x.getSetting(CairoSettingNames.Token.APP_USER_ACCESS_TOKEN_TIME_TO_LIVE))
							.appUserRefreshTokenTimeToLive(x.getSetting(x.getSetting(CairoSettingNames.Token.APP_USER_REFRESH_TOKEN_TIME_TO_LIVE)))
							.reuseAppUserRefreshTokens(x.getSetting(x.getSetting(CairoSettingNames.Token.APP_USER_REFRESH_TOKEN_TIME_TO_LIVE)))

							.tenantAppUserAccessTokenFormat(Optional.ofNullable((OAuth2TokenFormat) x.getSetting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_FORMAT)).map(OAuth2TokenFormat::getValue).orElse(null))
							.tenantAppUserAccessTokenTimeToLive(x.getSetting(CairoSettingNames.Token.TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE))
							.tenantAppUserRefreshTokenTimeToLive(x.getSetting(x.getSetting(CairoSettingNames.Token.TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE)))
							.reuseTenantAppUserRefreshTokens(x.getSetting(x.getSetting(CairoSettingNames.Token.TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE)))


							.build()
					).orElse(new ClientMongodb.TokenSettings())
			)
			.build();
	}

	public static BasicClient convertBasicClient(ClientMongodb clientMongodb) {
		return BasicClient.builder()
			.id(clientMongodb.getClientId())
			.clientId(clientMongodb.getClientId())
			.clientName(clientMongodb.getClientName())
			.build();
	}
}
