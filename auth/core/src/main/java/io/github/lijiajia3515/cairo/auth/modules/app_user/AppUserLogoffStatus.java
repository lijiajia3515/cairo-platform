package io.github.lijiajia3515.cairo.auth.modules.app_user;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 应用用户注销状态
 */
public enum AppUserLogoffStatus {
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

	AppUserLogoffStatus(String logoffStatusValue) {
		this.logoffStatusValue = logoffStatusValue;
	}

	public static Optional<AppUserLogoffStatus> logoffStatusValueOf(String logoffStatusValue) {
		return Arrays.stream(AppUserLogoffStatus.values()).filter(x -> x.logoffStatusValue.equals(logoffStatusValue)).findFirst();
	}
}
