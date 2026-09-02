package io.github.lijiajia3515.cairo.auth.modules.verify_code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证验证码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyVerifyCodeArgs {

	/**
	 * 业务码
	 */
	private String bizCode;
	/**
	 * 对象
	 */
	private String target;

	/**
	 * 验证码
	 */
	private String verifyCode;

	/**
	 * 最大错误次数
	 */
	@Builder.Default
	private int maxFailCount = 3;
}
