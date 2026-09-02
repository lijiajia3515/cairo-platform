package io.github.lijiajia3515.cairo.auth.framework.sns;

public interface SnsProvider {

	/**
	 * 获取第三方认证 OpenId
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @param snsCode       第三方认证授权码
	 * @return 获取OpenId
	 */
	SnsInfo getSnsInfo(String snsProviderId, String snsCode);


	/**
	 * 第三方认证类型是否支持
	 *
	 * @param snsType 连接类型
	 * @return 支持
	 */
	boolean supports(String snsType);
}
