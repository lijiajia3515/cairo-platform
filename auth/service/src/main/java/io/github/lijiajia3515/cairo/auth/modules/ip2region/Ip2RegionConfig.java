package io.github.lijiajia3515.cairo.auth.modules.ip2region;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;


/**
 * region转换
 */
@Configuration(proxyBeanMethods = false)
public class Ip2RegionConfig {

	@Bean
	@ConfigurationProperties(prefix = "ip2region")
	public Ip2RegionProperties ip2RegionProperties() {
		return new Ip2RegionProperties();
	}

	@Bean
	Ip2RegionService ip2RegionService(ObjectProvider<Ip2RegionProperties> properties) {
		return new Ip2RegionService(Optional.ofNullable(properties.getIfAvailable()).map(Ip2RegionProperties::getDbPath).orElse(null));
	}
}
