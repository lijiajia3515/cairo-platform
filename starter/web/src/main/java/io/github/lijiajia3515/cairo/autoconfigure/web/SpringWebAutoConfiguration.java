package io.github.lijiajia3515.cairo.autoconfigure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.web.jackson.CairoJacksonProperties;
import io.github.lijiajia3515.cairo.web.jackson.StandardJackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * spring web 优化配置
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(value = ObjectMapper.class)
public class SpringWebAutoConfiguration {

	/**
	 * 优先级最高的 转换器 由 spring jackson properties 配置
	 *
	 * @param mapper spring objectMapper
	 * @return message converter
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	MappingJackson2HttpMessageConverter highestJsonHttpMessageConverter(ObjectMapper mapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
		converter.setSupportedMediaTypes(List.of(
			new MediaType("application", "json")
		));
		return converter;
	}

	/**
	 * 优先级最低的 转换器(spring mvc config default除外) 由 spring jackson properties 配置
	 *
	 * @param mapper spring jackson objectMapper
	 * @return message converter
	 */
	@Bean
	MappingJackson2HttpMessageConverter lowestJsonHttpMessageConverter(ObjectMapper mapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
		converter.setSupportedMediaTypes(List.of(
			new MediaType("application", "*+json")
		));
		return converter;
	}

	/**
	 * spring actuator 配置 固定写法 (spring admin 前端无扩展导致 ,在此优化)
	 *
	 * @return message converter
	 */
	@Bean
	@Order(100)
	// @ConditionalOnClass(value = {CairoHttpMessageConverter.class, ObjectMapper.class})
	public MappingJackson2HttpMessageConverter springBootActuatorHttpMessageConverter(ApplicationContext context) {
		CairoJacksonProperties properties = CairoJacksonProperties.builder()
			.build();
		Jackson2ObjectMapperBuilderCustomizer customizer = new StandardJackson2ObjectMapperBuilderCustomizer(context, properties);
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		customizer.customize(builder);

		ObjectMapper objectMapper = builder.createXmlMapper(false).build();

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
		converter.setSupportedMediaTypes(List.of(
			new MediaType("application", "vnd.spring-boot.actuator.v1+json"),
			new MediaType("application", "vnd.spring-boot.actuator.v2+json"),
			new MediaType("application", "vnd.spring-boot.actuator.v3+json"),
			new MediaType("application", "vnd.spring-boot.actuator.v4+json"),

			new MediaType("application", "vnd.spring-boot.actuator.v1+json", StandardCharsets.UTF_8),
			new MediaType("application", "vnd.spring-boot.actuator.v2+json", StandardCharsets.UTF_8),
			new MediaType("application", "vnd.spring-boot.actuator.v3+json", StandardCharsets.UTF_8),
			new MediaType("application", "vnd.spring-boot.actuator.v4+json", StandardCharsets.UTF_8)
		));

		return converter;
	}

}
