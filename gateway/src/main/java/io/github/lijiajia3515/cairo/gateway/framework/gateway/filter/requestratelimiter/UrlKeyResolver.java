package io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.requestratelimiter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class UrlKeyResolver implements KeyResolver {
	@Override
	public Mono<String> resolve(ServerWebExchange exchange) {
		return Mono.just("url_".concat(exchange.getRequest().getURI().getPath()));
	}
}
