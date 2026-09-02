package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.resource.authentication;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientAuthenticationToken;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.CairoOAuthParameterNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.AuthenticationType.CLIENT;

@Slf4j
public class ClientJwtAuthenticationConverter implements CairoJwtAuthenticationConverter {
    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;

    public ClientJwtAuthenticationConverter() {
        grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String type = jwt.getClaimAsString(CairoOAuthParameterNames.AUTH_TYPE);
        if (!CLIENT.getValue().equals(type)) {
            return null;
        }

        List<String> scopeValues = jwt.getClaimAsStringList(OAuth2ParameterNames.SCOPE);
        Set<String> scopes = Optional.ofNullable(scopeValues).<Set<String>>map(HashSet::new).orElse(Collections.emptySet());
        String loginTypeValue = jwt.getClaimAsString(CairoOAuthParameterNames.LOGIN_TYPE);
        String appId = jwt.getClaimAsString(CairoOAuthParameterNames.APP_ID);
        String clientId = jwt.getClaimAsString(OAuth2ParameterNames.CLIENT_ID);
        String clientTokenId = jwt.getSubject();

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                jwt.getTokenValue(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                scopes
        );

        Collection<GrantedAuthority> tokenAuthorities = grantedAuthoritiesConverter.convert(jwt);

        CairoOAuthClientPrincipal principal = CairoOAuthClientPrincipal.builder()
                .id(clientTokenId)
                .loginType(loginTypeValue)
                .appId(appId)
                .clientId(clientId)
                .build();

        return new CairoOAuthClientAuthenticationToken(token,principal, tokenAuthorities);
    }

}
