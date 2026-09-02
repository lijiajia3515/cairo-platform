package io.github.lijiajia3515.cairo.auth.modules.biz_log;

/**
 * 存储日志服务实现
 */
public interface BizLogService {
	/**
	 * 获取系统应用ID
	 *
	 * @return 应用ID
	 */
	String getAppId();

	/**
	 * 存储匿名端业务日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeOpenBizLog(OpenBizLog bizLog);

	/**
	 * 存储端业务日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeClientBizLog(ClientBizLog bizLog);

	/**
	 * 存储账号日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeAccountBizLog(AccountBizLog bizLog);



	/**
	 * 存储终端业务日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeAppBizLog(AppBizLog bizLog);

	/**
	 * 存储子应用业务日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeSubappBizLog(SubappBizLog bizLog);


	/**
	 * 存储企业终端日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeTenantAppBizLog(TenantAppBizLog bizLog);

	/**
	 * 存储企业子应用日志
	 *
	 * @param bizLog 业务日志
	 */
	void storeTenantSubappBizLog(TenantSubappBizLog bizLog);
}
