package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已删除企业应用消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedTenantAppMessage {
	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
