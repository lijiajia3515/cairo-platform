package io.github.lijiajia3515.cairo.auth.framework.security.app_user;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * cairo app user password authentication token
 */
public class CairoAppUserPasswordAuthenticationToken extends AbstractAuthenticationToken {


	/**
	 * appId
	 */
	@Getter
	private final String appId;

	/**
	 * endpointId
	 */
	@Getter
	private final String endpointId;

	/**
	 * clientId
	 */
	@Getter
	private final String clientId;

	/**
	 * 手机号码
	 */
	@Getter
	private final String username;
	/**
	 * 验证码
	 */
	private final String password;

	/**
	 * Creates a token
	 */
	public CairoAppUserPasswordAuthenticationToken(String appId, String endpointId, String clientId, String username, String password) {
		super(null);
		Assert.notNull(appId, "appId not null");
		Assert.notNull(endpointId, "endpointId not null");
		Assert.notNull(clientId, "clientId not null");
		Assert.notNull(username, "username not null");
		Assert.notNull(password, "password not null");

		this.appId = appId;
		this.endpointId = endpointId;
		this.clientId = clientId;
		this.username = username;
		this.password = password;
	}

	@Override
	public String getCredentials() {
		return password;
	}

	@Override
	public String getPrincipal() {
		return String.format("app_user:%s:%s:%s:password:%s", appId, endpointId, clientId, username);
	}

}
