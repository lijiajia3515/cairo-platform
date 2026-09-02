package io.github.lijiajia3515.cairo.auth.framework.security.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * 联接码错误 认证异常
 */
public class SnsCodeFailedException extends AuthenticationException {
	public SnsCodeFailedException() {
		super("第三方认证授权失败");
	}

	public SnsCodeFailedException(String msg) {
		super(msg);
	}
}
