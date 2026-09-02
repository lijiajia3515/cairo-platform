package io.github.lijiajia3515.cairo.auth.modules.sms.message.message;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 根据手机号发送消息队列处理器
 */
@Configuration(proxyBeanMethods = false)
public class DeleteSmsMsgByDeletedSmsTemplateQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteSmsMsgByDeletedSmsTemplateQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue deleteSmsMsgByDeletedSmsTemplateQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.DELETE_SMS_MSG_BY_DELETED_SMS_TEMPLATE);
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
	public Binding deleteSmsMsgByDeletedSmsTemplateQueueBind(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																 @Qualifier("deleteSmsMsgByDeletedSmsTemplateQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SMS_TEMPLATE, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
