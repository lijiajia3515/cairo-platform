package io.github.lijiajia3515.cairo.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.feign.error.CairoErrorDecoder;
import io.github.lijiajia3515.cairo.feign.error.DefaultErrorDecoder;
import feign.Client;
import feign.Target;
import feign.okhttp.OkHttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.List;

@EnableFeignClients(basePackages = {
	"io.github.lijiajia3515.cairo.**.modules"
})
@Configuration(proxyBeanMethods = false)
public class FeignConfig {

	@Bean
	public CircuitBreakerNameResolver circuitBreakerNameResolver() {
		return (String feignClientName, Target<?> target, Method method) -> feignClientName + "_" + method.getName();
	}

	@Bean
	public Client feignRetryClient(okhttp3.OkHttpClient okHttpClient,
								   LoadBalancerClient loadBalancerClient,
								   LoadBalancedRetryFactory loadBalancedRetryFactory,
								   LoadBalancerClientFactory loadBalancerClientFactory,
								   List<LoadBalancerFeignRequestTransformer> transformers) {
		OkHttpClient delegate = new OkHttpClient(okHttpClient);
		return new RetryableFeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancedRetryFactory, loadBalancerClientFactory, transformers);
	}

	@Bean
	CairoErrorDecoder cairoErrorDecoder(ObjectMapper objectMapper) {
		return new CairoErrorDecoder(objectMapper, new DefaultErrorDecoder());
	}
}
