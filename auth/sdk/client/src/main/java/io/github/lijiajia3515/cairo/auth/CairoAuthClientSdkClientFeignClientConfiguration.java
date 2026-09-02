package io.github.lijiajia3515.cairo.auth;

import io.github.lijiajia3515.cairo.auth.framework.feign.signv1.CairoFeignSignV1Interceptor;
import org.springframework.context.annotation.Bean;

public class CairoAuthClientSdkClientFeignClientConfiguration {

	@Bean
	public CairoFeignSignV1Interceptor cairoFeignSignV1Interceptor() {
		return new CairoFeignSignV1Interceptor();
	}
}
