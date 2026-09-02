package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.OAuth2AuthorizationMongodb;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.function.Function;

public class OAuth2AuthorizationMongodbMapper extends AbstractSecurityMapper implements Function<OAuth2Authorization, OAuth2AuthorizationMongodb> {
    @Override
    public OAuth2AuthorizationMongodb apply(OAuth2Authorization oAuth2Authorization) {
        OAuth2AuthorizationMongodb.OAuth2AuthorizationMongodbBuilder builder = OAuth2AuthorizationMongodb.builder();
        builder.id(oAuth2Authorization.getId())
                .registeredClientId(oAuth2Authorization.getRegisteredClientId())
                .principalName(oAuth2Authorization.getPrincipalName())
                .authorizationGrantType(oAuth2Authorization.getAuthorizationGrantType().getValue())
                .authorizedScopes(oAuth2Authorization.getAuthorizedScopes())
                .attributes(writeMap(oAuth2Authorization.getAttributes()))
                .state(Optional.ofNullable(oAuth2Authorization.<String>getAttribute(OAuth2ParameterNames.STATE))
                        .filter(StringUtils::hasText)
                        .orElse(null)
                );

		// authorization code
        Optional.ofNullable(oAuth2Authorization.getToken(OAuth2AuthorizationCode.class))
                .map(this::getToken)
                .ifPresent(builder::authorizationCode);

		// id token
        Optional.ofNullable(oAuth2Authorization.getToken(OidcIdToken.class))
                .map(this::getIdToken)
                .ifPresent(builder::idToken);

		// default
        Optional.ofNullable(oAuth2Authorization.getToken(OAuth2AccessToken.class))
                .map(this::getAccessToken)
                .ifPresent(builder::accessToken);
        Optional.ofNullable(oAuth2Authorization.getToken(OAuth2RefreshToken.class))
                .map(this::getRefreshToken)
                .ifPresent(builder::refreshToken);

		return builder.build();
    }

    public <T extends AbstractOAuth2Token> OAuth2AuthorizationMongodb.Token getToken(OAuth2Authorization.Token<T> token) {
        OAuth2AuthorizationMongodb.Token.TokenBuilder<?, ?> builder = OAuth2AuthorizationMongodb.Token.builder();
        if (token != null) {
            builder.tokenValue(token.getToken().getTokenValue())
                    .issuedAt(token.getToken().getIssuedAt())
                    .expiresAt(token.getToken().getExpiresAt())
                    .metadata(writeMap(token.getMetadata()));

        }
        return builder.build();
    }

    public <T extends AbstractOAuth2Token> OAuth2AuthorizationMongodb.IdToken getIdToken(OAuth2Authorization.Token<OidcIdToken> token) {
        OAuth2AuthorizationMongodb.IdToken.IdTokenBuilder<?, ?> builder = OAuth2AuthorizationMongodb.IdToken.builder();
        if (token != null) {
            builder.tokenValue(token.getToken().getTokenValue())
                    .issuedAt(token.getToken().getIssuedAt())
                    .expiresAt(token.getToken().getExpiresAt())
                    .metadata(writeMap(token.getMetadata()));

        }
        return builder.build();
    }

    public <T extends AbstractOAuth2Token> OAuth2AuthorizationMongodb.AccessToken getAccessToken(OAuth2Authorization.Token<OAuth2AccessToken> token) {
        OAuth2AuthorizationMongodb.AccessToken.AccessTokenBuilder<?, ?> builder = OAuth2AuthorizationMongodb.AccessToken.builder();
        if (token != null) {
            builder.tokenValue(token.getToken().getTokenValue())
                    .issuedAt(token.getToken().getIssuedAt())
                    .expiresAt(token.getToken().getExpiresAt())
                    .metadata(writeMap(token.getMetadata()))
                    .type(token.getToken().getTokenType().getValue())
                    .scopes(token.getToken().getScopes())
            ;

        }
        return builder.build();
    }

    public <T extends AbstractOAuth2Token> OAuth2AuthorizationMongodb.RefreshToken getRefreshToken(OAuth2Authorization.Token<OAuth2RefreshToken> token) {
        OAuth2AuthorizationMongodb.RefreshToken.RefreshTokenBuilder<?, ?> builder = OAuth2AuthorizationMongodb.RefreshToken.builder();
        if (token != null) {
            builder.tokenValue(token.getToken().getTokenValue())
                    .issuedAt(token.getToken().getIssuedAt())
                    .expiresAt(token.getToken().getExpiresAt())
                    .metadata(writeMap(token.getMetadata()));

        }
        return builder.build();
    }

}
