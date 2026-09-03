package io.github.lijiajia3515.cairo.auth.framework.security.account;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

@Getter
public class CairoAccountVerifyCodeAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 手机号码
     */
    private final String phoneNumber;

    /**
     * 验证码
     */
    private final String verifyCode;

    /**
     * Creates a token
     */
    public CairoAccountVerifyCodeAuthenticationToken(String phoneNumber, String verifyCode) {
        super(null);
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
    }

    @Override
    public String getCredentials() {
        return verifyCode;
    }

    @Override
    public String getPrincipal() {
        return String.format("account:verify_code:%s", phoneNumber);
    }
}
