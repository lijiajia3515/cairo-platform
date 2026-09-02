package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 修改企业应用状态消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedTenantAppStatusMessage {
	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * enabled
	 */
	private Boolean enabled;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
