package io.github.lijiajia3515.cairo.auth.modules.tenant;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantClientApiServiceImpl implements TenantClientApiService {

	private final TenantClientApiFeignClient tenantClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantClientApiServiceImpl(TenantClientApiFeignClient tenantClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantClientFeignClient = tenantClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<Tenant> getTenantList(GetTenantArgs args) {
		try {
			ResponseEntity<BusinessResult<List<Tenant>>> tenantList = tenantClientFeignClient.getTenantList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(tenantList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantList error", e);
			throw e;
		}
	}

	@Override
	public Tenant getTenantInfo(GetTenantInfoArgs args) {
		try {
			ResponseEntity<BusinessResult<Tenant>> tenantInfo = tenantClientFeignClient.getTenantInfo(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(tenantInfo.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantInfo error", e);
			throw e;
		}
	}
}
