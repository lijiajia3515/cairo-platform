package io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.rewrite;

import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayResult;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class GatewayBusinessResultFunction implements RewriteFunction<Object, Object> {
	@Override
	public Publisher<Object> apply(ServerWebExchange exchange, Object o) {
		HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
		if (!HttpStatus.OK.equals(statusCode) && o != null) {
			return Mono.justOrEmpty(o);
		}
		return Mono.just(GatewayResult.builder()
			.code(DefaultBusiness.SUCCESS.code)
			.message(DefaultBusiness.SUCCESS.message)
			.data(o)
			.requestId((String) exchange.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE))
			.build());
	}
}
