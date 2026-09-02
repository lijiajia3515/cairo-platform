package io.github.lijiajia3515.cairo.auth.domain.dto.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetCaptchaResponse {
	/**
	 * 校验码key
	 */
	private String captchaKey;

	/**
	 * 验证码类型
	 */
	private CairoCaptchaType captchaType;

	/**
	 * 验证码图片
	 */
	private String captchaImageUrl;
	/**
	 * 过期时间
	 */
	private Long expireTime;
}
