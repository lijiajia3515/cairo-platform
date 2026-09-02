package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 企业未申请此终端 认证异常
 */
public class TenantEndpointNotApplyException extends AuthenticationException {
	public TenantEndpointNotApplyException() {
		super("企业未申请此终端");
	}

	public TenantEndpointNotApplyException(String msg) {
		super(msg);
	}
}
