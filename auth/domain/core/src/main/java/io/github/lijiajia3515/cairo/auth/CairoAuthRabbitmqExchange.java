package io.github.lijiajia3515.cairo.auth;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqExchange;

/**
 * 交换机定义
 */
public enum CairoAuthRabbitmqExchange implements CairoRabbitmqExchange {
	/**
	 * 认证服务领域交换机
	 */
	AUTH("auth")
	;

	/**
	 * 交换机名称
	 */
	private final String name;

	CairoAuthRabbitmqExchange(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
