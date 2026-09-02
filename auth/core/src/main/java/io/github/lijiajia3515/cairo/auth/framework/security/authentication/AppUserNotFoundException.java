package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 应用用户不存在 认证异常
 */
public class AppUserNotFoundException extends AuthenticationException {
	public AppUserNotFoundException() {
		super("应用用户不存在");
	}

	public AppUserNotFoundException(String msg) {
		super(msg);
	}
}
