package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthClientProperties;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.idempotent.RedisIdempotentServiceImpl;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.framework.sign.v1.SignProperties;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.RedisVerifyCodeServiceImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 认证配置
 */
@Configuration(proxyBeanMethods = false)
public class AuthConfig {

	@Bean
	@ConfigurationProperties("cairo.auth")
	@RefreshScope
	public AuthProperties cairoAuthProperties() {
		return new AuthProperties();
	}

	@Bean
	@ConfigurationProperties("cairo.auth.client")
	public AuthClientProperties authClientProperties() {
		return new AuthClientProperties();
	}
	@Bean
	@ConfigurationProperties("cairo.security")
	public CairoSecurityProperties cairoSecurityAuthProperties() {
		return new CairoSecurityProperties();
	}

	@Bean
	@ConfigurationProperties("cairo.sign")
	public SignProperties signProperties() {
		return new SignProperties();
	}


	@Bean
	RedisVerifyCodeServiceImpl verifyCodeService(RedisTemplate<String, Object> redisTemplate) {
		return new RedisVerifyCodeServiceImpl(redisTemplate);
	}

	@Bean
	RedisIdempotentServiceImpl redisIdempotentService(RedisTemplate<String, Object> redisTemplate) {
		return new RedisIdempotentServiceImpl(redisTemplate);
	}

}
