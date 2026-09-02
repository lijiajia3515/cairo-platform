package io.github.lijiajia3515.cairo.auth.modules.captcha.token;

/**
 * captcha 存储接口
 */
public interface CaptchaTokenService {

	/**
	 * 存储验证token
	 *
	 * @param args 存储参数
	 * @return 图形验证码token
	 */
	CaptchaToken storeToken(StoreCaptchaTokenArgs args);

	/**
	 * 验证token
	 */
	boolean verifyToken(VerifyCaptchaTokenArgs args);
}
