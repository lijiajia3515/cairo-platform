package io.github.lijiajia3515.cairo.sba.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
	@Bean
	SecurityFilterChain mySecurity(HttpSecurity security, AdminServerProperties properties) throws Exception {
		SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setTargetUrlParameter("redirectTo");
		security
			.csrf(config -> config.disable())

			.cors(config -> config.disable())

			.authorizeHttpRequests(requests -> {
				requests
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.requestMatchers("/favicon.ico").permitAll()
					.requestMatchers("/actuator/**").permitAll()
					.requestMatchers("/static/**", "/resource/**", "/public/**", "/assets/**").permitAll()
					.requestMatchers(properties.getContextPath() + "/login").permitAll()
					.anyRequest().authenticated();
			})

			.httpBasic(config -> {
				config.realmName("cairo");
			})

			.formLogin(config -> {
				config.permitAll()
					.loginPage(properties.getContextPath().concat("/login"));
			})

			.logout(config-> {
				config.logoutUrl(properties.getContextPath().concat("/logout"));
			})

			.rememberMe(config -> {
				config.alwaysRemember(true);
			})

			.sessionManagement(config-> {

			})

			.exceptionHandling(c -> {
			});
		return security.build();
	}
}
