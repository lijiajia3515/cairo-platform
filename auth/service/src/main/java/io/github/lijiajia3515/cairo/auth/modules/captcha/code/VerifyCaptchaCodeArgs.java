package io.github.lijiajia3515.cairo.auth.modules.captcha.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 验证图形验证码字符
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCaptchaCodeArgs {
	/**
	 * 验证key
	 */
	@NotNull(groups = {Api.class, Service.class})
	private String captchaKey;

	/**
	 * 验证code
	 */
	@NotNull(groups = {Api.class, Service.class})
	private String captchaCode;

	/**
	 * ip
	 */
	@NotNull(groups = Service.class)
	private String ip;

	/**
	 * 服务级别验证
	 */
	public interface Service {

	}

	/**
	 * api级别验证
	 */
	public interface Api {

	}

}
