package io.github.lijiajia3515.cairo.autoconfigure.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lijiajia3515.cairo.redis.CairoKeyRedisSerializer;
import io.github.lijiajia3515.cairo.redis.CairoRedisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class CairoRedisConfiguration {
	@Bean
	@ConfigurationProperties(prefix = "cairo.redis")
	public CairoRedisProperties cairoRedisProperties() {
		return new CairoRedisProperties();
	}

	@Bean("keyRedisSerializer")
	@ConditionalOnMissingBean(CairoKeyRedisSerializer.class)
	public CairoKeyRedisSerializer redisKeySerializer(CairoRedisProperties cairoRedisProperties) {
		return new CairoKeyRedisSerializer(cairoRedisProperties.getKeyPrefix());
	}

	@Bean("stringValueRedisSerializer")
	public RedisSerializer<?> stringValueRedisSerializer() {
		return RedisSerializer.string();
	}

	@Bean("objectValueRedisSerializer")
	public RedisSerializer<?> objectValueRedisSerializer() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		SimpleModule module = new SimpleModule();
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		mapper.registerModule(module);
		mapper.registerModule(javaTimeModule);
		mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
		GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(mapper);
		return redisSerializer;
	}

	@Bean
	RedisTemplate<String, Object> cairostringObjectRedisTemplate(
		RedisConnectionFactory factory,
		@Qualifier("keyRedisSerializer") RedisSerializer<?> keyRedisSerializer,
		@Qualifier("objectValueRedisSerializer") RedisSerializer<?> valueRedisSerializer) {
		final RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(keyRedisSerializer);
		template.setHashKeySerializer(keyRedisSerializer);

		template.setValueSerializer(valueRedisSerializer);
		template.setHashValueSerializer(valueRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public StringRedisTemplate cairoStringRedisTemplate(RedisConnectionFactory redisConnectionFactory, @Qualifier("keyRedisSerializer") RedisSerializer<?> keyRedisSerializer) {
		StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory);
		template.setKeySerializer(keyRedisSerializer);
		template.setHashKeySerializer(keyRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}

}
