package io.github.lijiajia3515.cairo.sba.framework;

import de.codecentric.boot.admin.server.cloud.discovery.DefaultServiceInstanceConverter;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.util.StringUtils.hasText;

public class ConsulServiceInstanceConverter extends DefaultServiceInstanceConverter {
	private static final String KEY_MANAGEMENT_SCHEME = "management_scheme";

	private static final String KEY_MANAGEMENT_ADDRESS = "management_address";

	private static final String KEY_MANAGEMENT_PORT = "management_port";

	private static final String KEY_MANAGEMENT_PATH = "management_context-path";

	private static final String KEY_HEALTH_PATH = "health_path";

	public ConsulServiceInstanceConverter() {
		super();
	}

	@Override
	protected URI getManagementUrl(ServiceInstance instance) {
		URI serviceUrl = this.getServiceUrl(instance);
		String managementScheme = this.getManagementScheme(instance);
		String managementHost = this.getManagementHost(instance);
		int managementPort = this.getManagementPort(instance);

		UriComponentsBuilder builder;
		if (serviceUrl.getHost().equals(managementHost) && serviceUrl.getScheme().equals(managementScheme)
			&& serviceUrl.getPort() == managementPort) {
			builder = UriComponentsBuilder.fromUri(serviceUrl);
		}
		else {
			builder = UriComponentsBuilder.newInstance().scheme(managementScheme).host(managementHost);
			if (managementPort != -1) {
				builder.port(managementPort);
			}
		}

		return builder.path("/").path(getManagementPath(instance)).build().toUri();
	}

	@Override
	protected String getHealthPath(ServiceInstance instance) {
		if (instance instanceof ConsulServiceInstance) {
			String healthPath = instance.getMetadata().get(KEY_HEALTH_PATH);
			if (hasText(healthPath)) {
				return healthPath;
			}
		}
		return super.getHealthPath(instance);
	}

	protected String getManagementScheme(ServiceInstance instance) {
		if (instance instanceof ConsulServiceInstance) {
			String managementServerScheme = instance.getMetadata().get(KEY_MANAGEMENT_SCHEME);
			if (hasText(managementServerScheme)) {
				return managementServerScheme;
			}
		}

		return getServiceUrl(instance).getScheme();
	}

	@Override
	protected String getManagementHost(ServiceInstance instance) {
		if (instance instanceof ConsulServiceInstance) {
			String managementServerHost = instance.getMetadata().get(KEY_MANAGEMENT_ADDRESS);
			if (hasText(managementServerHost)) {
				return managementServerHost;
			}
		}
		return getServiceUrl(instance).getHost();
	}

	@Override
	protected int getManagementPort(ServiceInstance instance) {
		if (instance instanceof ConsulServiceInstance) {
			String managementPort = instance.getMetadata().get(KEY_MANAGEMENT_PORT);
			if (hasText(managementPort)) {
				return Integer.parseInt(managementPort);
			}
		}
		return super.getManagementPort(instance);
	}

	@Override
	protected String getManagementPath(ServiceInstance instance) {
		if (instance instanceof ConsulServiceInstance) {
			String managementServerHost = instance.getMetadata().get(KEY_MANAGEMENT_PATH);
			if (hasText(managementServerHost)) {
				return managementServerHost;
			}
		}
		return super.getManagementPath(instance);
	}
}
