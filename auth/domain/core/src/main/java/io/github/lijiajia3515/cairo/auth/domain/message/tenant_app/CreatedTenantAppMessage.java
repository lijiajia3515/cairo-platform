package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建企业应用消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedTenantAppMessage {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 开通的终端ID
	 */
	private List<String> endpointIds;

	/**
	 * 开通的子应用ID
	 */
	private List<String> subappIds;


	/**
	 * 管理员账号ID数组
	 */
	private List<String> adminAccountIds;

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
