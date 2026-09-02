package io.github.lijiajia3515.cairo.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLogAspect;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLogService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.rabbitmq.RabbitmqBizLogService;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class BizLogConfig {

//	@Bean
//	Slf4jBizLogService slf4jBizLogService() {
//		return new Slf4jBizLogService();
//	}

	@Bean
	RabbitmqBizLogService rabbitmqBizLogService(CairoSecurityProperties cairoSecurityProperties, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, ObjectMapper objectMapper) {
		Assert.notNull(cairoSecurityProperties.getCairoAppId(), "appId不能为空");
		return new RabbitmqBizLogService(cairoSecurityProperties.getCairoAppId(), rabbitTemplate, cairoRabbitmqTool, objectMapper);
	}

	@Bean
	BizLogAspect bizLogAspect(ObjectMapper objectMapper, BizLogService bizLogService) {
		return new BizLogAspect(objectMapper, bizLogService);
	}
}
