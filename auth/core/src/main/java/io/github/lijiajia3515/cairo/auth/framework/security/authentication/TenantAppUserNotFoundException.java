package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 用户不存在 认证异常
 */
public class TenantAppUserNotFoundException extends AuthenticationException {
	public TenantAppUserNotFoundException() {
		super("用户不存在");
	}

	public TenantAppUserNotFoundException(String msg) {
		super(msg);
	}
}
