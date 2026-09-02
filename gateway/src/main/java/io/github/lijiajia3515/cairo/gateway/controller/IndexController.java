package io.github.lijiajia3515.cairo.gateway.controller;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import jakarta.annotation.security.PermitAll;
import java.util.Collections;
import java.util.Map;

@Controller
public class IndexController {

	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	@PermitAll
	@NewSpan("indexView")
	public ResponseEntity<String> indexView() {
		return ResponseEntity.ok("<h1>Cairo Gateway Service</h1>");
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseBody
	public Mono<Map<String, String>> hello() {
		return Mono.just(Collections.singletonMap("hello", "hello"));
	}
}
