package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 应用已禁用 认证异常
 */
public class AppDisabledException extends AuthenticationException {
	public AppDisabledException() {
		super("应用已禁用");
	}

	public AppDisabledException(String msg) {
		super(msg);
	}
}
