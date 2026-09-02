package io.github.lijiajia3515.cairo.auth.modules.app_user.message;

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
public class CreateAppUserByModifiedAppInfoQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateAppUserByModifiedAppInfoQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue createAppUserByModifiedAppInfoQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.CREATE_APP_USER_BY_MODIFIED_APP);
		return QueueBuilder.durable(name)
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
	public Binding createAppUserByModifiedAppInfoQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
															  @Qualifier("createAppUserByModifiedAppInfoQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_APP_INFO, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
