package io.github.lijiajia3515.cairo.auth.modules.sns.exception;

public abstract class SnsTokenException extends RuntimeException {
	public SnsTokenException(String message) {
		super(message);
	}
}
