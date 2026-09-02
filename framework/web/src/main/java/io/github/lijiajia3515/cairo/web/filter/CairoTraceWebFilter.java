package io.github.lijiajia3515.cairo.web.filter;


import io.github.lijiajia3515.cairo.web.utils.CairoWebExchangeUtils;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;

import java.io.IOException;

@RequiredArgsConstructor
public class CairoTraceWebFilter implements Filter, Ordered {

	private final Tracer tracer;

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 6;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;
		Span span = tracer.currentSpan();
		if (span != null) {
			servletRequest.setAttribute(CairoWebExchangeUtils.REQUEST_ID_ATTRIBUTE, span.context().traceId());
			if (!response.isCommitted()) {
				servletResponse.setHeader("X-Trace-Id", span.context().traceId());
			}
		}

		chain.doFilter(request, response);
	}
}
