package io.github.lijiajia3515.cairo.core.exception;

import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.ParamsBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 参数异常
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "参数错误")
public class ParamsErrorBusinessException extends BusinessException {
	public ParamsErrorBusinessException() {
		super(ParamsBusiness.ERROR);
	}

	public ParamsErrorBusinessException(String message) {
		this(message, ParamsBusiness.ERROR);
	}

	public ParamsErrorBusinessException(String message, Business business) {
		super(message, business);
	}

	public ParamsErrorBusinessException(String message, Throwable superThrowable, Business business) {
		super(message, superThrowable, business);
	}
}
