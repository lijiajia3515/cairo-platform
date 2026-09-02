package io.github.lijiajia3515.cairo.auth.domain.message.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已创建账号消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreatedAccountMessage implements Serializable {
	/**
	 * 账号id
	 */
	private String accountId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 登录名
	 */
	private String username;

	/**
	 * 注册时密码
	 */
	private String password;

	/**
	 * 事件账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
