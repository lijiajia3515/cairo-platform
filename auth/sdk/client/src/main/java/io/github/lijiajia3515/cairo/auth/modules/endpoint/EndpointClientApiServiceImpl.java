package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class EndpointClientApiServiceImpl implements EndpointClientApiService {

	private final EndpointClientApiFeignClient endpointClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public EndpointClientApiServiceImpl(EndpointClientApiFeignClient endpointClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.endpointClientApiFeignClient = endpointClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<Endpoint> getEndpointList(GetEndpointClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Endpoint>>> endpointList = endpointClientApiFeignClient.getEndpointList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(endpointList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("endpointList error", e);
			throw e;
		}
	}

	@Override
	public Page<Endpoint> getEndpointPageList(GetEndpointClientArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<Endpoint>>> endpointPageList = endpointClientApiFeignClient.getEndpointPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(endpointPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("endpointPageList error", e);
			throw e;
		}
	}

	@Override
	public List<Endpoint> getEndpointByAppList(GetEndpointByAppClientArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Endpoint>>> endpointByAppList = endpointClientApiFeignClient.getEndpointByAppList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(endpointByAppList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("appDepartmentList error", e);
			throw e;
		}
	}
}
