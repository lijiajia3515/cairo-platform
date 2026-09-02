package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.MetadataTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TreeNodeTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.CreateDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.DeleteDepartmentUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.GetDepartmentByDepartmentIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.GetDepartmentTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.ModifyDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.MoveDepartmentArgs;
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
import java.util.Optional;

/**
 * [tenant_subapp_user/api] tenant app department controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_department")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppDepartmentTenantSubappApiController {

	private final TenantAppDepartmentTenantSubappApiService tenantAppDepartmentTenantSubappApiService;

	/**
	 * 获取部门列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 部门集合
	 */
	@PostMapping("/get_tenant_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public List<MetadataTenantAppDepartment> getTenantDepartmentList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																	 @Validated @RequestBody GetDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppDepartmentTenantSubappApiService.getTenantAppDepartmentList(tenantId, appId, args);
	}

	/**
	 * 获取部门分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 部门分页列表
	 */
	@PostMapping("/get_tenant_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public Page<MetadataTenantAppDepartment> getTenantAppDepartmentPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																			@Validated @RequestBody GetDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppDepartmentTenantSubappApiService.getTenantDepartmentPageList(tenantId, appId, args);
	}

	@PostMapping("/get_path_tenant_app_department_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public List<PathTenantAppDepartment> getPathTenantAppDepartmentList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																		@Validated @RequestBody GetDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppDepartmentTenantSubappApiService.getPathTenantAppDepartmentList(tenantId, appId, args);
	}

	@PostMapping("/get_path_tenant_app_department_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public Page<PathTenantAppDepartment> getPathTenantAppDepartmentPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
																			@Validated @RequestBody GetDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppDepartmentTenantSubappApiService.getPathTenantAppDepartmentPageList(tenantId, appId, args);
	}

	/**
	 * 获取部门树形列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 部门分页列表
	 */
	@PostMapping("/get_tenant_app_department_tree")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public TreeNodeTenantAppDepartment getDepartmentTree(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														 @Validated @RequestBody GetDepartmentTreeArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		return tenantAppDepartmentTenantSubappApiService.getTenantAppDepartmentTree(tenantId, appId, args);
	}

	/**
	 * 获取部门根据部门ID
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return PathDepartment
	 */
	@PostMapping("/get_tenant_app_department_by_department_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:read')")
	public Optional<PathTenantAppDepartment> getDepartmentById(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
															   @Validated @RequestBody GetDepartmentByDepartmentIdArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppDepartmentTenantSubappApiService.getTenantAppDepartmentByDepartmentId(tenantId, appId, args.getDepartmentId());
	}

	/**
	 * 创建部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门
	 */

	@PostMapping("/create_tenant_app_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:create_tenant_app_department')")
	public Optional<String> createTenantDepartment(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												   @Validated @RequestBody CreateDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppDepartmentTenantSubappApiService.createTenantAppDepartment(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门
	 */
	@PostMapping("/modify_tenant_app_department_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:modify_tenant_app_department_info')")
	public Optional<String> modifyTenantDepartmentInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
													   @Validated @RequestBody ModifyDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppDepartmentTenantSubappApiService.modifyTenantAppDepartmentInfo(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 菜单移动
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 部门
	 */
	@PostMapping("/move_tenant_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:move_tenant_app_department')")
	public Optional<String> moveTenantDepartment(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												 @Validated @RequestBody MoveDepartmentArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppDepartmentTenantSubappApiService.moveTenantAppDepartment(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 删除部门,包含删除子级部门
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 删除的部门列表
	 */
	@PostMapping("/delete_tenant_app_department")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department:all', 'tenant_app_department:delete_tenant_app_department')")
	public Optional<String> deleteTenantDepartment(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
												   @Validated @RequestBody DeleteDepartmentUserArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppDepartmentTenantSubappApiService.deleteTenantAppDepartment(tenantId, appId, args);
		return Optional.empty();
	}

}
