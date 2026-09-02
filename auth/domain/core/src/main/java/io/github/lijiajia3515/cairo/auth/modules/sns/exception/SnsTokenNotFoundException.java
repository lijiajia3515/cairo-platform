package io.github.lijiajia3515.cairo.auth.modules.sns.exception;

public class SnsTokenNotFoundException extends SnsTokenException {
	public SnsTokenNotFoundException(String token) {
		super("snsToken错误 token: " + token);
	}
}
