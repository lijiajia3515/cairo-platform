package io.github.lijiajia3515.cairo.rabbitmq;

import java.util.Optional;

public class CairoRabbitmqQueueHelper {
	private final CairoRabbitmqProperties.Queue config;

	public CairoRabbitmqQueueHelper(CairoRabbitmqProperties.Queue config) {
		this.config = config;
	}

	/**
	 * 获取队列名
	 *
	 * @param queue 队列
	 * @return 队列名称
	 */
	public String getName(CairoRabbitmqQueue queue) {
		return Optional.ofNullable(config.getPrefix()).orElse("").concat(queue.getName());
	}

}
