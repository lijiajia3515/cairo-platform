package io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.requestratelimiter;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

public class PrincipalKeyResolver implements KeyResolver {
	private static final String PREFIX = "principal_";

	private static final List<String> DEFAULT_PRINCIPAL_HEADER_NAMES = List.of("x-cairo-user-id", "x-cairo-account-Id", "x-cairo-client-id", "x-cairo-token-id");

	private List<String> principalHeaderNames = DEFAULT_PRINCIPAL_HEADER_NAMES;


	@Override
	public Mono<String> resolve(ServerWebExchange exchange) {
		return principalHeaderNames.stream()
			.flatMap(x -> exchange.getRequest().getHeaders().getOrDefault(x, Collections.emptyList()).stream())
			.filter(x->x != null && !x.isBlank())
			.findFirst()
			.map(PREFIX::concat)
			.map(Mono::just)
			.orElse(Mono.empty());
	}

	public List<String> getPrincipalHeaderNames() {
		return principalHeaderNames;
	}

	public void setPrincipalHeaderNames(List<String> headerNames) {
		this.principalHeaderNames = headerNames;
	}
}
