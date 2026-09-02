package io.github.lijiajia3515.cairo.auth.framework.sns.exception;

/**
 * 第三方认证账号不支持异常
 */
public class SnsAuthenticationNotSupportException extends SnsAuthenticationException {
	public SnsAuthenticationNotSupportException() {
		super("第三方账号认证类型不支持");
	}

	public SnsAuthenticationNotSupportException(String message) {
		super(message);
	}
}
