package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业子应用未开通 认证异常
 */
public class TenantSubappNotApplyException extends AuthenticationException {
	public TenantSubappNotApplyException() {
		super("企业子应用未开通");
	}

	public TenantSubappNotApplyException(String msg) {
		super(msg);
	}
}
