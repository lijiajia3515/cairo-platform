package io.github.lijiajia3515.cairo.auth.framework.security.sns;

/**
 * 认证连接服务
 */
public interface TenantAppUserSnsAuthService {
	/**
	 * 企业应用级用户ID
	 *
	 * @param tenantId      企业ID
	 * @param appId         应用id
	 * @param snsType       第三方认证类型
	 * @param snsProviderId 第三方认证提供者ID
	 * @param snsCode       第三方认证授权码
	 * @return 企业应用级用户ID
	 */
	String getTenantAppUserId(String tenantId, String appId, String snsType, String snsProviderId, String snsCode);
}
