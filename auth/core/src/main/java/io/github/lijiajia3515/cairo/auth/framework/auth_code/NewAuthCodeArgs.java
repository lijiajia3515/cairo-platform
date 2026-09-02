package io.github.lijiajia3515.cairo.auth.framework.auth_code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取认证码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewAuthCodeArgs {

	/**
	 * 账号id
	 */
	private String accountId;

	/**
	 * 写入ip
	 */
	private String ip;
}
