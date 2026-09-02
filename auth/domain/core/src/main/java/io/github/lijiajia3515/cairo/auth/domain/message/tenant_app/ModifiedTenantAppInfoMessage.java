package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改企业应用信息消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifiedTenantAppInfoMessage {
	/**
	 * tenantId
	 */
	private String tenantId;

	/**
	 * appId
	 */
	private String appId;

	/**
	 * 被移除的管理员ID
	 */
	private List<String> removeAdminAccountIds;
	/**
	 * 新管理员账号ID
	 */
	private List<String> newAdminAccountIds;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
