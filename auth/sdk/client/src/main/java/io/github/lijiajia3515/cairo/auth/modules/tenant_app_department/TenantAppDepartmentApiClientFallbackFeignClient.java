package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api-department fallback feignclient
 */
public class TenantAppDepartmentApiClientFallbackFeignClient implements TenantAppDepartmentApiClientFeignClient {
	public static final RuntimeException EX = new ErrorBusinessException("认证服务-服务故障");

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppDepartment>>> getTenantAppDepartmentList(String authorization, GetDepartmentArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<Page<TenantAppDepartment>>> getTenantAppDepartmentPageList(String authorization, GetDepartmentArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<PathTenantAppDepartment>>> getPathTenantAppDepartmentList(String authorization, GetDepartmentArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<TenantAppDepartment>>> getTenantAppSubDepartmentList(String authorization, GetDepartmentArgs args) {
		throw EX;
	}
}
