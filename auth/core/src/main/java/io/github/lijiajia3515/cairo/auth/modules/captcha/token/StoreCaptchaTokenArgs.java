package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证图形验证码token接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreCaptchaTokenArgs {
	/**
	 * 来源ip
	 */
	@NotNull
	private String ip;

	/**
	 * 允许失败最大次数
	 */
	@NotNull
	private int maxFailCount;
}
