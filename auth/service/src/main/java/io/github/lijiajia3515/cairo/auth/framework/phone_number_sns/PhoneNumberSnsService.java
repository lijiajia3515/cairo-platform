package io.github.lijiajia3515.cairo.auth.framework.phone_number_sns;

/**
 * 联接服务
 */
public interface PhoneNumberSnsService {

	/**
	 * 获取第三方认证手机号信息
	 *
	 * @param snsProviderId 第三方认证提供商ID
	 * @param snsCode       第三方认证授权码
	 * @return ConnectOpenId
	 */
	PhoneNumberSnsInfo getPhoneNumberInfo(String snsProviderId, String snsCode) throws Exception;
}
