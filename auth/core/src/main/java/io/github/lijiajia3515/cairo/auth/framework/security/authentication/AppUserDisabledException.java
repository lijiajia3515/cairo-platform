package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 应用用户已禁用 认证异常
 */
public class AppUserDisabledException extends AuthenticationException {
	public AppUserDisabledException() {
		super("应用用户已禁用");
	}

	public AppUserDisabledException(String msg) {
		super(msg);
	}
}
