package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.CreateRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.DeleteRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.DeleteTenantRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.GetRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.GetRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.GetRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.GetTenantRoleSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRoleStatusArgs;
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
 * [tenant_subapp_user/api] tenant app role controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_role")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppRoleTenantSubappApiController {

	private final TenantAppRoleTenantSubappApiService tenantAppRoleTenantSubappApiService;

	/**
	 * 获取角色集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 角色集合
	 */
	@PostMapping("/get_tenant_app_role_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')")
	public List<MetadataTenantAppRole> getTenantAppRoleList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetRoleArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppRoleTenantSubappApiService.getTenantAppRoleList(tenantId, appId, args);
	}

	/**
	 * 获取角色分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 角色分页集合
	 */

	@PostMapping("/get_tenant_app_role_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')")
	public Page<MetadataTenantAppRole> getTenantAppRolePageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetRoleArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppRoleTenantSubappApiService.getTenantAppRolePageList(tenantId, appId, args);
	}

	/**
	 * 获取角色基本信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_app_role_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')")
	public Optional<TenantAppRole> getTenantAppRolePermission(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetRoleInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppRoleTenantSubappApiService.getTenantAppRoleInfo(tenantId, appId, args.getRoleId());
	}

	/**
	 * 获取角色菜单权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_app_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')")
	public List<MenuNode> getTenantAppRolePermission(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetRolePermissionArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppRoleTenantSubappApiService.getTenantAppRolePermission(tenantId, appId, args.getRoleId(), args.getEndpointId(), args.getSubappId(), args.getSubappVersion());
	}


	/**
	 * 创建角色
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_tenant_app_role")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:create_tenant_app_role')")
	public Optional<String> createTenantAppRole(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody CreateRoleArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppRoleTenantSubappApiService.createTenantAppRole(tenantId, appId, args);

		return Optional.empty();
	}

	/**
	 * 修改角色信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:modify_tenant_app_role_info')")
	public Optional<String> modifyTenantAppRoleInfo(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody ModifyRoleInfoArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppRoleTenantSubappApiService.modifyTenantAppRoleInfo(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 修改角色权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:modify_tenant_app_role_permission')")
	public Optional<String> modifyTenantAppRolePermission(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody ModifyRolePermissionArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppRoleTenantSubappApiService.modifyTenantAppRolePermission(tenantId, appId,args);
		return Optional.empty();
	}

	/**
	 * 修改角色状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_role_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_role:all', 'tenant_app_role:modify_tenant_app_role_status')")
	public Optional<String> modifyTenantAppRoleStatus(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody ModifyRoleStatusArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppRoleTenantSubappApiService.modifyTenantAppRoleStatus(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 删除角色
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_role")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:delete_tenant_app_role')")
	public Optional<String> deleteTenantAppRole(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody DeleteRoleArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();

		tenantAppRoleTenantSubappApiService.deleteTenantAppRole(tenantId, appId, args);
		return Optional.empty();
	}


	/**
	 * 获取应用角色子应用版本
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_tenant_role_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:read')")
	public List<TenantAppRoleSubappVersion> getTenantRoleSubappVersion(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetTenantRoleSubappVersionArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppRoleTenantSubappApiService.getTenantRoleSubappVersion(tenantId,appId, args.getEndpointId(), args.getRoleId(), args.getSubappId());
	}

	/**
	 * 删除企业角色权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_role:all', 'tenant_app_role:delete_tenant_role_permission')")
	public Optional<String> deleteTenantRolePermission(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody DeleteTenantRolePermissionArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppRoleTenantSubappApiService.deleteTenantRolePermission(tenantId,appId, args);
		return Optional.empty();
	}

}
