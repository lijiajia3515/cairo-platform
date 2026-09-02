package io.github.lijiajia3515.cairo.auth.framework.sign.v1;

import io.github.lijiajia3515.cairo.core.business.Business;


public enum SignBusiness implements Business {
	BAD("Sign.Bad", "签名参数错误"),

	TIME_EXPIRED("Sign.TimeExpired", "请求时效过期"),

	REPEATED_REQUEST("Sign.RepeatedRequest", "请求重复");

	private final String code;
	private final String message;

	SignBusiness(String code, String message) {
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
