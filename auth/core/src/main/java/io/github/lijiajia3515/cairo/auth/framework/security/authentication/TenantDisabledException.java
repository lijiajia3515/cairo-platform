package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业已禁用 认证异常
 */
public class TenantDisabledException extends AuthenticationException {
	public TenantDisabledException() {
		super("企业已禁用");
	}

	public TenantDisabledException(String msg) {
		super(msg);
	}
}
