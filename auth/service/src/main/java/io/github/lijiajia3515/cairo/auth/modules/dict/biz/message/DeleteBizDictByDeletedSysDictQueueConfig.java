package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue.DELETE_BIZ_DICT;


/**
 * 删除业务级字典队列配置
 */
@Configuration(proxyBeanMethods = false)
public class DeleteBizDictByDeletedSysDictQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteBizDictByDeletedSysDictQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue deleteBizDictByDeletedSysDictQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(DELETE_BIZ_DICT);
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
	public Binding deleteBizDictByDeletedSysDictQueueBind(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
												@Qualifier("deleteBizDictByDeletedSysDictQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_SYS_DICT, "*", "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
