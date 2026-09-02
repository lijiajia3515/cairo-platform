package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建企业应用用户消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreatedTenantAppUserMessage implements Serializable {
	/**
	 * 企业ID
	 */
	private String tenantId;

	/**
	 * 应用ID
	 */
	private String appId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 用户昵称
	 */
	private String nickname;

	/**
	 * 是否管理员
	 */
	private boolean admin;

	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 事件用户ID
	 */
	private String eventUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;

}
