package io.github.lijiajia3515.cairo.auth.domain.message.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已修改账号密码消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifiedAccountPasswordMessage implements Serializable {
	/**
	 * 账号ID
	 */
	private String accountId;

	/**
	 * 账号ID
	 */
	private String eventAccountId;

	/**
	 * 事件账号ID
	 */
	private LocalDateTime eventTime;
}
