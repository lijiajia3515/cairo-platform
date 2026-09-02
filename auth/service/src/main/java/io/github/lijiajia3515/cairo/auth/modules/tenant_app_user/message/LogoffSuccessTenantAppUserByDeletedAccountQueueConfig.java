package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;

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
public class LogoffSuccessTenantAppUserByDeletedAccountQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public LogoffSuccessTenantAppUserByDeletedAccountQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue logoffSuccessTenantAppUserByDeletedAccountQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.LOGOFF_SUCCESS_TENANT_APP_USER_BY_DELETED_ACCOUNT);
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
	public Binding logoffSuccessTenantAppUserByDeletedAccountQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																		  @Qualifier("logoffSuccessTenantAppUserByDeletedAccountQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.DELETED_ACCOUNT);
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
