package io.github.lijiajia3515.cairo.rabbitmq;

import lombok.Data;

import java.util.Map;

@Data
public class CairoRabbitmqProperties {
	/**
	 * 交换机配置
	 */
	private Map<String, Exchange> exchange;

	/**
	 * 路由key前缀
	 */
	private Map<String, RouteKey> routeKey;

	/**
	 * 队列名称配置
	 */
	private Queue queue;

	/**
	 * 路由名称
	 */
	@Data
	public static class Exchange {
		private String name;
	}

	@Data
	public static class RouteKey {
		private String prefix;
	}

	@Data
	public static class Queue {
		private String prefix;
	}
}
