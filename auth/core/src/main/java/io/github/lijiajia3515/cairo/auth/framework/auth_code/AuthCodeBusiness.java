package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import io.github.lijiajia3515.cairo.core.business.Business;


public enum AuthCodeBusiness implements Business {
	PARAMS_ERROR("AuthCode.ParamsError","认证码参数错误"),
	EXPIRED("AuthCode.CodeExpired", "认证码失效"),
	BAD("AuthCode.Bad","认证码错误");

	private final String code;
	private final String message;

	AuthCodeBusiness(String code, String message) {
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
