package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 用户已禁用 认证异常
 */
public class TenantAppUserDisabledException extends AuthenticationException {
	public TenantAppUserDisabledException() {
		super("用户已禁用");
	}

	public TenantAppUserDisabledException(String msg) {
		super(msg);
	}
}
