package io.github.lijiajia3515.cairo.rabbitmq;

public interface CairoRabbitmqRouteKey {

	/**
	 * 获取交换机名称
	 *
	 * @return 交换机名称
	 */
	CairoRabbitmqExchange getExchange();

	/**
	 * 获取路由名称
	 */
	String getName();

	/**
	 * 获取引用企业模式 路由key
	 *
	 * @param tenantId 企业id
	 * @param appId    appId
	 * @return key
	 */
	default String getTenantAppKey(String tenantId, String appId) {
		return getName().replace("$tenantId", tenantId).replace("$appId", appId);
	}

	/**
	 * 获取企业模式 路由key
	 *
	 * @param tenantId 企业id
	 * @return key
	 */
	default String getTenantKey(String tenantId) {
		return getName().replace("$tenantId", tenantId);
	}

	/**
	 * 获取应用
	 *
	 * @param appId    appId
	 * @return key
	 */
	default String getAppKey(String appId) {
		return getName().replace("$appId", appId);
	}

	/**
	 * 获取路由key
	 *
	 * @return key
	 */
	default String getKey() {
		return getName();
	}

}
