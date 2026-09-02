package io.github.lijiajia3515.cairo.auth.framework.security.core;

import lombok.Getter;
import org.springframework.util.Assert;

import java.io.Serializable;

@Getter
public final class LoginType implements Serializable {

	/**
	 * 密码登录
	 */
	public static final LoginType PASSWORD = new LoginType("password");

	/**
	 * 验证码登录
	 */
	public static final LoginType VERIFY_CODE = new LoginType("verify_code");

	/**
	 * 第三方登录
	 */
	public static final LoginType SNS = new LoginType("sns");

	/**
	 * 账号登录
	 */
	public static final LoginType ACCOUNT = new LoginType("account");

	/**
	 * 未知
	 */
	public static final LoginType UNKNOWN = new LoginType("unknown");

	/**
	 * type value
	 */
	private final String value;

	public LoginType(String value) {
		Assert.hasText(value, "value cannot be empty");
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		LoginType that = (LoginType) obj;
		return this.getValue().equalsIgnoreCase(that.getValue());
	}

	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}

}
