package io.github.lijiajia3515.cairo.gateway.config;

import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.factory.BusinessResultGatewayFilterFactory;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.factory.CairoPrincipalRequestRateLimiterGatewayFilterFactory;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.factory.CairoRequestRateLimiterGatewayFilterFactory;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.factory.GatewayResultGatewayFilterFactory;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.loadbalancer.CairoLoadBalancerClientFilter;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.requestratelimiter.CairoRedisRateLimiter;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.requestratelimiter.PrincipalKeyResolver;
import io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.requestratelimiter.UrlKeyResolver;
import io.github.lijiajia3515.cairo.gateway.framework.webflux.CairoWebfluxResponseHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.codec.ServerCodecConfigurer;

import java.util.List;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class GatewayConfig {

	@Bean
	@ConditionalOnEnabledFilter
	@ConditionalOnBean(LoadBalancerClientFactory.class)
	public CairoLoadBalancerClientFilter cairoLoadBalancerClientFilter(LoadBalancerClientFactory loadBalancerClientFactory, GatewayLoadBalancerProperties properties) {
		return new CairoLoadBalancerClientFilter(loadBalancerClientFactory, properties);
	}

	@Bean
	@ConditionalOnEnabledFilter
	public BusinessResultGatewayFilterFactory businessResultResponseGatewayFilterFactory(
		ServerCodecConfigurer codecConfigurer, Set<MessageBodyDecoder> bodyDecoders,
		Set<MessageBodyEncoder> bodyEncoders) {
		return new BusinessResultGatewayFilterFactory(codecConfigurer.getReaders(), bodyDecoders, bodyEncoders);
	}

	@Bean
	@ConditionalOnEnabledFilter
	public GatewayResultGatewayFilterFactory gatewayResultResponseGatewayFilterFactory(
		ServerCodecConfigurer codecConfigurer, Set<MessageBodyDecoder> bodyDecoders,
		Set<MessageBodyEncoder> bodyEncoders) {
		return new GatewayResultGatewayFilterFactory(codecConfigurer.getReaders(), bodyDecoders, bodyEncoders);
	}

	@Configuration(proxyBeanMethods = false)
	public static class GatewayRequestRateLimiterConfig {
		@Bean
		@Primary
		public KeyResolver urlKeyResolver() {
			return new UrlKeyResolver();
		}

		@Bean
		public PrincipalKeyResolver principalKeyResolver() {
			return new PrincipalKeyResolver();
		}

		@Bean
		@Primary
		@ConfigurationProperties(prefix = "cairo.cloud.gateway.redis-rate-limiter")
		public RedisRateLimiter redisRateLimiter(ReactiveStringRedisTemplate redisTemplate,
												 @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
												 ConfigurationService configurationService) {
			return new RedisRateLimiter(redisTemplate, redisScript, configurationService);
		}

		@Bean
		@ConfigurationProperties(prefix = "cairo.cloud.gateway.custom.redis-rate-limiter1")
		public CairoRedisRateLimiter redisRateLimiter1(ReactiveStringRedisTemplate redisTemplate,
													   @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
													   ConfigurationService configurationService) {
			CairoRedisRateLimiter redisRateLimiter = new CairoRedisRateLimiter(redisTemplate, redisScript, configurationService, "redis-rate-limiter1");
			redisRateLimiter.setRemainingHeader("X-RateLimit-1-Remaining");
			redisRateLimiter.setReplenishRateHeader("X-RateLimit-1-Replenish-Rate");
			redisRateLimiter.setBurstCapacityHeader("X-RateLimit-1-Burst-Capacity");
			redisRateLimiter.setRequestedTokensHeader("X-RateLimit-1-Requested-Tokens");
			return redisRateLimiter;
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.cloud.gateway.custom.redis-rate-limiter2")
		public CairoRedisRateLimiter redisRateLimiter2(ReactiveStringRedisTemplate redisTemplate,
													   @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
													   ConfigurationService configurationService) {
			CairoRedisRateLimiter redisRateLimiter = new CairoRedisRateLimiter(redisTemplate, redisScript, configurationService, "redis-rate-limiter2");
			redisRateLimiter.setRemainingHeader("X-RateLimit-2-Remaining");
			redisRateLimiter.setReplenishRateHeader("X-RateLimit-2-Replenish-Rate");
			redisRateLimiter.setBurstCapacityHeader("X-RateLimit-2-Burst-Capacity");
			redisRateLimiter.setRequestedTokensHeader("X-RateLimit-2-Requested-Tokens");
			return redisRateLimiter;
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.cloud.gateway.custom.redis-rate-limiter3")
		public CairoRedisRateLimiter redisRateLimiter3(ReactiveStringRedisTemplate redisTemplate,
													   @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
													   ConfigurationService configurationService) {
			CairoRedisRateLimiter redisRateLimiter = new CairoRedisRateLimiter(redisTemplate, redisScript, configurationService, "redis-rate-limiter3");
			redisRateLimiter.setRemainingHeader("X-RateLimit-3-Remaining");
			redisRateLimiter.setReplenishRateHeader("X-RateLimit-3-Replenish-Rate");
			redisRateLimiter.setBurstCapacityHeader("X-RateLimit-3-Burst-Capacity");
			redisRateLimiter.setRequestedTokensHeader("X-RateLimit-3-Requested-Tokens");
			return redisRateLimiter;
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.cloud.gateway.custom.redis-rate-limiter4")
		public CairoRedisRateLimiter redisRateLimiter4(ReactiveStringRedisTemplate redisTemplate,
													   @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> redisScript,
													   ConfigurationService configurationService) {
			CairoRedisRateLimiter redisRateLimiter = new CairoRedisRateLimiter(redisTemplate, redisScript, configurationService, "redis-rate-limiter4");
			redisRateLimiter.setRemainingHeader("X-RateLimit-4-Remaining");
			redisRateLimiter.setReplenishRateHeader("X-RateLimit-4-Replenish-Rate");
			redisRateLimiter.setBurstCapacityHeader("X-RateLimit-4-Burst-Capacity");
			redisRateLimiter.setRequestedTokensHeader("X-RateLimit-4-Requested-Tokens");
			return redisRateLimiter;
		}


		@Bean
		@ConditionalOnEnabledFilter
		public CairoRequestRateLimiterGatewayFilterFactory cairoRequestRateLimiterGatewayFilterFactory(RateLimiter<?> rateLimiter,
																									   KeyResolver resolver,
																									   CairoWebfluxResponseHandler responseHandler) {
			return new CairoRequestRateLimiterGatewayFilterFactory(rateLimiter, resolver, responseHandler, -50);
		}

		@Bean
		@ConditionalOnEnabledFilter
		public CairoPrincipalRequestRateLimiterGatewayFilterFactory cairoPrincipalRequestRateLimiterGatewayFilterFactory(RateLimiter<?> rateLimiter,
																														 @Qualifier("principalKeyResolver") KeyResolver resolver,
																														 CairoWebfluxResponseHandler responseHandler) {
			return new CairoPrincipalRequestRateLimiterGatewayFilterFactory(rateLimiter, resolver, responseHandler, -49);
		}

	}

}
