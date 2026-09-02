package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebofficeConfig {

	@Bean
	@ConfigurationProperties(prefix = "cairo.weboffice")
	public WebofficeProperties wpsProperties() {
		return new WebofficeProperties();
	}
}
