package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue.SYNC_BIZ_DICT_BY_SYS_DICT;


/**
 * 同步业务级字典队列配置
 */
@Configuration(proxyBeanMethods = false)
public class SyncBizDictBySyncSysDictQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public SyncBizDictBySyncSysDictQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue syncBizDictQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(SYNC_BIZ_DICT_BY_SYS_DICT);
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
	public Binding syncBizDictQueueBind(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
									@Qualifier("syncBizDictQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.SYNC_SYS_DICT, "*", "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
