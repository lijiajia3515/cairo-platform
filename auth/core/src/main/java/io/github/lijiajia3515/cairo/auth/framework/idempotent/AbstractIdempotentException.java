package io.github.lijiajia3515.cairo.auth.framework.idempotent;

import io.github.lijiajia3515.cairo.core.business.Business;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;

public abstract class AbstractIdempotentException extends ConflictBusinessException {

	public AbstractIdempotentException(String message, Business business) {
		super(message, business);
	}

	public AbstractIdempotentException(String message, Throwable superThrowable, Business business) {
		super(message, superThrowable, business);
	}
}
