package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 终端已禁用 认证异常
 */
public class EndpointDisabledException extends AuthenticationException {
	public EndpointDisabledException() {
		super("终端已禁用");
	}

	public EndpointDisabledException(String msg) {
		super(msg);
	}
}
