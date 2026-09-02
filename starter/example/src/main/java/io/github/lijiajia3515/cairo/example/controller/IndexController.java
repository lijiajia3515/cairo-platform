package io.github.lijiajia3515.cairo.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.PermitAll;

@Slf4j
@RestController
public class IndexController {

	@GetMapping("/test1")
	public Object test1() {
		log.info("test1");
		return "ABCDE";
	}

	@GetMapping("/test2")
	@PermitAll
	public Object test2() {
		return 1 / 0;
	}
}
