package io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqExchange;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqRouteKey;

/**
 * 路由事件
 */
public enum BizLogRabbitmqRouteKey implements CairoRabbitmqRouteKey {

	/**
	 * 匿名认证发送业务日志
	 */
	SEND_OPEN_BIZ_LOG("app_scope.$appId.biz_log.send_open_biz_log"),

	/**
	 * 发送服务端业务日志
	 */
	SEND_CLIENT_BIZ_LOG("app_scope.$appId.biz_log.send_client_biz_log"),

	/**
	 * 发送账号业务日志
	 */
	SEND_ACCOUNT_BIZ_LOG("app_scope.$appId.biz_log.send_account_biz_log"),

	/**
	 * 发送终端业务日志
	 */
	SEND_APP_BIZ_LOG("app_scope.$appId.biz_log.send_app_biz_log"),

	/**
	 * 发送终端业务日志
	 */
	SEND_SUBAPP_BIZ_LOG("app_scope.$appId.biz_log.send_subapp_biz_log"),

	/**
	 * 发送企业终端业务日志
	 */
	SEND_TENANT_APP_BIZ_LOG("tenant_app_scope.$tenantId.$appId.biz_log.send_tenant_app_biz_log"),

	/**
	 * 发送企业子应用业务日志
	 */
	SEND_TENANT_SUBAPP_BIZ_LOG("tenant_app_scope.$tenantId.$appId.biz_log.send_tenant_subapp_biz_log"),

	;

	/**
	 * 路由名称
	 */
	private final String name;


	BizLogRabbitmqRouteKey(String name) {
		this.name = name;
	}


	@Override
	public CairoRabbitmqExchange getExchange() {
		return BizLogRabbitmqExchange.BIZ_LOG;
	}

	@Override
	public String getName() {
		return name;
	}
}
