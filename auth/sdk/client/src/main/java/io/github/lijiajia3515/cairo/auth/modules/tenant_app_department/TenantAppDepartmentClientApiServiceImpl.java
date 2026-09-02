package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TenantAppDepartmentClientApiServiceImpl implements TenantAppDepartmentClientApiService {

	private final TenantAppDepartmentApiClientFeignClient tenantAppDepartmentApiClientFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public TenantAppDepartmentClientApiServiceImpl(TenantAppDepartmentApiClientFeignClient tenantAppDepartmentApiClientFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.tenantAppDepartmentApiClientFeignClient = tenantAppDepartmentApiClientFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}


	@Override
	public List<TenantAppDepartment> getDepartmentList(GetDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantAppDepartment>>> tenantDepartmentResponseEntity =  tenantAppDepartmentApiClientFeignClient.getTenantAppDepartmentList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(tenantDepartmentResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.error("tenantDepartmentResponseEntity error", e);
			throw e;
		}
	}

	@Override
	public Page<TenantAppDepartment> getDepartmentPageList(GetDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<Page<TenantAppDepartment>>> departmentPageList = tenantAppDepartmentApiClientFeignClient.getTenantAppDepartmentPageList(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
				return Optional.ofNullable(departmentPageList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("departmentPageList error", e);
			throw e;
		}
	}

	@Override
	public List<PathTenantAppDepartment> getPathTenantAppDepartmentList(GetDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<List<PathTenantAppDepartment>>> pathTenantAppDepartmentList = tenantAppDepartmentApiClientFeignClient.getPathTenantAppDepartmentList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(pathTenantAppDepartmentList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("departmentPageList error", e);
			throw e;
		}
	}

	@Override
	public List<TenantAppDepartment> getTenantAppSubDepartmentList(GetDepartmentArgs args) {
		try {
			ResponseEntity<BusinessResult<List<TenantAppDepartment>>> tenantAppSubDepartmentList = tenantAppDepartmentApiClientFeignClient.getTenantAppSubDepartmentList(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(tenantAppSubDepartmentList.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.error("getTenantAppSubDepartmentList error", e);
			throw e;
		}
	}
}
