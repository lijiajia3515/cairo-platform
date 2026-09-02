package io.github.lijiajia3515.cairo.auth.modules.dict.sys.message;

import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class DeleteSysDictByDeletedAppQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteSysDictByDeletedAppQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue deleteSysDictByDeleteAppQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.DELETE_SYS_DICT_BY_DELETE_APP);
		return QueueBuilder
			.durable(name)
			.build();
	}

	/**
	 * 业务队列绑定
	 *
	 * @param queue 业务交换机
	 * @return 业务队列绑定
	 */
	@Bean
	public Binding deleteSysDictByDeleteAppQueueBind(@Qualifier("deleteSysDictByDeleteAppQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_APP, "*");
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
