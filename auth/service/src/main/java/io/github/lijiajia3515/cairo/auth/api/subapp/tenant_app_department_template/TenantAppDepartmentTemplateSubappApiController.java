package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_department_template;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.MetadataTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TreeNodeTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.CreateTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.DeleteTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.GeTenantAppDepartmentTemplateByDepartmentIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.GetTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.GetTenantAppDepartmentTemplateTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.ModifyTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.ModifyTenantAppDepartmentTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.MoveTenantAppDepartmentTemplateArgs;
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
 * [subapp_user/api] tenant_app_department_template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/tenant_app_department_template")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppDepartmentTemplateSubappApiController {
	private final TenantAppDepartmentTemplateSubappApiService tenantAppDepartmentTemplateSubappApiService;

	/**
	 * 获取企业部门模板列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业部门模板集合
	 */
	@PostMapping("/get_tenant_app_department_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public List<MetadataTenantAppDepartmentTemplate> getTenantAppDepartmentTemplateList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																						@Validated @RequestBody GetTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getTenantAppDepartmentTemplateList(appId, args);
	}

	/**
	 * 获取企业部门模板分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业部门模板分页列表
	 */
	@PostMapping("/get_tenant_app_department_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public Page<MetadataTenantAppDepartmentTemplate> getTenantAppDepartmentTemplatePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																							@Validated @RequestBody GetTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getTenantAppDepartmentTemplatePageList(appId, args);
	}

	@PostMapping("/get_path_tenant_app_department_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public List<PathTenantAppDepartmentTemplate> getDepartmentAncestor(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	   @Validated @RequestBody GetTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getPathTenantAppDepartmentTemplateList(appId, args);
	}

	@PostMapping("/get_path_tenant_app_department_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public Page<PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplatePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																							@Validated @RequestBody GetTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getPathTenantAppDepartmentTemplatePageList(appId, args);
	}

	/**
	 * 获取企业部门模板树形列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业部门模板分页列表
	 */
	@PostMapping("/get_tenant_app_department_template_tree")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public TreeNodeTenantAppDepartmentTemplate getDepartmentTreeList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	 @Validated @RequestBody GetTenantAppDepartmentTemplateTreeArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getTenantAppDepartmentTemplateTree(appId, args);
	}

	/**
	 * 获取企业部门模板根据企业部门模板ID
	 *
	 * @param principal principal
	 * @param args      参数
	 * @return PathDepartment
	 */
	@PostMapping("/get_tenant_app_department_template_by_department_id")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public Optional<PathTenantAppDepartmentTemplate> getTenantAppDepartmentTemplateById(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																						@Validated @RequestBody GeTenantAppDepartmentTemplateByDepartmentIdArgs args) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getTenantAppDepartmentTemplateByTenantAppDepartmentTemplateId(appId, args.getTenantAppDepartmentTemplateId());
	}

	/**
	 * 创建企业部门模板
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 企业部门模板
	 */

	@PostMapping("/create_tenant_app_department_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:create_tenant_app_department_template')")
	public Optional<String> createTenantAppDepartmentTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody CreateTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppDepartmentTemplateSubappApiService.createTenantAppDepartmentTemplate(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改企业部门模板
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 企业部门模板
	 */
	@PostMapping("/modify_tenant_app_department_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:modify_tenant_app_department_template_info')")
	public Optional<String> modifyTenantAppDepartmentTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody ModifyTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppDepartmentTemplateSubappApiService.modifyTenantAppDepartmentTemplateInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 企业部门模板移动
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 企业部门模板
	 */
	@PostMapping("/move_tenant_app_department_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'department:move_tenant_app_department_template')")
	public Optional<String> moveTenantAppDepartmentTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody MoveTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppDepartmentTemplateSubappApiService.moveTenantAppDepartmentTemplate(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除企业部门模板,包含删除子级企业部门模板
	 *
	 * @param principal principal
	 * @param args      args
	 * @return 删除的企业部门模板列表
	 */
	@PostMapping("/delete_tenant_app_department_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:delete_tenant_app_department_template')")
	public Optional<String> deleteTenantAppDepartmentTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody DeleteTenantAppDepartmentTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppDepartmentTemplateSubappApiService.deleteTenantAppDepartmentTemplate(appId, args);
		return Optional.empty();
	}


	/**
	 * 修改企业部门模板状态
	 *
	 * @param principal principal
	 * @param args      args
	 */
	@PostMapping("/modify_tenant_app_department_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:modify_tenant_app_department_template_status')")
	public Optional<String> modifyTenantAppDepartmentTemplateStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	@Validated @RequestBody ModifyTenantAppDepartmentTemplateStatusArgs args) {
		String appId = principal.getAppId();
		tenantAppDepartmentTemplateSubappApiService.modifyTenantAppDepartmentTemplateStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 查询企业部门模板状态
	 *
	 * @param principal 凭证
	 * @return 企业部门模板状态
	 */
	@PostMapping("/get_tenant_app_department_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_department_template:all', 'tenant_app_department_template:read')")
	public Optional<Boolean> getTenantAppDepartmentDepartmentStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal) {
		String appId = principal.getAppId();
		return tenantAppDepartmentTemplateSubappApiService.getTenantAppDepartmentDepartmentStatus(appId);
	}

}
