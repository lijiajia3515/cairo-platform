package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.ACCOUNT_PASSWORD;


/**
 * 终端用户密码模式 authentication token
 */
@Getter
public class OAuthTenantAppUserPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


    /**
     * 租户id
     */
    private final String tenantId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;

    /**
     * scopes
     */
    private final Set<String> scopes;

    /**
     * Sub-class constructor.
     *
     * @param username             username
     * @param password             password
     * @param clientPrincipal      the authenticated client principal
     * @param additionalParameters the additional parameters
     */
    protected OAuthTenantAppUserPasswordAuthenticationToken(String tenantId, String username, String password, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(ACCOUNT_PASSWORD, clientPrincipal, additionalParameters);
        this.tenantId = tenantId;
        this.username = username;
        this.password = password;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(scopes);
    }


}
