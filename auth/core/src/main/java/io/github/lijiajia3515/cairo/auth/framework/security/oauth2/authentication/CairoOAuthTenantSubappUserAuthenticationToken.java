package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication;


import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAccessToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * oauth tenant app subapp user authentication token
 */
public class CairoOAuthTenantSubappUserAuthenticationToken extends AbstractOAuth2TokenAuthenticationToken<OAuthTenantAppUserAccessToken> {

    private final CairoOAuthTenantSubappUserPrincipal principal;


    /**
     * 创建已认证的token
     * @param token token
     * @param principal principal
     * @param authorities authorities
     */
    public CairoOAuthTenantSubappUserAuthenticationToken(OAuthTenantAppUserAccessToken token, CairoOAuthTenantSubappUserPrincipal principal, Collection<? extends GrantedAuthority> authorities) {
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
    public CairoOAuthTenantSubappUserPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getId();
    }
}
