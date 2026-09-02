package io.github.lijiajia3515.cairo.auth.domain.dto.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图形验证码token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaWebToken implements Serializable {

	/**
	 * 验证码token
	 */
	private String captchaToken;

	/**
	 * 过期时间
	 */
	private Long expireTime;
}
