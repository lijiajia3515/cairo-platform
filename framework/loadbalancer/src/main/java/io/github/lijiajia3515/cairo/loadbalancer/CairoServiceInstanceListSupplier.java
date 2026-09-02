package io.github.lijiajia3515.cairo.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 灰度服务实例筛选
 *
 * @author javadaily
 * 参考：org.springframework.cloud.loadbalancer.core.ZonePreferenceServiceInstanceListSupplier
 */
@Slf4j
public class CairoServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {
	private static final String HEADER_NAME = "x-cairo-tag";

	public CairoServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
		super(delegate);
	}

	@Override
	public Flux<List<ServiceInstance>> get() {
		return delegate.get();
	}

	@Override
	public Flux<List<ServiceInstance>> get(Request request) {
		return delegate.get(request).map(instances -> filteredByTag(instances, getTag(request.getContext())));
	}

	/**
	 * filter instance by requestTag
	 *
	 * @author javadaily
	 */
	private List<ServiceInstance> filteredByTag(List<ServiceInstance> instances, String requestTag) {
		log.info("request cairo-tag is {}", requestTag);
		List<ServiceInstance> filterInstances;
		instances.forEach(x -> {
			log.info("{}", x.getMetadata());
		});
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
		}
		return tag;
	}

	/**
	 * get tag from header
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
}
