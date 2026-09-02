package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantEndpointClientApiServiceImpl implements TenantEndpointClientApiService {

	private final TenantEndpointApiClientFeignClient tenantEndpointApiClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantEndpointClientApiServiceImpl(TenantEndpointApiClientFeignClient tenantEndpointApiClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantEndpointApiClientFeignClient = tenantEndpointApiClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<TenantEndpoint> getCurrentEndpointList(GetCurrentEndpointArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantEndpoint>>> tenantEndpointResponseEntity =  tenantEndpointApiClientFeignClient.getCurrentEndpointList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(tenantEndpointResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("tenantEndpointResponseEntity error", e);
			throw e;
		}
	}
}
