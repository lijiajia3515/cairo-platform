package io.github.lijiajia3515.cairo.auth.framework.security.authentication;


import org.springframework.security.core.AuthenticationException;

/**
 * 验证码错误 认证异常
 */
public class VerifyCodeBadCredentialsException extends AuthenticationException {
	/**
	 * Constructs a <code>BadCredentialsException</code> with the specified message.
	 * @param msg the detail message
	 */
	public VerifyCodeBadCredentialsException(String msg) {
		super(msg);
	}

	/**
	 * Constructs a <code>BadCredentialsException</code> with the specified message and
	 * root cause.
	 * @param msg the detail message
	 * @param cause root cause
	 */
	public VerifyCodeBadCredentialsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
