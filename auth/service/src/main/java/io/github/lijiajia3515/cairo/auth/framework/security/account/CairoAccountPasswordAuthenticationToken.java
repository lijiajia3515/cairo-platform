package io.github.lijiajia3515.cairo.auth.framework.security.account;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * cairo account password authentication token
 */
public class CairoAccountPasswordAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 * 手机号码
	 */
	@Getter
	private final String username;

	/**
	 * 密码
	 */
	private final String password;

	/**
	 * Creates a token
	 */
	public CairoAccountPasswordAuthenticationToken(String username, String password) {
		super(null);
		this.username = username;
		this.password = password;
	}

	@Override
	public Object getCredentials() {
		return password;
	}

	@Override
	public Object getPrincipal() {
		return String.format("account_password_%s", username);
	}
}
