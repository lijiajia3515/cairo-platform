package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 凭证错误 认证异常
 */
public class TokenInvalidException extends AuthenticationException {
	public TokenInvalidException() {
		super("token错误");
	}

	public TokenInvalidException(String msg) {
		super(msg);
	}
}
