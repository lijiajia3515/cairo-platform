package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 子应用不存在 认证异常
 */
public class SubappNotApplyException extends AuthenticationException {
	public SubappNotApplyException() {
		super("子应用未开通");
	}

	public SubappNotApplyException(String msg) {
		super(msg);
	}
}
