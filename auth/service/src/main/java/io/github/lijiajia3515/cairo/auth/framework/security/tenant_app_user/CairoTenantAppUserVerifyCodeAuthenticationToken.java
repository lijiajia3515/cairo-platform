package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * cairo tenant app user verify code authentication token
 */
@Getter
public class CairoTenantAppUserVerifyCodeAuthenticationToken extends AbstractAuthenticationToken {


    /**
     * tenantId
     */
    private final String tenantId;

    /**
     * appId
     */
    private final String appId;
    /**
     * endpointId
     */
    private final String endpointId;

    /**
     * clientId
     */
    private final String clientId;

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
    public CairoTenantAppUserVerifyCodeAuthenticationToken(String tenantId, String appId, String endpointId, String clientId,  String phoneNumber, String verifyCode) {
        super(null);
		Assert.notNull(tenantId, "tenantId not null");
        Assert.notNull(appId, "appId not null");
        Assert.notNull(appId, "endpointId not null");
        Assert.notNull(clientId, "clientId not null");
        Assert.notNull(phoneNumber, "phoneNumber not null");
        Assert.notNull(verifyCode, "verifyCode not null");

        this.tenantId = tenantId;
        this.appId = appId;
        this.endpointId = endpointId;
        this.clientId = clientId;
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
    }

    @Override
    public String getCredentials() {
        return verifyCode;
    }

    @Override
    public String getPrincipal() {
        return String.format("tenant_app_user:%s:%s:%s:%s:verify_code:%s", tenantId, appId, endpointId, clientId, phoneNumber);
    }

}
