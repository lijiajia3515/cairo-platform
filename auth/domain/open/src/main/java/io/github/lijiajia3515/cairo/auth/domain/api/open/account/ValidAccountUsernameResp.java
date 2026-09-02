package io.github.lijiajia3515.cairo.auth.domain.api.open.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 校验账号用户名返回值
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ValidAccountUsernameResp {
	/**
	 * 是否可用
	 */
	private boolean success;

	/**
	 * 格式异常
	 */
	private boolean formatIllegal;

	/**
	 * 已存在
	 */
	private boolean exists;
}
