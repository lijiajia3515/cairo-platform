package io.github.lijiajia3515.cairo.auth.modules.auth_code;


import io.github.lijiajia3515.cairo.auth.framework.auth_code.AuthCodeVerifyStat;
import io.github.lijiajia3515.cairo.auth.framework.auth_code.VerifyAuthCodeArgs;


public interface AuthCodeClientApiService {

	/**
	 * authCode token 校验
	 * 需要权限 auth_code:verify_token
	 *
	 * @param args args
	 * @return 验证是否通过
	 */
	AuthCodeVerifyStat verifyAuthCode(VerifyAuthCodeArgs args);

}
