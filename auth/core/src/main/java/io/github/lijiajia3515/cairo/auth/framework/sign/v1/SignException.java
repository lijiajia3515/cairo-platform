package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;

/**
 * 签名异常
 */
public class SignException extends ConflictBusinessException {

	public SignException(String message, SignBusiness business) {
		super(message, business);
	}

	public SignException(String message, Throwable superThrowable, SignBusiness business) {
		super(message, superThrowable, business);
	}
}
