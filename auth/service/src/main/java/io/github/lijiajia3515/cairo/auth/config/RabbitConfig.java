package io.github.lijiajia3515.cairo.auth.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq.BizLogRabbitmqExchange;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@EnableRabbit
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({RabbitAutoConfiguration.class})
public class RabbitConfig {

	private final CairoRabbitmqTool cairoRabbitmqTool;

	public RabbitConfig(CairoRabbitmqTool cairoRabbitmqTool) {
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@Bean
	MessageConverter defaultMessageConverter(ObjectMapper mapper) {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));


		final SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		objectMapper.registerModule(simpleModule);

		final JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper.registerModule(javaTimeModule);

		return new Jackson2JsonMessageConverter(mapper);
	}

	/**
	 * 业务交换机
	 *
	 * @return 交换机
	 */
	@Bean
	public AbstractExchange cairoAuthExchange() {
		String name = cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH);
		return new TopicExchange(name, true, false);
	}

	/**
	 * rabbitmq 客制化
	 *
	 * @return customizer
	 */
	@Bean
	ContainerCustomizer<SimpleMessageListenerContainer> simpleMessageListenerContainer() {
		return container -> container.setMaxConcurrentConsumers(3);
	}

	/**
	 * 业务日志交换机
	 *
	 * @return 交换机
	 */
	@Bean
	public AbstractExchange bizLogExchange() {
		String name = cairoRabbitmqTool.getExchange().getName(BizLogRabbitmqExchange.BIZ_LOG);
		return new TopicExchange(name, true, false);
	}

}
