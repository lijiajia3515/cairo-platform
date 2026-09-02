package io.github.lijiajia3515.cairo.auth.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;


/**
 * 跨域配置
 */
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOriginPatterns(List.of("http://*","https://*"));
		// 允许所有公开请求头 跨域
		cors.setExposedHeaders(List.of("*"));
		// 允许 所有请求头跨域
		cors.setAllowedHeaders(List.of("*"));
		// 允许 所有方法跨域
		cors.setAllowedMethods(List.of("*"));
		// 允许携带凭证
		cors.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/oauth2/**", cors);
		source.registerCorsConfiguration("/api/**", cors);
		source.registerCorsConfiguration("/.well-known/**", cors);
		source.registerCorsConfiguration("/**", cors); // TODO 待删除
		return source;
	}

	/**
	 * Spring Security 各过滤器链均 cors().disable()，且 /open_api/** 等路径被 web.ignoring() 放行，
	 * 不会经过 Security 的 CorsFilter。此处注册独立的 CorsFilter，在 Security 之前处理跨域，
	 * 保证被忽略的路径同样返回跨域响应头。
	 */
	@Bean
	FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsConfigurationSource corsConfigurationSource) {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registration.addUrlPatterns("/*");
		return registration;
	}
}
