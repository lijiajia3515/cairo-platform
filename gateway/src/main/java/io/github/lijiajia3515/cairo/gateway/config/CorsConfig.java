package io.github.lijiajia3515.cairo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;

//@Configuration(proxyBeanMethods = false)
public class CorsConfig {

//	@Bean
	CorsWebFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("http://*");
		config.addAllowedOrigin("http*://*");
		config.addAllowedOriginPattern("chrome-extension://*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(Duration.ofMinutes(30));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsWebFilter(source);
	}
}
