package io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 修改企业终端状态消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedTenantEndpointStatusMessage {
	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * appId
	 */
	private String appId;
	/**
	 * appId
	 */
	private String endpointId;

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
