package io.github.lijiajia3515.cairo.auth.modules.file.tenant_file.message;

import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 发送短信队列配置
 */
@Configuration(proxyBeanMethods = false)
public class CreateTenantBucketQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateTenantBucketQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue createTenantBucketQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.CREATE_TENANT_BUCKET);
		return QueueBuilder
			.durable(name)
			.build();
	}

	/**
	 * 业务队列绑定
	 *
	 * @param queue    业务交换机
	 * @return 业务队列绑定
	 */
	@Bean
	public Binding createTenantBucketQueueBind(@Qualifier("createTenantBucketQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT, "*");
		return BindingBuilder
			.bind(queue)
			.to(authExchange())
			.with(routeKey)
			.noargs();
	}

	public AbstractExchange authExchange() {
		String name = cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH);
		return new TopicExchange(name, true, false);
	}
}
