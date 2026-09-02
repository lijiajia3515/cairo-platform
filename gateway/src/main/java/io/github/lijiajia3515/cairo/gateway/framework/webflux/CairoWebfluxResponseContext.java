package io.github.lijiajia3515.cairo.gateway.framework.webflux;

import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;

public class CairoWebfluxResponseContext implements ServerResponse.Context {

	private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();

	private List<ViewResolver> viewResolvers = Collections.emptyList();

	public CairoWebfluxResponseContext() {
	}

	public CairoWebfluxResponseContext(List<HttpMessageWriter<?>> messageWriters, List<ViewResolver> viewResolvers) {
		this.messageWriters = messageWriters;
		this.viewResolvers = viewResolvers;
	}

	@Override
	public List<HttpMessageWriter<?>> messageWriters() {
		return messageWriters;
	}

	@Override
	public List<ViewResolver> viewResolvers() {
		return viewResolvers;
	}
}
