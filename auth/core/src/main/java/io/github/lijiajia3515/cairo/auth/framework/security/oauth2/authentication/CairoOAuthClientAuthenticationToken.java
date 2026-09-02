package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * oauth client authentication token
 */
public class CairoOAuthClientAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<OAuth2AccessToken> {
    private final CairoOAuthClientPrincipal principal;

    /**
     * 创建已认证的token
     *
     * @param token       oauth token
     * @param principal   principal
     * @param authorities authorities
     */
    public CairoOAuthClientAuthenticationToken(OAuth2AccessToken token, CairoOAuthClientPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
        super(token, authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public CairoOAuthClientPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getId();
    }
}
