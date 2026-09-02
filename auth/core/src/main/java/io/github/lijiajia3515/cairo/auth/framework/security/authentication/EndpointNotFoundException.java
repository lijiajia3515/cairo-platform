package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 终端不存在 认证异常
 */
public class EndpointNotFoundException extends AuthenticationException {
	public EndpointNotFoundException() {
		super("终端不存在");
	}

	public EndpointNotFoundException(String msg) {
		super(msg);
	}
}
