package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import lombok.Getter;

public class WebOfficeRuntimeException extends RuntimeException {

	@Getter
	private final WebOfficeError error;

	public WebOfficeRuntimeException(WebOfficeError error) {
		this.error = error;
	}

	public WebOfficeRuntimeException(WebOfficeError error, String message) {
		super(message);
		this.error = error;
	}

	public WebOfficeRuntimeException(WebOfficeError error, String message, Throwable cause) {
		super(message, cause);
		this.error = error;
	}

	public WebOfficeRuntimeException(WebOfficeError error, Throwable cause) {
		super(cause);

		this.error = error;
	}

	public WebOfficeRuntimeException(WebOfficeError error, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.error = error;
	}
}
