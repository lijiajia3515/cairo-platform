package io.github.lijiajia3515.cairo.auth.domain.message.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已创建企业消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedTenantMessage {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 企业名称
	 */
	private String tenantName;


	/**
	 * 别名
	 */
	private String aliasName;


	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 企业拥有者
	 */
	private String ownerAccountId;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
