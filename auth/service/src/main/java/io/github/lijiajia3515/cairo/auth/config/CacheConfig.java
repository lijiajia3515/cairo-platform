package io.github.lijiajia3515.cairo.auth.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class CacheConfig {

	@EnableCaching
	@Configuration(proxyBeanMethods = false)
	public static class CairoCacheConfigurer implements CachingConfigurer {
		private final CacheManager cacheManager;

		public CairoCacheConfigurer(CacheManager cacheManager) {
			this.cacheManager = cacheManager;
		}

		@Override
		public CacheManager cacheManager() {
			return cacheManager;
		}

		@Override
		public CacheResolver cacheResolver() {
			return new SimpleCacheResolver(cacheManager);
		}

		@Override
		public KeyGenerator keyGenerator() {
			return new CairoKeyGenerator();
		}

		@Override
		public CacheErrorHandler errorHandler() {
			return new SimpleCacheErrorHandler();
		}
	}

	@Bean
	public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties,
														   @Qualifier("authObjectValueRedisSerializer") RedisSerializer<?> valueRedisSerializer) {
		CacheProperties.Redis redisProperties = cacheProperties.getRedis();
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueRedisSerializer));
		if (redisProperties.getTimeToLive() != null) {
			config = config.entryTtl(redisProperties.getTimeToLive());
		}
		if (redisProperties.getKeyPrefix() != null) {
			config = config.computePrefixWith(x -> redisProperties.getKeyPrefix() + ':' + x + ":");
		}
		if (!redisProperties.isCacheNullValues()) {
			config = config.disableCachingNullValues();
		}
		if (!redisProperties.isUseKeyPrefix()) {
			config = config.disableKeyPrefix();
		}
		return config;
	}


	public static class CairoKeyGenerator implements KeyGenerator {

		@Override
		public Object generate(Object target, Method method, Object... params) {
			if (params.length == 0) {
				return SimpleKey.EMPTY;
			}
			return Arrays.stream(params).map(Object::toString).collect(Collectors.joining(":"));
		}
	}
}
