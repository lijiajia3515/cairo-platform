package io.github.lijiajia3515.cairo.rabbitmq;

import lombok.Getter;

@Getter
public class CairoRabbitmqTool {
	/**
	 * 交换机工具类
	 */
	private final CairoRabbitmqExchangeHelper exchange;
	/**
	 * 路由工具类
	 */
	private final CairoRabbitmqRouteKeyHelper routeKey;

	/**
	 * 队列名工具类
	 */
	private final CairoRabbitmqQueueHelper queue;

	public CairoRabbitmqTool(CairoRabbitmqExchangeHelper exchange,
							 CairoRabbitmqRouteKeyHelper routeKey,
							 CairoRabbitmqQueueHelper queue) {
		this.exchange = exchange;
		this.routeKey = routeKey;
		this.queue = queue;
	}

}
