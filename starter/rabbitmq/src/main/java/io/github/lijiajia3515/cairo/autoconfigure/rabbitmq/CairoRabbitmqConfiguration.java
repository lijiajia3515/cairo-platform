package io.github.lijiajia3515.cairo.autoconfigure.rabbitmq;

import io.github.lijiajia3515.cairo.rabbitmq.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CairoRabbitmqConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "cairo.rabbitmq")
	@ConditionalOnClass(CairoRabbitmqProperties.class)
	@ConditionalOnMissingBean(CairoRabbitmqProperties.class)
	CairoRabbitmqProperties cairoRabbitmqProperties() {
		return new CairoRabbitmqProperties();
	}

	@Bean
	@ConditionalOnMissingBean(CairoRabbitmqTool.class)
	CairoRabbitmqTool cairoRabbitmqTool(CairoRabbitmqProperties cairoRabbitmqProperties) {
		CairoRabbitmqExchangeHelper exchange = new CairoRabbitmqExchangeHelper(cairoRabbitmqProperties.getExchange());
		CairoRabbitmqRouteKeyHelper routeKey = new CairoRabbitmqRouteKeyHelper(cairoRabbitmqProperties.getRouteKey());
		CairoRabbitmqQueueHelper queue = new CairoRabbitmqQueueHelper(cairoRabbitmqProperties.getQueue());
		return new CairoRabbitmqTool(exchange, routeKey, queue);

	}
}
