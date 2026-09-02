package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业未申请此应用 认证异常
 */
public class TenantAppNotApplyException extends AuthenticationException {
	public TenantAppNotApplyException() {
		super("企业未申请此应用");
	}

	public TenantAppNotApplyException(String msg) {
		super(msg);
	}
}
