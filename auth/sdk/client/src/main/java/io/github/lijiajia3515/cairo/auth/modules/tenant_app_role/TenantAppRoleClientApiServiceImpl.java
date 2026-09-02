package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantAppRoleClientApiServiceImpl implements TenantAppRoleApiService {

	private final TenantAppRoleApiClientFeignClient tenantAppRoleApiClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppRoleClientApiServiceImpl(TenantAppRoleApiClientFeignClient tenantAppRoleApiClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppRoleApiClientFeignClient = tenantAppRoleApiClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<MetadataTenantAppRole> getTenantAppRoleList(GetTenantAppRoleArgs args) {
		try {
			ResponseEntity<BusinessResult<List<MetadataTenantAppRole>>> roleList = tenantAppRoleApiClientFeignClient.getRoleList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(roleList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("roleList error", e);
			throw e;
		}
	}

	@Override
	public Page<MetadataTenantAppRole> getTenantAppRolePageList(GetTenantAppRoleArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<MetadataTenantAppRole>>> rolePageList = tenantAppRoleApiClientFeignClient.getRolePageList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(rolePageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("rolePageList error", e);
			throw e;
		}
	}
}
