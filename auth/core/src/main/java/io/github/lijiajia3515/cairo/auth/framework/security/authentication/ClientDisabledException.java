package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 客户端已禁用 认证异常
 */
public class ClientDisabledException extends AuthenticationException {
	public ClientDisabledException() {
		super("客户端已禁用");
	}

	public ClientDisabledException(String msg) {
		super(msg);
	}
}
