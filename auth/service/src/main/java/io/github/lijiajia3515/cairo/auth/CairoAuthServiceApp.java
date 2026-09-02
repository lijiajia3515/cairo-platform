package io.github.lijiajia3515.cairo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CairoAuthServiceApp {

	public static void main(String[] args) {
		SpringApplication.run(CairoAuthServiceApp.class, args);
	}

}
