package io.github.lijiajia3515.cairo.auth.modules.sns.exception;

public class SnsTokenUsedException extends SnsTokenException {
	public SnsTokenUsedException(String token) {
		super("snsToken已经被使用，请重新获取 token: " + token);
	}
}
