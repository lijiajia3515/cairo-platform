package io.github.lijiajia3515.cairo.core.verifycode;

/**
 * 验证码服务
 */
public interface VerifyCodeService {
	/**
	 * 存储验证码
	 *
	 * @param args 参数
	 */
	void store(StoreVerifyCodeArgs args);

	/**
	 * 核销验证码
	 *
	 * @param args 参数
	 * @return 验证码状态
	 */
	VerifyCodeStat verify(VerifyVerifyCodeArgs args);

	/**
	 * 过期验证码
	 *
	 * @param args 参数
	 */
	void expire(ExpireVerifyCodeArgs args);
}
