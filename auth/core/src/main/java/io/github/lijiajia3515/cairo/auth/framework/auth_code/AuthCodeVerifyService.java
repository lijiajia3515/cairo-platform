package io.github.lijiajia3515.cairo.auth.framework.auth_code;

/**
 * AuthCode校验接口
 */
public interface AuthCodeVerifyService {

	/**
	 * 验证 AuthCode
	 *
	 * @param args 参数
	 * @return 校验状态
	 */
	AuthCodeVerifyStat verify(VerifyAuthCodeArgs args);
}
