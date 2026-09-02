package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 账号不存在 认证异常
 */
public class AccountNotFoundException extends AuthenticationException {
	public AccountNotFoundException() {
		super("账号不存在");
	}

	public AccountNotFoundException(String msg) {
		super(msg);
	}
}
