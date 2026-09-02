package io.github.lijiajia3515.cairo.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.blocking.retry.BlockingLoadBalancedRetryFactory;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;

public class BackoffBlockingLoadBalancerRetryFactory extends BlockingLoadBalancedRetryFactory {
	protected final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

	public BackoffBlockingLoadBalancerRetryFactory(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
		super(loadBalancerFactory);
		this.loadBalancerFactory = loadBalancerFactory;
	}

	@Override
	public BackOffPolicy createBackOffPolicy(String serviceId) {
		LoadBalancerProperties properties = loadBalancerFactory.getProperties(serviceId);
		LoadBalancerProperties.Retry retry = properties.getRetry();
		if (!retry.getBackoff().isEnabled()) {
			return super.createBackOffPolicy(serviceId);
		}
		ExponentialRandomBackOffPolicy randomBackOffPolicy = new ExponentialRandomBackOffPolicy();
		randomBackOffPolicy.setInitialInterval(retry.getBackoff().getMinBackoff().toMillis());
		randomBackOffPolicy.setMaxInterval(retry.getBackoff().getMaxBackoff().toMillis());
		randomBackOffPolicy.setMultiplier(retry.getBackoff().getJitter());
		return randomBackOffPolicy;
	}
}
