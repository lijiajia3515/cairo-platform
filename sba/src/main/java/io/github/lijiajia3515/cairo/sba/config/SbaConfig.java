package io.github.lijiajia3515.cairo.sba.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration(proxyBeanMethods = false)
public class SbaConfig {
	@Bean
	public DateTimeFormatter sbaDate(){
		//LocalDateTime.from(Instant.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	}
}
