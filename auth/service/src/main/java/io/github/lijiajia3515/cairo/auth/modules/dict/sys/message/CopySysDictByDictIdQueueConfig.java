package io.github.lijiajia3515.cairo.auth.modules.dict.sys.message;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthQueue.COPY_SYS_DICT_BY_DICT;


/**
 * 复制业务级字典队列配置
 */
@Configuration(proxyBeanMethods = false)
public class CopySysDictByDictIdQueueConfig {
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CopySysDictByDictIdQueueConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列声明
	 *
	 * @return 业务队列
	 */
	@Bean
	public Queue copySysDictByDictIdQueue() {
		String name = cairoRabbitmqTool.getQueue().getName(COPY_SYS_DICT_BY_DICT);
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
	public Binding copySysDictByDictIdQueueBind(@Qualifier("cairoAuthExchange") AbstractExchange exchange,
																		 @Qualifier("copySysDictByDictIdQueue") Queue queue) {
		String routeKey = cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.COPY_SYS_DICT, "*", "*");
		return BindingBuilder
			.bind(queue)
			.to(exchange)
			.with(routeKey)
			.noargs();
	}
}
