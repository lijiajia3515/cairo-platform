package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业被禁用 认证异常
 */
public class TenantAppDisabledException extends AuthenticationException {
	public TenantAppDisabledException() {
		super("企业应用已禁用");
	}

	public TenantAppDisabledException(String msg) {
		super(msg);
	}
}
