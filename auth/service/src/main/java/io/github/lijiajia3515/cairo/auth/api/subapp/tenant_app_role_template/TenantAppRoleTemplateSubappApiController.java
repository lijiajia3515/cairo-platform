package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_role_template;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.MetadataTenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplatePermissionNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplateSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.CreateTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.DeleteTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.DeleteTenantAppRoleTemplatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.GetTenantAppRoleTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.GetTenantAppRoleTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.GetTenantAppRoleTemplatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.GetTenantAppRoleTemplateSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_role_template.ModifyTenantAppRoleTemplateStatusArgs;
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
 * [subapp_user_api/api] tenant_app_role_template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/tenant_app_role_template")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppRoleTemplateSubappApiController {

	private final TenantAppRoleTemplateSubappApiService tenantAppRoleTemplateSubappApiService;

	/**
	 * 获取企业角色模板集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业角色模板集合
	 */
	@PostMapping("/get_tenant_app_role_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')")
	public List<MetadataTenantAppRoleTemplate> getTenantAppRoleTemplateList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppRoleTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleTemplateSubappApiService.getTenantAppRoleTemplateList(appId, args);
	}

	/**
	 * 获取企业角色模板分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业角色模板分页集合
	 */

	@PostMapping("/get_tenant_app_role_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')")
	public Page<MetadataTenantAppRoleTemplate> getTenantAppRoleTemplatePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppRoleTemplateArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleTemplateSubappApiService.getTenantAppRoleTemplatePageList(appId, args);
	}

	/**
	 * 获取企业角色模板基本信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_app_role_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')")
	public Optional<TenantAppRoleTemplate> getTenantAppRoleTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppRoleTemplateInfoArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleTemplateSubappApiService.getTenantAppRoleTemplateInfo(appId,args.getTenantAppRoleTemplateId());
	}

	/**
	 * 获取企业角色模板菜单权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_app_role_template_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')")
	public List<TenantAppRoleTemplatePermissionNode> getTenantAppRoleTemplatePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppRoleTemplatePermissionArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleTemplateSubappApiService.getTenantAppRoleTemplatePermission(appId, args.getTenantAppRoleTemplateId(), args.getEndpointId(), args.getSubappId(), args.getSubappVersion());
	}


	/**
	 * 创建企业角色模板
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_tenant_app_role_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:create_tenant_app_role_template')")
	public Optional<String> createTenantAppRoleTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreateTenantAppRoleTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.createTenantAppRoleTemplate(appId,args);
		return Optional.empty();
	}

	/**
	 * 修改企业角色模板信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_info')")
	public Optional<String> modifyTenantAppRoleTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyTenantAppRoleTemplateInfoArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.modifyTenantAppRoleTemplateInfo(appId,args);
		return Optional.empty();
	}

	/**
	 * 修改企业角色模板权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_template_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_permission')")
	public Optional<String> modifyTenantAppRoleTemplatePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyTenantAppRoleTemplatePermissionArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.modifyTenantAppRoleTemplatePermission(appId,args);
		return Optional.empty();
	}

	/**
	 * 修改企业角色模板状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:modify_tenant_app_role_template_status')")
	public Optional<String> modifyTenantAppRoleTemplateStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyTenantAppRoleTemplateStatusArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.modifyTenantAppRoleTemplateStatus(appId,args);
		return Optional.empty();
	}

	/**
	 * 删除企业角色模板
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_role_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:delete_tenant_app_role_template')")
	public Optional<String> deleteTenantAppRoleTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteTenantAppRoleTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.deleteTenantAppRoleTemplate(appId,args);
		return Optional.empty();
	}

	/**
	 * 获取企业角色模板子应用版本
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_app_role_template_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:read')")
	public List<TenantAppRoleTemplateSubappVersion> getTenantAppRoleTemplateSubappVersion(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppRoleTemplateSubappVersionArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleTemplateSubappApiService.getTenantAppRoleTemplateSubappVersion(appId,args.getEndpointId(), args.getTenantAppRoleTemplateId(), args.getSubappId());
	}

	/**
	 * 删除企业角色模板权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_role_template_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role_template:all', 'tenant_app_role_template:delete_tenant_app_role_template_permission')")
	public Optional<String> deleteTenantAppRoleTemplatePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteTenantAppRoleTemplatePermissionArgs args) {
		String appId = principal.getAppId();
		tenantAppRoleTemplateSubappApiService.deleteTenantAppRoleTemplatePermission(appId,args);
		return Optional.empty();
	}
}
