package io.github.lijiajia3515.cairo.auth.modules.login_log.tenant_app_user_login_log.message;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
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
public class DeleteTenantAppUserLoginLogByDeletedTenantEndpointQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteTenantAppUserLoginLogByDeletedTenantEndpointQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue deleteTenantAppUserLoginLogByDeletedTenantEndpointQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.DELETE_TENANT_APP_USER_LOGIN_LOG_DELETED_TENANT_ENDPOINT);
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
	public Binding deleteTenantAppUserLoginLogByDeletedTenantEndpointQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																							 @Qualifier("deleteTenantAppUserLoginLogByDeletedTenantEndpointQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_ENDPOINT, "*", "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
