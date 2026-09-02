package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.modules.link.LinkProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短链配置类
 */
@Configuration(proxyBeanMethods = false)
public class LinkConfig {

	@Bean
	@RefreshScope
	@ConfigurationProperties("link")
	public LinkProperties linkProperties() {
		return new LinkProperties();
	}
}
