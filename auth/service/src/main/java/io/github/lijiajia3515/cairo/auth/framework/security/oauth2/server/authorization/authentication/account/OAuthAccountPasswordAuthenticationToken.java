package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_PASSWORD;


/**
 * 账号密码模式 authentication token
 */
@Getter
public class OAuthAccountPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


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
    protected OAuthAccountPasswordAuthenticationToken(String username, String password, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(ACCOUNT_PASSWORD, clientPrincipal, additionalParameters);
        this.username = username;
        this.password = password;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(scopes);
    }

}
