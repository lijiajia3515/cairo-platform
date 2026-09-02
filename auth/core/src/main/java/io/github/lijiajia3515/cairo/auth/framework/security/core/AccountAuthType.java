package io.github.lijiajia3515.cairo.auth.framework.security.core;

import lombok.Getter;
import org.springframework.util.Assert;

import java.io.Serializable;

@Getter
public final class AccountAuthType implements Serializable {



	/**
	 * 单点登录
	 */
	public static final AccountAuthType SSO = new AccountAuthType("sso");

	/**
	 * oauth2登录
	 */
	public static final AccountAuthType OAUTH2 = new AccountAuthType("oauth2");

	/**
	 * 未知
	 */
	public static final AccountAuthType UNKNOWN = new AccountAuthType("unknown");

	/**
	 * type value
	 */
	private final String value;

	public AccountAuthType(String value) {
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
		AccountAuthType that = (AccountAuthType) obj;
		return this.getValue().equalsIgnoreCase(that.getValue());
	}

	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}

}
