package io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqExchange;

/**
 * 交换机定义
 */
public enum BizLogRabbitmqExchange implements CairoRabbitmqExchange {
	/**
	 * 认证服务领域交换机
	 */
	BIZ_LOG("biz_log")
	;

	/**
	 * 交换机名称
	 */
	private final String name;

	BizLogRabbitmqExchange(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
