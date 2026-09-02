package io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已删除企业子应用消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedTenantSubappMessage {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 子应用ID
	 */
	private String subappId;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
