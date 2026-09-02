package io.github.lijiajia3515.cairo.feign.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		log.debug("basic request interceptor");
	}
}
