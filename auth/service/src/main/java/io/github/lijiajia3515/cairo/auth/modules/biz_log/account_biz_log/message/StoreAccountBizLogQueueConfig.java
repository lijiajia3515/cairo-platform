package io.github.lijiajia3515.cairo.auth.modules.biz_log.account_biz_log.message;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq.BizLogRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.message.BizLogQueue;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 存储账号级别日志队列配置
 */
@Configuration(proxyBeanMethods = false)
public class StoreAccountBizLogQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public StoreAccountBizLogQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue storeAccountBizLogQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(BizLogQueue.STORE_ACCOUNT_BIZ_LOG);
		return QueueBuilder
			.durable(name)
			.ttl((int) Duration.ofMinutes(30).toMillis()) // 保留30分钟
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
	public Binding storeAccountBizLogQueueBinding(@Qualifier("bizLogExchange") AbstractExchange exchange,
												  @Qualifier("storeAccountBizLogQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getAppKey(BizLogRabbitmqRouteKey.SEND_ACCOUNT_BIZ_LOG, "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
