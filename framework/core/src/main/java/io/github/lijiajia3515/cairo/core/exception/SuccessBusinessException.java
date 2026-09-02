package io.github.lijiajia3515.cairo.core.exception;

import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 成功异常 保留类
 */
@ResponseStatus(code = HttpStatus.OK, reason = "OK")
public class SuccessBusinessException extends BusinessException {
	public SuccessBusinessException() {
		super(DefaultBusiness.SUCCESS);
	}

	public SuccessBusinessException(String message) {
		super(message, DefaultBusiness.SUCCESS);
	}

	public SuccessBusinessException(String message, Throwable superThrowable) {
		super(message, superThrowable, DefaultBusiness.SUCCESS);
	}
}
