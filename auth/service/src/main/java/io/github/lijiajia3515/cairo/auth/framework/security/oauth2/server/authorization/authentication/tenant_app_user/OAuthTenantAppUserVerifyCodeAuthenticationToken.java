package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.tenant_app_user;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthTenantAppUserAuthorizationTypes.ACCOUNT_VERIFY_CODE;


/**
 * 应用级用户验证码模式 authentication token
 */
@Getter
public class OAuthTenantAppUserVerifyCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


    /**
     * 企业id
     */
    private final String tenantId;

    /**
     * 用户名
     */
    private final String phoneNumber;

    /**
     * 验证码
     */
    private final String verifyCode;

    /**
     * scopes
     */
    private final Set<String> scopes;


    /**
     * Sub-class constructor.
     *
     * @param phoneNumber          phoneNumber
     * @param verifyCode           verifyCode
     * @param clientPrincipal      the authenticated client principal
     * @param additionalParameters the additional parameters
     */
    protected OAuthTenantAppUserVerifyCodeAuthenticationToken(String tenantId, String phoneNumber, String verifyCode, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(ACCOUNT_VERIFY_CODE, clientPrincipal, additionalParameters);
        this.tenantId = tenantId;
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(scopes);
    }

}
