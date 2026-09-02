package io.github.lijiajia3515.cairo.auth.domain.api.client.captcha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 获取校验码参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VerifyCaptchaTokenArgs {
	/**
	 * 请求token
	 */
	private String captchaToken;

	/**
	 * 客户端ip
	 */
	private String clientIp;

}
