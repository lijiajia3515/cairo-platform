package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 应用不存在 认证异常
 */
public class AppNotFoundException extends AuthenticationException {
	public AppNotFoundException() {
		super("应用不存在");
	}

	public AppNotFoundException(String msg) {
		super(msg);
	}
}
