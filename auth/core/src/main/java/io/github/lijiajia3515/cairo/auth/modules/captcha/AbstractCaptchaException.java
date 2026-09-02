package io.github.lijiajia3515.cairo.auth.modules.captcha;

import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;

public abstract class AbstractCaptchaException extends BusinessException {

	public AbstractCaptchaException(Business business) {
		super(business);
	}

	public AbstractCaptchaException(String message, Business business) {
		super(message, business);
	}

	public AbstractCaptchaException(String message, Throwable superThrowable, Business business) {
		super(message, superThrowable, business);
	}
}
