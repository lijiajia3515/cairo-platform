package io.github.lijiajia3515.cairo.core.exception;

import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.core.business.RequestBusiness;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 业务冲突异常
 */
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "冲突")
public class ConflictBusinessException extends BusinessException {
	public ConflictBusinessException() {
		super(DefaultBusiness.CONFLICT);
	}

	public ConflictBusinessException(String message) {
		super(message, DefaultBusiness.CONFLICT);
	}

	public ConflictBusinessException(String message, Business business) {
		super(message, business);
	}

	public ConflictBusinessException(String message, Throwable superThrowable, Business business) {
		super(message, superThrowable, business);
	}
}
