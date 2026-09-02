package io.github.lijiajia3515.cairo.auth.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lijiajia3515.cairo.auth.framework.security.jackson2.CairoAuthSecurityModule;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.jackson2.CairoOAuthClientSecurityModule;
import io.github.lijiajia3515.cairo.redis.CairoKeyRedisSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

@Configuration(proxyBeanMethods = false)
public class RedisConfig {


	@Bean
//	@Primary
	public RedisSerializer<?> authObjectValueRedisSerializer() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

		mapper.registerModule(new JavaTimeModule());
		mapper.registerModule(new CoreJackson2Module());
		mapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
		mapper.registerModule(new OAuth2ClientJackson2Module());
		mapper.registerModule(new CairoAuthSecurityModule());
		mapper.registerModule(new CairoOAuthClientSecurityModule());

		// 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
		// mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

		mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
		GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(mapper);
		return redisSerializer;
	}

	@Bean
	@Primary
	RedisTemplate<String, Object> authStringObjectRedisTemplate(
		RedisConnectionFactory factory,
		CairoKeyRedisSerializer keyRedisSerializer,
		@Qualifier("authObjectValueRedisSerializer") RedisSerializer<?> valueRedisSerializer) {
		final RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(keyRedisSerializer);
		template.setHashKeySerializer(keyRedisSerializer);

		template.setValueSerializer(valueRedisSerializer);
		template.setHashValueSerializer(valueRedisSerializer);
		template.afterPropertiesSet();
		return template;
	}
}
