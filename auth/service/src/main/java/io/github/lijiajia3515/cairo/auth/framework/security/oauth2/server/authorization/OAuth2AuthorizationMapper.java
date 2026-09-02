package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.OAuth2AuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserRefreshToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserRefreshToken;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Function;

public class OAuth2AuthorizationMapper extends AbstractSecurityMapper implements Function<OAuth2AuthorizationMongodb, OAuth2Authorization> {
	private final RegisteredClientRepository registeredClientRepository;

	public OAuth2AuthorizationMapper(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public OAuth2Authorization apply(OAuth2AuthorizationMongodb oAuth2AuthorizationMongodb) {
		String registeredClientId = oAuth2AuthorizationMongodb.getRegisteredClientId();
		RegisteredClient registeredClient = registeredClientRepository.findById(registeredClientId);
		if (registeredClient == null) {
			throw new DataRetrievalFailureException("The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
		}
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
			.id(oAuth2AuthorizationMongodb.getId())
			.principalName(oAuth2AuthorizationMongodb.getPrincipalName())
			.authorizationGrantType(new AuthorizationGrantType(oAuth2AuthorizationMongodb.getAuthorizationGrantType()))
			.authorizedScopes(oAuth2AuthorizationMongodb.getAuthorizedScopes())
			.attributes(attr -> {
				attr.putAll(parseMap(oAuth2AuthorizationMongodb.getAttributes()));
				String state = oAuth2AuthorizationMongodb.getState();
				if (StringUtils.hasText(state)) {
					attr.put(OAuth2ParameterNames.STATE, state);
				}
			});

		// authorization code
		if (oAuth2AuthorizationMongodb.getAuthorizationCode() != null) {
			OAuth2AuthorizationMongodb.Token token = oAuth2AuthorizationMongodb.getAuthorizationCode();
			OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt());
			builder.token(authorizationCode, metadata -> metadata.putAll(parseMap(token.getMetadata())));
		}

		// id
		if (oAuth2AuthorizationMongodb.getIdToken() != null) {
			OAuth2AuthorizationMongodb.Token code = oAuth2AuthorizationMongodb.getIdToken();
			Map<String, Object> metadataMap = parseMap(code.getMetadata());
			OidcIdToken oidcIdToken = new OidcIdToken(code.getTokenValue(), code.getIssuedAt(), code.getExpiresAt(), (Map<String, Object>) metadataMap.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME));
			builder.token(oidcIdToken, metadata -> metadata.putAll(parseMap(code.getMetadata())));
		}

		// default
		if (oAuth2AuthorizationMongodb.getAccessToken() != null) {
			OAuth2AuthorizationMongodb.AccessToken accessToken = oAuth2AuthorizationMongodb.getAccessToken();
			OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessToken.getTokenValue(), accessToken.getIssuedAt(), accessToken.getExpiresAt(), accessToken.getScopes());
			builder.token(token, metadata -> metadata.putAll(parseMap(accessToken.getMetadata())));
		}

		if (oAuth2AuthorizationMongodb.getRefreshToken() != null) {
			OAuth2AuthorizationMongodb.Token code = oAuth2AuthorizationMongodb.getRefreshToken();
			OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(code.getTokenValue(), code.getIssuedAt(), code.getExpiresAt());
			builder.token(refreshToken, metadata -> metadata.putAll(parseMap(code.getMetadata())));
		}

		return builder.build();
	}


}
