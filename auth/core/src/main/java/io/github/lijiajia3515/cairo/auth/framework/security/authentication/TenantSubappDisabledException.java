package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 子应用未开通 认证异常
 */
public class TenantSubappDisabledException extends AuthenticationException {
	public TenantSubappDisabledException() {
		super("子应用已禁用");
	}

	public TenantSubappDisabledException(String msg) {
		super(msg);
	}
}
