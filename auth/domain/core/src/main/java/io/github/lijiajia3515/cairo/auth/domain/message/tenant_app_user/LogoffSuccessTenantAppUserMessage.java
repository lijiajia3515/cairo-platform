package io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 注销成功企业应用级用户消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LogoffSuccessTenantAppUserMessage implements Serializable {
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
