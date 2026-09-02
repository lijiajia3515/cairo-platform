package io.github.lijiajia3515.cairo.auth.modules.captcha.code;

/**
 * captcha 存储接口
 */
public interface CaptchaCodeService {
	/**
	 * 存储图形验证码
	 *
	 * @param captcha 图形验证码
	 */
	void storeCode(CaptchaCode captcha);

	/**
	 * 验证 图形验证码
	 *
	 * @param args 验证参数
	 */
	void verifyCode(VerifyCaptchaCodeArgs args);

}
