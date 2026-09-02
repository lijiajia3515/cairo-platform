package io.github.lijiajia3515.cairo.auth.framework.feign;

import io.github.lijiajia3515.cairo.auth.framework.feign.signv1.CairoFeignSignV1Interceptor;
import io.github.lijiajia3515.cairo.feign.interceptor.RequestAuthorizationRequestInterceptor;
import org.springframework.context.annotation.Bean;

public class CairoRequestFeignClientConfiguration {
	@Bean
	public CairoFeignSignV1Interceptor cairoFeignSignV1Interceptor(){
		return new CairoFeignSignV1Interceptor();
	}

	@Bean
	RequestAuthorizationRequestInterceptor requestAuthorizationRequestInterceptor() {
		return new RequestAuthorizationRequestInterceptor();
	}

}
