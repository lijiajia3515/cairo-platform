
package io.github.lijiajia3515.cairo.gateway.framework.gateway.filter.factory;

import io.github.lijiajia3515.cairo.core.business.RequestBusiness;
import io.github.lijiajia3515.cairo.gateway.framework.CairoWebExchangeUtils;
import io.github.lijiajia3515.cairo.gateway.framework.webflux.CairoWebfluxResponseHandler;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayErrorBusinessResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.cloud.gateway.support.HttpStatusHolder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * User Request Rate Limiter filter. @See <a href="https://stripe.com/blog/rate-limiters">...</a> and
 * <a href="https://gist.github.com/ptarjan/e38f45f2dfe601419ca3af937fff574d#file-1-check_request_rate_limiter-rb-L11-L34">...</a>.
 */
public class CairoPrincipalRequestRateLimiterGatewayFilterFactory
	extends AbstractGatewayFilterFactory<CairoPrincipalRequestRateLimiterGatewayFilterFactory.Config> {

	/**
	 * Key-Resolver key.
	 */
	public static final String KEY_RESOLVER_KEY = "keyResolver";

	private static final String EMPTY_KEY = "____EMPTY_KEY__";

	private final RateLimiter defaultRateLimiter;

	private final KeyResolver defaultKeyResolver;

	/**
	 * Switch to deny requests if the Key Resolver returns an empty key, defaults to true.
	 */
	private boolean denyEmptyKey = true;

	/**
	 * HttpStatus to return when denyEmptyKey is true, defaults to FORBIDDEN.
	 */
	private String emptyKeyStatusCode = HttpStatus.FORBIDDEN.name();

	private final CairoWebfluxResponseHandler responseHandler;

	private int order = -50;

	public CairoPrincipalRequestRateLimiterGatewayFilterFactory(RateLimiter defaultRateLimiter, KeyResolver defaultKeyResolver, CairoWebfluxResponseHandler responseHandler, int order) {
		super(Config.class);
		this.defaultRateLimiter = defaultRateLimiter;
		this.defaultKeyResolver = defaultKeyResolver;
		this.responseHandler = responseHandler;
		this.order = order;
	}

	public KeyResolver getDefaultKeyResolver() {
		return defaultKeyResolver;
	}

	public RateLimiter getDefaultRateLimiter() {
		return defaultRateLimiter;
	}

	public boolean isDenyEmptyKey() {
		return denyEmptyKey;
	}

	public void setDenyEmptyKey(boolean denyEmptyKey) {
		this.denyEmptyKey = denyEmptyKey;
	}

	public String getEmptyKeyStatusCode() {
		return emptyKeyStatusCode;
	}

	public void setEmptyKeyStatusCode(String emptyKeyStatusCode) {
		this.emptyKeyStatusCode = emptyKeyStatusCode;
	}

	@Override
	public GatewayFilter apply(Config config) {
		KeyResolver resolver = getOrDefault(config.keyResolver, defaultKeyResolver);
		RateLimiter<Object> limiter = getOrDefault(config.rateLimiter, defaultRateLimiter);
		boolean denyEmpty = getOrDefault(config.denyEmptyKey, this.denyEmptyKey);
		HttpStatusHolder emptyKeyStatus = HttpStatusHolder.parse(getOrDefault(config.emptyKeyStatus, this.emptyKeyStatusCode));


		GatewayFilter filter = (exchange, chain) -> resolver.resolve(exchange).defaultIfEmpty(EMPTY_KEY).flatMap(key -> {


			if (EMPTY_KEY.equals(key)) {
				return chain.filter(exchange);
			}

			GatewayErrorBusinessResult<Object> responseEntity = GatewayErrorBusinessResult.builder()
				.code(RequestBusiness.LIMIT_EXCEEDED.getCode())
				.message(RequestBusiness.LIMIT_EXCEEDED.getMessage())
				.requestId((String) exchange.getAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE))
				.retryable(true)
				.build();

			String routeId = config.getRouteId();
			if (routeId == null) {
				Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
				routeId = route.getId();
			}
			return limiter.isAllowed(routeId, key).flatMap(response -> {

				for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
					exchange.getResponse().getHeaders().add(header.getKey(), header.getValue());
				}

				if (response.isAllowed()) {
					return chain.filter(exchange);
				}
				return responseHandler.handler(exchange, config.getStatusCode(), responseEntity);
			});
		});
		return new OrderedGatewayFilter(filter, order);
	}

	private <T> T getOrDefault(T configValue, T defaultValue) {
		return (configValue != null) ? configValue : defaultValue;
	}

	public static class Config implements HasRouteId {

		private KeyResolver keyResolver;

		private RateLimiter rateLimiter;

		private HttpStatus statusCode = HttpStatus.TOO_MANY_REQUESTS;

		private Boolean denyEmptyKey;

		private String emptyKeyStatus;

		private String routeId;

		public KeyResolver getKeyResolver() {
			return keyResolver;
		}

		public Config setKeyResolver(KeyResolver keyResolver) {
			this.keyResolver = keyResolver;
			return this;
		}

		public RateLimiter getRateLimiter() {
			return rateLimiter;
		}

		public Config setRateLimiter(RateLimiter rateLimiter) {
			this.rateLimiter = rateLimiter;
			return this;
		}

		public HttpStatus getStatusCode() {
			return statusCode;
		}

		public Config setStatusCode(HttpStatus statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Boolean getDenyEmptyKey() {
			return denyEmptyKey;
		}

		public Config setDenyEmptyKey(Boolean denyEmptyKey) {
			this.denyEmptyKey = denyEmptyKey;
			return this;
		}

		public String getEmptyKeyStatus() {
			return emptyKeyStatus;
		}

		public Config setEmptyKeyStatus(String emptyKeyStatus) {
			this.emptyKeyStatus = emptyKeyStatus;
			return this;
		}

		@Override
		public void setRouteId(String routeId) {
			this.routeId = routeId;
		}

		@Override
		public String getRouteId() {
			return this.routeId;
		}

	}

}
