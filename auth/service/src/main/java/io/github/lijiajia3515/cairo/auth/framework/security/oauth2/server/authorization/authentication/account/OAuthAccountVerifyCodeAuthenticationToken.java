package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.authentication.account;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;
import java.util.Set;

import static io.github.lijiajia3515.cairo.auth.framework.security.oauth2.core.OAuthAccountAuthorizationGrantTypes.ACCOUNT_VERIFY_CODE;


/**
 * 账号验证码模式 authentication token
 */
@Getter
public class OAuthAccountVerifyCodeAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {


    /**
     * 手机号码
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
    protected OAuthAccountVerifyCodeAuthenticationToken(String phoneNumber, String verifyCode, Set<String> scopes, Authentication clientPrincipal, Map<String, Object> additionalParameters) {
        super(ACCOUNT_VERIFY_CODE, clientPrincipal, additionalParameters);
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
        this.scopes = scopes;
    }


}
