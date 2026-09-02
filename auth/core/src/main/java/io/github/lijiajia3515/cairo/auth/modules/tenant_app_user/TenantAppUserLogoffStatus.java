package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 企业用户注销状态
 */
public enum TenantAppUserLogoffStatus {
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

	TenantAppUserLogoffStatus(String logoffStatusValue) {
		this.logoffStatusValue = logoffStatusValue;
	}

	public static Optional<TenantAppUserLogoffStatus> logoffStatusValueOf(String logoffStatusValue) {
		return Arrays.stream(TenantAppUserLogoffStatus.values()).filter(x -> x.logoffStatusValue.equals(logoffStatusValue)).findFirst();
	}
}
