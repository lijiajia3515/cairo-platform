package io.github.lijiajia3515.cairo.gateway.config;

import io.github.lijiajia3515.cairo.gateway.framework.redis.CairoRedisKeySerializer;
import io.github.lijiajia3515.cairo.gateway.framework.redis.CairoRedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class RedisConfig {

	@Bean
	@ConfigurationProperties(prefix = "redis")
    CairoRedisProperties cairoRedisProperties() {
		return new CairoRedisProperties();
	}

	@Bean
    CairoRedisKeySerializer cairoRedisKeySerializer(CairoRedisProperties properties) {
		return new CairoRedisKeySerializer(properties);
	}

	@Bean
	@ConditionalOnMissingBean(name = "reactiveRedisTemplate")
	@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
	public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
		ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
		CairoRedisKeySerializer keySerializer) {
		GenericJackson2JsonRedisSerializer objectSerializer = new GenericJackson2JsonRedisSerializer();

		RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext.<String, Object>newSerializationContext()
			.key(keySerializer)
			.value(objectSerializer)
			.hashKey(keySerializer)
			.hashValue(objectSerializer)
			.build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
	}

	@Bean
	@Primary
	public ReactiveRedisTemplate<String, Object> reactiveDefaultRedisTemplate(
		ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
		CairoRedisKeySerializer keySerializer) {
		GenericJackson2JsonRedisSerializer objectSerializer = new GenericJackson2JsonRedisSerializer();

		RedisSerializationContext<String, Object> serializationContext = RedisSerializationContext.<String, Object>newSerializationContext()
			.key(keySerializer)
			.value(objectSerializer)
			.hashKey(keySerializer)
			.hashValue(objectSerializer)
			.build();
		return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
	}

	@Bean
	@Primary
	public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
		ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
		CairoRedisKeySerializer keySerializer) {
		StringRedisSerializer valueRedisSerializer = new StringRedisSerializer();
		RedisSerializationContext<String, String> serializationContext = RedisSerializationContext.<String, String>newSerializationContext()
			.key(keySerializer)
			.value(valueRedisSerializer)
			.hashKey(keySerializer)
			.hashValue(valueRedisSerializer)
			.build();
		return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory, serializationContext);
	}
}
