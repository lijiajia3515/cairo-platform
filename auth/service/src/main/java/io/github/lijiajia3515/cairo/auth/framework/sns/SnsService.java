package io.github.lijiajia3515.cairo.auth.framework.sns;

/**
 * 第三方认证服务
 */
public interface SnsService {

	/**
	 * 第三方认证信息
	 *
	 * @param snsType       第三方认证类型
	 * @param snsProviderId 第三方认证提供商ID
	 * @param snsCode       第三方认证授权码
	 * @return 第三方认证信息
	 */
	SnsInfo getSnsInfo(String snsType, String snsProviderId, String snsCode);
}
