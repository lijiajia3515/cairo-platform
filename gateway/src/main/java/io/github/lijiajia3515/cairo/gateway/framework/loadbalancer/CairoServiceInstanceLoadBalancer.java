package io.github.lijiajia3515.cairo.gateway.framework.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 自定义负载均衡器
 *
 * @see org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer
 */
@Slf4j
public class CairoServiceInstanceLoadBalancer implements ReactorServiceInstanceLoadBalancer {
	private static final String HEADER_NAME = "x-cairo-tag";
	private static final String PARAMETER_NAME = "cairo_tag";

	private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
	private final String serviceId;

	private final AtomicInteger position;

	public CairoServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
		this(serviceInstanceListSupplierProvider, serviceId, new Random().nextInt(1000));
	}

	public CairoServiceInstanceLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
											String serviceId, int seedPosition) {
		this.serviceId = serviceId;
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		this.position = new AtomicInteger(seedPosition);
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
		return supplier.get(request).next()
			.map(serviceInstances -> processInstanceResponse(request, supplier, serviceInstances));

	}

	private Response<ServiceInstance> processInstanceResponse(Request request, ServiceInstanceListSupplier supplier,
															  List<ServiceInstance> serviceInstances) {
		Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(request, serviceInstances);
		if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
			((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
		}
		return serviceInstanceResponse;
	}

	private Response<ServiceInstance> getInstanceResponse(Request request, List<ServiceInstance> instances) {

		if (instances.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("No servers available for service: " + serviceId);
			}
			return new EmptyResponse();
		}

		// Do not move position when there is only 1 instance, especially some suppliers
		// have already filtered instances
		if (instances.size() == 1) {
			return new DefaultResponse(instances.get(0));
		}

		// custom
		List<ServiceInstance> filterServiceInstances = filteredByTag(instances, getTag(request.getContext()));


		// Ignore the sign bit, this allows pos to loop sequentially from 0 to
		// Integer.MAX_VALUE
		int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;

		ServiceInstance instance = filterServiceInstances.get(pos % filterServiceInstances.size());

		return new DefaultResponse(instance);
	}

	/**
	 * filter instance by requestTag
	 *
	 * @author javadaily
	 */
	private List<ServiceInstance> filteredByTag(List<ServiceInstance> instances, String requestTag) {
		log.info("request cairo-tag is {}", requestTag);
		List<ServiceInstance> filterInstances;
		if (requestTag != null && !requestTag.isEmpty()) {
			filterInstances = instances.stream()
				.filter(instance -> requestTag.equalsIgnoreCase(instance.getMetadata().getOrDefault(HEADER_NAME, "")))
				.collect(Collectors.toList());
		} else {
			filterInstances = instances.stream()
				.filter(instance -> "".equalsIgnoreCase(instance.getMetadata().getOrDefault(HEADER_NAME, "")))
				.collect(Collectors.toList());
		}

		return filterInstances.isEmpty() ? instances : filterInstances;
	}

	private String getTag(Object requestContext) {
		if (requestContext == null) {
			return null;
		}
		String tag = null;
		if (requestContext instanceof RequestDataContext) {
			tag = getTagFormHeader((RequestDataContext) requestContext);
		} else  if (requestContext instanceof DefaultRequestContext){
			tag = getTagFormHeader((DefaultRequestContext) requestContext);
		}
		return tag;
	}

	/**
	 * get version from header
	 *
	 * @author javadaily
	 */
	private String getTagFormHeader(RequestDataContext context) {
		if (context.getClientRequest() != null) {
			HttpHeaders headers = context.getClientRequest().getHeaders();
			if (headers != null) {
				//could extract to the properties
				return headers.getFirst(HEADER_NAME);
			}
		}
		return null;
	}
	private String getTagFormHeader(DefaultRequestContext context) {
		if (context.getClientRequest() != null) {
			if (context.getClientRequest() instanceof RequestData) {
				HttpHeaders headers = ((RequestData) context.getClientRequest()).getHeaders();
				if (headers != null){
					return headers.getFirst(HEADER_NAME);
				}
			}
		}
		return null;
	}

}
