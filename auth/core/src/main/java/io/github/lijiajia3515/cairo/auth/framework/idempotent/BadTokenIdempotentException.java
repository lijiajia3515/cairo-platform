package io.github.lijiajia3515.cairo.auth.framework.idempotent;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BadTokenIdempotentException extends AbstractIdempotentException {


	public BadTokenIdempotentException(String message) {
		super(message, IdempotentBusiness.BAD_TOKEN);
	}

	public BadTokenIdempotentException(String message, Throwable superThrowable) {
		super(message, superThrowable, IdempotentBusiness.BAD_TOKEN);
	}
}
