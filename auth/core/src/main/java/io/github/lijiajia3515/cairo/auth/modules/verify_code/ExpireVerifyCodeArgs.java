package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 过期验证码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpireVerifyCodeArgs {
	/**
	 * 业务码
	 */
	private String bizCode;
	/**
	 * 目标对象
	 */
	private String target;
}
