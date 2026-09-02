package io.github.lijiajia3515.cairo.auth.modules.account;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 账号注销状态
 */
public enum AccountLogoffStatus {
	/**
	 * 未注销
	 */
	NO("No"),

	/**
	 * 注销中
	 */
	PENDING("Pending"),

	/**
	 * 注销成功
	 */
	SUCCESS("Success");

	@Getter
	private final String logoffStatusValue;

	AccountLogoffStatus(String logoffStatusValue) {
		this.logoffStatusValue = logoffStatusValue;
	}

	public static Optional<AccountLogoffStatus> logoffStatusValueOf(String logoffStatusValue) {
		return Arrays.stream(AccountLogoffStatus.values()).filter(x -> x.logoffStatusValue.equals(logoffStatusValue)).findFirst();
	}
}
