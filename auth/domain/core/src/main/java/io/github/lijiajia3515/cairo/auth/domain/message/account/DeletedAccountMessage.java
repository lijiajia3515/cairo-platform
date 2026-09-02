package io.github.lijiajia3515.cairo.auth.domain.message.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 删除账号消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeletedAccountMessage implements Serializable {
	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 头像Url
	 */
	private String avatarUrl;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinTime;

	/**
	 * 事件账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
