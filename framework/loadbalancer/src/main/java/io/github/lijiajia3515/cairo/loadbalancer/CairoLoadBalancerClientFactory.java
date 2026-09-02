package io.github.lijiajia3515.cairo.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClientsProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;

public class CairoLoadBalancerClientFactory extends LoadBalancerClientFactory {
	public CairoLoadBalancerClientFactory(LoadBalancerClientsProperties properties) {
		super(properties);
	}
	@Override
	public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
		return getInstance(serviceId, CairoServiceInstanceLoadBalancer.class);
	}
}
