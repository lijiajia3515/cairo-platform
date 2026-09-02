package io.github.lijiajia3515.cairo.auth.modules.account_authorization;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 账号会话状态
 */
public enum AccountAuthorizationStatus {
	/**
	 * 正常/使用中/登录
	 */
	OK("ok"),

	/**
	 * 已过期
	 */
	EXPIRED("expired"),

	/**
	 * 黑名单
	 */
	BLACKLIST("blacklist"),

	/**
	 * 登出
	 */
	LOGOUT("logout");

	@Getter
	private final String statusValue;

	AccountAuthorizationStatus(String statusValue) {
		this.statusValue = statusValue;
	}

	public static Optional<AccountAuthorizationStatus> statusValueOf(String statusValue) {
		return Arrays.stream(AccountAuthorizationStatus.values()).filter(x -> x.statusValue.equals(statusValue)).findFirst();
	}

	public static boolean isInvalidated(String statusValue) {
		return statusValue == null || !statusValue.equals(OK.statusValue);
	}
}
