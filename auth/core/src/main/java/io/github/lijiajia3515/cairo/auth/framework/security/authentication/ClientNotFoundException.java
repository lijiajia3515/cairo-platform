package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 客户端不存在 认证异常
 */
public class ClientNotFoundException extends AuthenticationException {
	public ClientNotFoundException() {
		super("客户端不存在");
	}

	public ClientNotFoundException(String msg) {
		super(msg);
	}
}
