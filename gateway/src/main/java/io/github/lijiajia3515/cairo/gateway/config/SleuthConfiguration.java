package io.github.lijiajia3515.cairo.gateway.config;

import io.github.lijiajia3515.cairo.gateway.framework.sleuth.TraceResponseHeaderWebFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.actuate.autoconfigure.tracing.NoopTracerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = NoopTracerAutoConfiguration.class)
public class SleuthConfiguration {

	@Bean
	public TraceResponseHeaderWebFilter traceIdResponseFilter(Tracer tracer) {
		return new TraceResponseHeaderWebFilter(tracer);
	}
}
