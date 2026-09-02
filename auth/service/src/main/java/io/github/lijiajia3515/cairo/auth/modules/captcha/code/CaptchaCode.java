package io.github.lijiajia3515.cairo.auth.modules.captcha.code;

import io.github.lijiajia3515.cairo.auth.domain.dto.captcha.CairoCaptchaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 验证码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaCode {
	/**
	 * 是否过期
	 */
	private boolean expired;

	/**
	 * 访问次数
	 */
	private int count;

	/**
	 * key
	 */
	private String key;

	/**
	 * 验证码类型
	 */
	private CairoCaptchaType type;

	/**
	 * 验证码
	 */
	private String code;

	/**
	 * ip
	 */
	private String ip;

	/**
	 * 过期时间
	 */
	private Duration ttl;
}
