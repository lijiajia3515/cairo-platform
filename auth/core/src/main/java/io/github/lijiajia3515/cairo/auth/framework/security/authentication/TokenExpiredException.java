package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * token过期 认证异常
 */
public class TokenExpiredException extends AuthenticationException {
	public TokenExpiredException() {
		super("token过期异常");
	}

	public TokenExpiredException(String msg) {
		super(msg);
	}
}
