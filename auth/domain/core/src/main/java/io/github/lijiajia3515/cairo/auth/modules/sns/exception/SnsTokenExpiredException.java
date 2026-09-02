package io.github.lijiajia3515.cairo.auth.modules.sns.exception;

import java.time.LocalDateTime;

public class SnsTokenExpiredException extends SnsTokenException {
	public SnsTokenExpiredException(String token, LocalDateTime expiredTime) {
		super("snsToken已过期【token: " + token + ",过期时间: " + expiredTime.toString() + "】，请重新获取");
	}
}
