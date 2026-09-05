package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 企业应用级用户会话状态
 */
public enum TenantAppUserAuthorizationStatus {
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

	TenantAppUserAuthorizationStatus(String statusValue) {
		this.statusValue = statusValue;
	}

	public static Optional<TenantAppUserAuthorizationStatus> statusValueOf(String statusValue) {
		return Arrays.stream(TenantAppUserAuthorizationStatus.values()).filter(x -> x.statusValue.equals(statusValue)).findFirst();
	}

	public static boolean isInvalidated(String statusValue) {
		return statusValue == null || !statusValue.equals(OK.statusValue);
	}
}
