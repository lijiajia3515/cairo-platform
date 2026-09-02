package io.github.lijiajia3515.cairo.gateway.framework.sleuth;


import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TraceResponseHeaderWebFilter implements WebFilter, Ordered {

	private final Tracer tracer;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		Span span = tracer.currentSpan();
		if (span != null && !exchange.getResponse().isCommitted()) {
			exchange.getAttributes().put(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE, span.context().traceId());
			exchange.getResponse().getHeaders().add("X-Trace-Id", span.context().traceId());
		}

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 6;

	}
}
