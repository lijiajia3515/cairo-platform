package io.github.lijiajia3515.cairo.auth.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.lijiajia3515.cairo.auth.framework.jackson.CustomModule;
import io.github.lijiajia3515.cairo.auth.framework.jackson.ObjectIdDeserializer;
import io.github.lijiajia3515.cairo.auth.framework.jackson.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration(proxyBeanMethods = false)
public class Jackson2Config {
	@Bean
	JavaTimeModule javaTimeModule() {
		final JavaTimeModule javaTimeModule = new JavaTimeModule();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
		return javaTimeModule;
	}

	@Bean
	SimpleModule simpleModule() {
		final SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
		return simpleModule;
	}

	@Bean
	CustomModule customModule() {
		return new CustomModule();
	}
}
