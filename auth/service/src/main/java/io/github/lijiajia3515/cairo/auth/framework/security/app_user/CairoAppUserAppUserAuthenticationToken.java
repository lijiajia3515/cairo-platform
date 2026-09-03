package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;


@Getter
public class CairoAppUserAppUserAuthenticationToken extends AbstractAuthenticationToken {
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
	 * 用户ID
	 */
	private final String userId;

	/**
	 * 登录方式
	 */
	private final LoginType loginType;

	/**
	 * Creates a token
	 */
	public CairoAppUserAppUserAuthenticationToken(String appId, String endpointId, String clientId,String userId, LoginType loginType) {
		super(null);
		Assert.notNull(appId, "appId not null");
		Assert.notNull(appId, "endpointId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(userId, "userId not null");

		this.appId = appId;
		this.endpointId = endpointId;
		this.clientId = clientId;
		this.userId = userId;
		this.loginType = loginType;
	}

	@Override
	public String getCredentials() {
		return null;
	}

	@Override
	public String getPrincipal() {
		return String.format("app_user:%s:%s:%s:user:%s", appId, endpointId, clientId, userId);
	}

}
