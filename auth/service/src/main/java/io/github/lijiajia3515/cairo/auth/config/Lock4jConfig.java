package io.github.lijiajia3515.cairo.auth.config;

import com.baomidou.lock.DefaultLockKeyBuilder;
import com.baomidou.lock.LockKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.AccountAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.AppUserAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.SubappUserAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.CairoLockFailureStrategy;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.TenantAppUserAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.TenantSubappUserAuthKeyBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;

/**
 * 分布式锁实现
 */
@Configuration(proxyBeanMethods = false)
public class Lock4jConfig {
	@Bean
	CairoLockFailureStrategy cairoLockFailureStrategy() {
		return new CairoLockFailureStrategy();
	}

	@Bean
	@Primary
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public LockKeyBuilder lockKeyBuilder(BeanFactory beanFactory) {
		return new DefaultLockKeyBuilder(beanFactory);
	}

	@Bean
	AccountAuthKeyBuilder accountAuthKeyBuilder() {
		return new AccountAuthKeyBuilder();
	}

	@Bean
	AppUserAuthKeyBuilder appUserAuthKeyBuilder() {
		return new AppUserAuthKeyBuilder();
	}

	@Bean
	SubappUserAuthKeyBuilder subappUserAuthKeyBuilder() {
		return new SubappUserAuthKeyBuilder();
	}

	@Bean
	TenantAppUserAuthKeyBuilder tenantAppUserAuthKeyBuilder() {
		return new TenantAppUserAuthKeyBuilder();
	}

	@Bean
	TenantSubappUserAuthKeyBuilder tenantSubappUserAuthKeyBuilder() {
		return new TenantSubappUserAuthKeyBuilder();
	}

}
