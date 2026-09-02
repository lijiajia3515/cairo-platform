package io.github.lijiajia3515.cairo.auth.framework.security.core;

public enum PasswordType {
	/**
	 * 登录密码
	 */
	PASSWORD("password"),

	/**
	 * 验证码
	 */
	VERIFY_CODE("verify_code"),

	/**
	 * 交易密码
	 */
	PAY_NUMBER("pay_number"),

	/**
	 * 指纹密码
	 */
	FINGERPRINT("fingerprint"),

	/**
	 * 人脸
	 */
	FACE("face");

	/**
	 * 类型
	 */
	private final String type;

	PasswordType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
