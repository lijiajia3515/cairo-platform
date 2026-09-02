package io.github.lijiajia3515.cairo.auth.modules.biz_log.message;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqQueue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 业务日志队列定义
 */
public enum BizLogQueue implements CairoRabbitmqQueue {

	/**
	 * 存储公开消息队列
	 */
	STORE_OPEN_BIZ_LOG("biz_log.store_open_biz_log"),

	/**
	 * 存储服务级别日志
	 */
	STORE_CLIENT_BIZ_LOG("biz_log.store_client_biz_log"),

	/**
	 * 存储账号级别日志
	 */
	STORE_ACCOUNT_BIZ_LOG("biz_log.store_account_biz_log"),


	/**
	 * 存储终端级别日志
	 */
	STORE_APP_BIZ_LOG("biz_log.store_app_biz_log"),


	/**
	 * 存储子应用业务日志
	 */
	STORE_SUBAPP_BIZ_LOG("biz_log.store_subapp_biz_log"),


	/**
	 * 存储企业终端业务日志
	 */
	STORE_TENANT_APP_BIZ_LOG("biz_log.store_tenant_app_biz_log"),

	/**
	 * 存储企业子应用业务日志
	 */
	STORE_TENANT_SUBAPP_BIZ_LOG("biz_log.store_tenant_subapp_biz_log"),
	;

	/**
	 * 队列名称
	 */
	private final String name;

	BizLogQueue(@Valid @NotNull String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
