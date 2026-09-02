package io.github.lijiajia3515.cairo.auth.config;


import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProviderProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SnsProviderConfig {

	@Bean
	@ConfigurationProperties("sns-provider")
	SnsProviderProperties SnsProviderProperties() {
		return new SnsProviderProperties();
	}

}
