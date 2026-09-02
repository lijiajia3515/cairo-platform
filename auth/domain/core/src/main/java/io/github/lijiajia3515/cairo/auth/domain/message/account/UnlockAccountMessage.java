package io.github.lijiajia3515.cairo.auth.domain.message.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 解锁账号
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UnlockAccountMessage implements Serializable {
	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 上次锁定时间
	 */
	private LocalDateTime lockedTime;

	/**
	 * 解锁时间
	 */
	private LocalDateTime unlockTime;

	/**
	 * 事件账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件时间
	 */
	private LocalDateTime eventTime;
}
