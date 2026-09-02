package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.message;

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
public class CreateTenantSubappByCreatedTenantAppQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateTenantSubappByCreatedTenantAppQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue createTenantSubappByCreatedTenantAppQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.CREATE_TENANT_SUBAPP_BY_CREATED_TENANT_APP);
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
	public Binding createTenantSubappByCreatedTenantAppQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																		@Qualifier("createTenantSubappByCreatedTenantAppQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP, "*", "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
