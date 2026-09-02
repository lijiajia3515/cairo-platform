package io.github.lijiajia3515.cairo.rabbitmq;

import java.util.Map;
import java.util.Optional;

public class CairoRabbitmqExchangeHelper {

	private static final String defaultConfig = "default";
	private final Map<String, CairoRabbitmqProperties.Exchange> config;

	public CairoRabbitmqExchangeHelper(Map<String, CairoRabbitmqProperties.Exchange> config) {
		this.config = config;
	}

	public String getName(CairoRabbitmqExchange exchange) {
		return Optional.ofNullable(exchange.getName())
			.or(() -> Optional.of(defaultConfig))
			.map(config::get)
			.map(CairoRabbitmqProperties.Exchange::getName)
			.orElse("cairo_default");
	}
}
