package io.github.lijiajia3515.cairo.auth.framework.sns.exception;

public abstract class SnsAuthenticationException extends RuntimeException {
	public SnsAuthenticationException(String message) {
		super(message);
	}
}
