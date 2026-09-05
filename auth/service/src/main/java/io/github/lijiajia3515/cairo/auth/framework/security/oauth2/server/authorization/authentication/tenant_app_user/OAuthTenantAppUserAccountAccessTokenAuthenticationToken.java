package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.ACCOUNT_ACCESS_TOKEN;


/**
 * 应用级用户账号授权模式 authentication token
 */
@Getter
public class OAuthTenantAppUserAccountAccessTokenAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


    /**
     * 账号访问令牌
     */
    private final String accountAccessToken;

    /**
     * tenantId
     */
    private final String tenantId;

    /**
     * scopes
     */
    private final Set<String> scopes;

    protected OAuthTenantAppUserAccountAccessTokenAuthenticationToken(String accountAccessToken, String tenantId, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(ACCOUNT_ACCESS_TOKEN, clientPrincipal, additionalParameters);
        this.accountAccessToken = accountAccessToken;
        this.tenantId = tenantId;
        this.scopes = scopes;
    }

}
