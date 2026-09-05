package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * 应用级用户账号授权模式 authentication token
 */
@Getter
public class CairoAppUserAccountAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 * 应用ID
	 */
	private final String appId;

	/**
	 * 终端ID
	 */
	private final String endpointId;

	/**
	 * 客户端ID
	 */
	private final String clientId;

	/**
	 * 账号ID
	 */
	private final String accountId;

	/**
	 * 登录方式
	 */
	private final LoginType loginType;

	/**
	 * Creates a token
	 */
	public CairoAppUserAccountAuthenticationToken(String appId, String endpointId, String clientId, String accountId, LoginType loginType) {
		super(null);

		Assert.notNull(appId, "appId not null");
		Assert.notNull(endpointId, "endpointId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(accountId, "accountId not null");
		Assert.notNull(loginType, "loginType not null");

		this.appId = appId;
		this.endpointId = endpointId;
		this.clientId = clientId;
		this.accountId = accountId;
		this.loginType = loginType;
	}

	@Override
	public String getCredentials() {
		return null;
	}

	@Override
	public String getPrincipal() {
		return String.format("app_user:%s:%s:%s:account:%s", appId, endpointId, clientId, accountId);
	}

}
