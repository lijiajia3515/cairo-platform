package io.github.lijiajia3515.cairo.auth.modules.biz_log.account_biz_log.message;

import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 删除账号级业务日志 根据 已删除客户端 队列配置
 */
@Configuration(proxyBeanMethods = false)
public class DeleteAccountBizLogByDeletedAppQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteAccountBizLogByDeletedAppQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue deleteAccountBizLogByDeletedAppQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(CairoAuthQueue.DELETE_ACCOUNT_BIZ_LOG_BY_DELETED_APP);
		return QueueBuilder
			.durable(name)
			.ttl((int) Duration.ofMinutes(30).toMillis()) // 保留30分钟
			.build();
	}

	/**
	 * 业务队列绑定
	 *
	 * @param queue 业务交换机
	 * @return 业务队列绑定
	 */
	@Bean
	public Binding deleteAccountBizLogByDeletedAppQueueBinding(@Qualifier("deleteAccountBizLogByDeletedAppQueue") Queue queue) {
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
