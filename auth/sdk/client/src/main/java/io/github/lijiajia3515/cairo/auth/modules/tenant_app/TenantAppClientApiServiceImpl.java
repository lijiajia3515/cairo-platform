package io.github.lijiajia3515.cairo.auth.modules.tenant_app;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantAppClientApiServiceImpl implements TenantAppClientApiService {

	private final TenantAppClientApiFeignClient tenantAppClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppClientApiServiceImpl(TenantAppClientApiFeignClient tenantAppClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppClientFeignClient = tenantAppClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<TenantApp> getTenantAppList(GetTenantAppArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantApp>>> tenantAppList = tenantAppClientFeignClient.getTenantAppList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(tenantAppList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantAppList error", e);
			throw e;
		}
	}

	@Override
	public Page<TenantApp> getTenantAppPageList(GetTenantAppArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<TenantApp>>> tenantAppPageList = tenantAppClientFeignClient.getTenantAppPageList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(tenantAppPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantAppPageList error", e);
			throw e;
		}
	}
}
