package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.web.bind.annotation.BusinessResultBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [client/api] tenant department controller
 */

@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app_department")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppDepartmentClientApiController {

	private final TenantAppDepartmentClientApiService tenantAppDepartmentClientApiService;

	/**
	 * 获取部门列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门列表
	 */
	@PostMapping("/get_tenant_app_department_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_department:all', 'tenant_app_department:read')")
	public List<TenantAppDepartment> getDepartmentList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
													   @Validated @RequestBody GetDepartmentArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentClientApiService.getTenantAppDepartmentList(appId, args);
	}

	/**
	 * 获取部门分页列表
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门分页列表
	 */
	@PostMapping("/get_tenant_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_department:all', 'tenant_app_department:read')")
	public Page<TenantAppDepartment> getDepartmentPageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
														   @Validated @RequestBody GetDepartmentArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentClientApiService.getTenantAppDepartmentPageList(appId, args);
	}

	@PostMapping("/get_path_tenant_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public List<PathTenantAppDepartment> getPathTenantAppDepartmentList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																		@Validated @RequestBody GetDepartmentArgs args) {
		String appId = principal.getAppId();

		return tenantAppDepartmentClientApiService.getPathTenantAppDepartmentList( appId, args);
	}

	@PostMapping("/get_tenant_app_sub_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public List<TenantAppDepartment> getTenantAppSubDepartmentList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																		@Validated @RequestBody GetDepartmentArgs args) {
		String appId = principal.getAppId();

		return tenantAppDepartmentClientApiService.getTenantAppSubDepartmentList( appId, args);
	}

}
