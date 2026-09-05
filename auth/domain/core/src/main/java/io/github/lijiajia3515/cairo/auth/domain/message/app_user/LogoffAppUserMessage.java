package io.github.lijiajia3515.cairo.auth.domain.message.app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 注销的应用级用户（3天保护期）
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LogoffAppUserMessage implements Serializable {
	/**
	 * 应用id
	 */
	private String appId;

	/**
	 * 应用级用户id
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
	private String eventAppUserId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
