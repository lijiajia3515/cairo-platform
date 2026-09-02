package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.TenantSubapp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantSubappClientApiServiceImpl implements TenantSubappClientApiService {

	private final TenantSubappApiClientFeignClient tenantSubappApiClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantSubappClientApiServiceImpl(TenantSubappApiClientFeignClient tenantSubappApiClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantSubappApiClientFeignClient = tenantSubappApiClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<TenantSubapp> getTenantSubappList(GetTenantSubappArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantSubapp>>> tenantSubappResponseEntity =  tenantSubappApiClientFeignClient.getTenantSubappList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(tenantSubappResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("tenantSubappResponseEntity error", e);
			throw e;
		}
	}
}
