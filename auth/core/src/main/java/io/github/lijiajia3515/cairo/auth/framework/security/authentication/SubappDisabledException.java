package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 子应用已禁用 认证异常
 */
public class SubappDisabledException extends AuthenticationException {
	public SubappDisabledException() {
		super("子应用被禁用");
	}

	public SubappDisabledException(String msg) {
		super(msg);
	}
}
