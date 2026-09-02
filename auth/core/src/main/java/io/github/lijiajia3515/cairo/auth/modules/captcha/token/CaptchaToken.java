package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaToken {
	/**
	 * 验证token
	 */
	private String token;

	/**
	 * ip 所属地
	 */
	private String ip;

	/**
	 * 访问次数
	 */
	private Integer count;

	/**
	 * 过期时间
	 */
	private long ttl;
}
