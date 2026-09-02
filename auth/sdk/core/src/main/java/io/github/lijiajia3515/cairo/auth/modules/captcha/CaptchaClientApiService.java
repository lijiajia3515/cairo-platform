package io.github.lijiajia3515.cairo.auth.modules.captcha;


import io.github.lijiajia3515.cairo.auth.domain.api.client.captcha.VerifyCaptchaTokenArgs;

public interface CaptchaClientApiService {

	/**
	 * 验证行为验证码token
	 * 需要权限 captcha:verify_token | captcha:all
	 *
	 * @param args args
	 * @return 验证是否通过
	 */
	Boolean verifyCaptchaToken(VerifyCaptchaTokenArgs args);

}
