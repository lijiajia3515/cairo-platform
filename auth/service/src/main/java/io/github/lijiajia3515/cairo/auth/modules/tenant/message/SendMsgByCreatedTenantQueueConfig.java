package io.github.lijiajia3515.cairo.auth.modules.tenant.message;

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
public class SendMsgByCreatedTenantQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public SendMsgByCreatedTenantQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue sendMsgByCreatedTenantQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.SEND_MESSAGE_BY_CREATED_TENANT);
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
	public Binding sendMsgByCreatedTenantQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
														  @Qualifier("sendMsgByCreatedTenantQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
