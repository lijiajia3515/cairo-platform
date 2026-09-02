package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.message;

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
public class OfflineAppUserAuthorizationByLogoffSuccessAppUserQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public OfflineAppUserAuthorizationByLogoffSuccessAppUserQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}


	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */

	@Bean
	public Queue offlineAppUserAuthorizationByLogoffSuccessAppUserQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.OFFLINE_APP_USER_AUTHORIZATION_BY_LOGOFF_SUCCESS_APP_USER);
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
	public Binding offlineAppUserAuthorizationByLogoffSuccessAppUserQueueBinding(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																						 @Qualifier("offlineAppUserAuthorizationByLogoffSuccessAppUserQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_SUCCESS_APP_USER, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
