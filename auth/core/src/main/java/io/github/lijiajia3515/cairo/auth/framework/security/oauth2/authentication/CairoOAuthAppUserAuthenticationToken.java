package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAppUserAccessToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * oauth app user authentication token
 */
public class CairoOAuthAppUserAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<OAuthAppUserAccessToken> {

    private final CairoOAuthAppUserPrincipal principal;


    /**
     * 创建已认证的token
     * @param token token
     * @param principal principal
     * @param authorities authorities
     */
    public CairoOAuthAppUserAuthenticationToken(OAuthAppUserAccessToken token, CairoOAuthAppUserPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
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
    public CairoOAuthAppUserPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getId();
    }
}
