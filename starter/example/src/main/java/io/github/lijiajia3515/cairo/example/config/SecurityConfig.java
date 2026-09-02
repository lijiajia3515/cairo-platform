package io.github.lijiajia3515.cairo.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(config -> {
				config.disable();
			})
			.authorizeHttpRequests(config -> {
				config.anyRequest().permitAll();
			});

		return http.build();
	}
}
