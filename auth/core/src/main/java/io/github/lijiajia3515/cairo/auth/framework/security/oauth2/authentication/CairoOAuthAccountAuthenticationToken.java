package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAccessToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * oauth account authentication token
 */
public class CairoOAuthAccountAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<OAuthAccountAccessToken> {

    private final CairoOAuthAccountPrincipal principal;


    /**
     * 创建已认证的token
     *
     * @param token       oauth token
     * @param principal   account principal
     * @param authorities authorities
     */
    public CairoOAuthAccountAuthenticationToken(OAuthAccountAccessToken token, CairoOAuthAccountPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
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
    public CairoOAuthAccountPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getId();
    }
}
