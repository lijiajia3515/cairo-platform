package io.github.lijiajia3515.cairo.auth.modules.menu.message;

import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SyncMenuBySyncedSubappVersionQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public SyncMenuBySyncedSubappVersionQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue syncMenuBySyncedSubappVersionQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.SYNC_MENU_BY_SYNCED_SUBAPP_VERSION);
		return QueueBuilder
			.durable(name)
			.build();
	}

	/**
	 * 业务队列绑定
	 *
	 * @param exchange 业务交换机
	 * @param queue    业务交换机
	 * @return 业务队列绑定
	 */
	@Bean
	public Binding syncMenuBySyncedSubappVersionQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
															  @Qualifier("syncMenuBySyncedSubappVersionQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.SYNCED_SUBAPP_VERSION, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
