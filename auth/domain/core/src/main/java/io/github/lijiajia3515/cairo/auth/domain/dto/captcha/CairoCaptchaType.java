package io.github.lijiajia3515.cairo.auth.domain.dto.captcha;

public enum CairoCaptchaType {
	/**
	 * 数字，字母混合
	 */
	DEFAULT,

	/**
	 * 数字格式
	 */
	NUMBER,

	/**
	 * 英文格式
	 */
	ENGLISH,

	/**
	 * 中文格式
	 */
	CHINESE,

	/**
	 * 数学运算
	 */
	MATH,
}
