package io.github.lijiajia3515.cairo.gateway.framework.webflux;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CairoWebfluxResponseHandler {
	private final CairoWebfluxResponseContext responseContext;

	public CairoWebfluxResponseHandler(CairoWebfluxResponseContext responseContext) {
		this.responseContext = responseContext;
	}

	public Mono<Void> handler(ServerWebExchange exchange, HttpStatus status, Object body) {
		return renderResponse(status, body).flatMap(response -> write(exchange, response))
			.flatMap(g->exchange.getResponse().setComplete());
	}

	protected Mono<ServerResponse> renderResponse(HttpStatus status, Object body) {
		return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON_UTF8).body(BodyInserters.fromValue(body));
	}

	private Mono<? extends Void> write(ServerWebExchange exchange, ServerResponse response) {
		// force content-type since writeTo won't overwrite response header values
		exchange.getResponse().getHeaders().setContentType(response.headers().getContentType());
		return response.writeTo(exchange, responseContext);
	}
}
