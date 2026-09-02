package io.github.lijiajia3515.cairo.auth.framework.sns.exception;

public class SnsAuthenticationCodeFailedException extends SnsAuthenticationException {
	public SnsAuthenticationCodeFailedException() {
		super("第三方认证授权失败");
	}

	public SnsAuthenticationCodeFailedException(String msg) {
		super(msg);
	}
}
