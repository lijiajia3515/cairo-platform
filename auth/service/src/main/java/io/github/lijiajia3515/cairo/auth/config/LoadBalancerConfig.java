package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.loadbalancer.BackoffBlockingLoadBalancerRetryFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LoadBalancerConfig {
	// @Bean
	// @LoadBalanced
	// RestTemplate cairoLoadbalancerRestTemplate() {
	// 	return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
	// }

	@Bean
	BackoffBlockingLoadBalancerRetryFactory backoffBlockingLoadBalancerRetryFactory(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
		return new BackoffBlockingLoadBalancerRetryFactory(loadBalancerFactory);
	}

//	@Bean
//	@ConditionalOnBean(ReactiveDiscoveryClient.class)
//	@ConditionalOnMissingBean
//	ServiceInstanceListSupplier cairoServiceInstanceListSupplier(ConfigurableApplicationContext configurableApplicationContext){
//		return ServiceInstanceListSupplier.builder()
//			.withDiscoveryClient()
//			.withCaching()
//			.with((configurableApplicationContext1, serviceInstanceListSupplier) -> new CairoServiceInstanceListSupplier(serviceInstanceListSupplier))
//			.build(configurableApplicationContext);
//	}
}
