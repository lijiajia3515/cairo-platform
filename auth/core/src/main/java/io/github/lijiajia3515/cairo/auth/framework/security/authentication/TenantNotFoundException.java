package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业不存在 认证异常
 */
public class TenantNotFoundException extends AuthenticationException {
	public TenantNotFoundException() {
		super("企业不存在");
	}

	public TenantNotFoundException(String msg) {
		super(msg);
	}
}
