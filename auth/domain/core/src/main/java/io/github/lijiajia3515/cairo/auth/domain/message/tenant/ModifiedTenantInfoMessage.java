package io.github.lijiajia3515.cairo.auth.domain.message.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建企业消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedTenantInfoMessage {
	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * 名称
	 */
	private String tenantName;

	/**
	 * 开发平台用户ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
