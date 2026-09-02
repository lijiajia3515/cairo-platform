package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 子应用不存在 认证异常
 */
public class SubappNotFoundException extends AuthenticationException {
	public SubappNotFoundException() {
		super("子应用不存在");
	}

	public SubappNotFoundException(String msg) {
		super(msg);
	}
}
