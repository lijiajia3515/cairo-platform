package io.github.lijiajia3515.cairo.auth.modules.captcha.code;

import io.github.lijiajia3515.cairo.core.business.Business;


public enum CaptchaCodeBusiness implements Business {
	NOT_FOUND("CaptchaCode.NotFound","行为验证码不存在"),
	BAD("CaptchaCode.Bad", "行为验证码错误"),
	EXPIRED("CaptchaCode.Expired", "行为验证码已失效");

	private final String code;
	private final String message;

	CaptchaCodeBusiness(String code, String message) {
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
