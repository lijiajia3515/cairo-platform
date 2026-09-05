package io.github.lijiajia3515.cairo.rabbitmq;

import java.util.Map;
import java.util.Optional;

/**
 * 路由工具类
 */
public class CairoRabbitmqRouteKeyHelper {

	private static final String DEFAULT_CONFIG = "default";
	private final Map<String, CairoRabbitmqProperties.RouteKey> config;

	public CairoRabbitmqRouteKeyHelper(Map<String, CairoRabbitmqProperties.RouteKey> config) {
		this.config = config;
	}

	/**
	 * 获取路由key
	 *
	 * @param routeKey 路由key
	 * @return 路由key
	 */
	public String getKey(CairoRabbitmqRouteKey routeKey) {
		return getPrefix(routeKey).concat(routeKey.getKey());
	}

	/**
	 * 获取应用企业模式的路由key
	 *
	 * @param routeKey 路由key
	 * @param tenantId 企业id
	 * @param appId    应用id
	 * @return 路由key
	 */
	public String getTenantAppKey(CairoRabbitmqRouteKey routeKey, String tenantId, String appId) {
		return getPrefix(routeKey).concat(routeKey.getTenantAppKey(tenantId, appId));
	}

	/**
	 * 获取应用企业模式的路由key
	 *
	 * @param routeKey 路由key
	 * @param tenantId 企业id
	 * @return 路由key
	 */
	public String getTenantKey(CairoRabbitmqRouteKey routeKey, String tenantId) {
		return getPrefix(routeKey).concat(routeKey.getTenantKey(tenantId));
	}

	/**
	 * 获取应用企业模式的路由key
	 *
	 * @param routeKey 路由key
	 * @param appId appId
	 * @return 路由key
	 */
	public String getAppKey(CairoRabbitmqRouteKey routeKey, String appId) {
		return getPrefix(routeKey).concat(routeKey.getAppKey(appId));
	}

	protected String getPrefix(CairoRabbitmqRouteKey routeKey) {
		return Optional.ofNullable(routeKey.getExchange())
			.map(CairoRabbitmqExchange::getName)
			.or(() -> Optional.of(DEFAULT_CONFIG))
			.map(config::get)
			.map(CairoRabbitmqProperties.RouteKey::getPrefix)
			.orElse("cairo_default.");
	}
}
