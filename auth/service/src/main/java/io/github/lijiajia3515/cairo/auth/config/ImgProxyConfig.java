package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.framework.imgproxy.ImgProxyProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ImgProxyConfig {

	@Bean
	@ConfigurationProperties(prefix = "imgproxy")
	ImgProxyProperties imgProxyProperties() {
		return new ImgProxyProperties();
	}

}
