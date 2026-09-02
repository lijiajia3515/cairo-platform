package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业已禁用 认证异常
 */
public class TenantEndpointDisabledException extends AuthenticationException {
	public TenantEndpointDisabledException() {
		super("企业终端已禁用");
	}

	public TenantEndpointDisabledException(String msg) {
		super(msg);
	}
}
