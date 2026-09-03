package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;


@Getter
public class CairoAppUserAuthenticationToken extends AbstractAuthenticationToken {
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
	 * subappId
	 */
	private final String subappId;

	/**
	 * subappVersion
	 */
	private final String subappVersion;


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
	public CairoAppUserAuthenticationToken(String appId, String endpointId, String clientId, String subappId, String subappVersion, String userId, LoginType loginType) {
		super(null);
		Assert.notNull(appId, "appId not null");
		Assert.notNull(appId, "endpointId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(subappId, "subappId not null");
		Assert.notNull(subappVersion, "subappVersion not null");
		Assert.notNull(userId, "userId not null");

		this.appId = appId;
		this.endpointId = endpointId;
		this.clientId = clientId;
		this.subappId = subappId;
		this.subappVersion = subappVersion;
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
