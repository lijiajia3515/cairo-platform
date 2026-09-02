package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;


import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client-api-department feignclient
 */
@FeignClient(
	contextId = "tenantAppDepartmentClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_app_department",
	fallbackFactory = TenantAppDepartmentApiClientFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantAppDepartmentApiClientFeignClient {

	/**
	 * 获取部门列表
	 * 需要权限：tenant_app_department:read | tenant_app_department:all
	 *
	 * @param args 参数
	 * @return 部门列表
	 */
	@PostMapping("/get_tenant_app_department_list")
	ResponseEntity<BusinessResult<List<TenantAppDepartment>>> getTenantAppDepartmentList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetDepartmentArgs args);

	/**
	 * 获取部门分页列表
	 * 需要权限：tenant_app_department:read | tenant_app_department:all
	 *
	 * @param args 参数
	 * @return 部门分页列表
	 */
	@PostMapping("/get_tenant_app_department_page_list")
	ResponseEntity<BusinessResult<Page<TenantAppDepartment>>> getTenantAppDepartmentPageList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetDepartmentArgs args);



	@PostMapping("/get_path_tenant_app_department_list")
	ResponseEntity<BusinessResult<List<PathTenantAppDepartment>>> getPathTenantAppDepartmentList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetDepartmentArgs args);


	@PostMapping("/get_tenant_app_sub_department_list")
	ResponseEntity<BusinessResult<List<TenantAppDepartment>>> getTenantAppSubDepartmentList(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody GetDepartmentArgs args);
}
