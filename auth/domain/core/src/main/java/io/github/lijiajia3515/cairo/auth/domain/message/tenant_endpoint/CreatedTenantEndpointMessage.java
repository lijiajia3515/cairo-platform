package io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已创建企业终端消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedTenantEndpointMessage {
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
	 * 状态
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
