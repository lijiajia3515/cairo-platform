package io.github.lijiajia3515.cairo.auth.framework.idempotent;

import io.github.lijiajia3515.cairo.core.business.Business;


public enum IdempotentBusiness implements Business {
	BAD_TOKEN("Idempotent.BadToken","幂等校验失败"),
	REPEATED_REQUEST("Idempotent.RepeatedRequest","重复请求")
	;

	private final String code;
	private final String message;

	IdempotentBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public String message() {
		return message;
	}
}
