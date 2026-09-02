package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantAppUserClientApiServiceImpl implements TenantAppUserClientApiService {

	private final TenantAppUserClientApiFeignClient tenantAppUserClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppUserClientApiServiceImpl(TenantAppUserClientApiFeignClient tenantAppUserClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppUserClientApiFeignClient = tenantAppUserClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<TenantAppUser> getTenantAppUserList(GetTenantAppUserArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantAppUser>>> tenantAppUserList = tenantAppUserClientApiFeignClient.getTenantAppUserList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(tenantAppUserList.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("tenantAppUserList error", e);
			throw e;
		}
	}

	@Override
	public Page<TenantAppUser> getTenantAppUserPageList(GetTenantAppUserArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<TenantAppUser>>> tenantAppUserPageList = tenantAppUserClientApiFeignClient.getTenantAppUserPageList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(tenantAppUserPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantAppUserPageList error", e);
			throw e;
		}
	}

	@Override
	public TenantAppUserAuthModel getTenantAppUserAuth(GetTenantAppUserAuthArgs args) {
		try {
			ResponseEntity<BusinessResult<TenantAppUserAuthModel>> tenantAppUserAuth = tenantAppUserClientApiFeignClient.getTenantAppUserAuth(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(tenantAppUserAuth.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantAppUserAuth error", e);
			throw e;
		}
	}

	@Override
	public List<TenantAppUser> getTenantAppUserSubDepartmentList(GetTenantAppUserArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantAppUser>>> tenantAppUserSubDepartmentList = tenantAppUserClientApiFeignClient.getTenantAppUserSubDepartmentList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(tenantAppUserSubDepartmentList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("tenantAppUserAuth error", e);
			throw e;
		}
	}
}
