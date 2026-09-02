package io.github.lijiajia3515.cairo.auth.domain.api.open.captcha;

import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaStyle;
import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class GetCaptchaArgs {
	/**
	 * 类型
	 */
	private CairoCaptchaType type;

	/**
	 * 风格
	 */
	private CairoCaptchaStyle style;

	/**
	 * 图片宽度
	 */
	@Builder.Default
	private int width = 160;

	/**
	 * 高度
	 */
	@Builder.Default
	private int height = 50;

}
