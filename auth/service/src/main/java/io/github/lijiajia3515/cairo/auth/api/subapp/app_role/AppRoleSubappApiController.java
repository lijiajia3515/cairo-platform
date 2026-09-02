package io.github.lijiajia3515.cairo.auth.api.subapp.app_role;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.CreateAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.DeleteAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.DeleteAppRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.GetAppRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.GetAppRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.GetAppRoleSubappVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRoleStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
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
 * [subapp_user/api] app role controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/app_role")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class AppRoleSubappApiController {

	private final AppRoleSubappApiService appRoleAppUserApiService;

	/**
	 * 获取应用角色集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用角色集合
	 */
	@PostMapping("/get_app_role_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')")
	public List<MetadataAppRole> getAppRoleList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppRoleArgs args) {
		String appId = principal.getAppId();
		return appRoleAppUserApiService.getAppRoleList(appId, args);
	}

	/**
	 * 获取应用角色分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 应用角色分页集合
	 */

	@PostMapping("/get_app_role_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')")
	public Page<MetadataAppRole> getAppRolePageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppRoleArgs args) {
		String appId = principal.getAppId();
		return appRoleAppUserApiService.getAppRolePageList(appId, args);
	}

	/**
	 * 获取应用角色基本信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_app_role_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')")
	public Optional<AppRole> getAppRoleInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppRoleInfoArgs args) {
		String appId = principal.getAppId();
		return appRoleAppUserApiService.getAppRoleInfo(appId, args.getRoleId());
	}

	/**
	 * 获取应用角色菜单权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_app_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')")
	public List<MenuNode> getAppRolePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppRolePermissionArgs args) {
		String appId = principal.getAppId();
		return appRoleAppUserApiService.getAppRolePermission(appId, args.getRoleId(), args.getEndpointId(), args.getSubappId(), args.getSubappVersion());
	}


	/**
	 * 创建应用角色
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_app_role")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:create_app_role')")
	public Optional<String> createAppRole(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreateAppRoleArgs args) {
		String appId = principal.getAppId();
		appRoleAppUserApiService.createAppRole(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改应用角色信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_app_role_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_info')")
	public Optional<String> modifyAppRoleInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAppRoleInfoArgs args) {
		String appId = principal.getAppId();
		appRoleAppUserApiService.modifyAppRoleInfo(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改应用角色权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_app_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_permission')")
	public Optional<String> modifyAppRolePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAppRolePermissionArgs args) {
		String appId = principal.getAppId();
		appRoleAppUserApiService.modifyAppRolePermission(appId, args);
		return Optional.empty();
	}

	/**
	 * 修改应用角色状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_app_role_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:modify_app_role_status')")
	public Optional<String> modifyAppRoleStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyAppRoleStatusArgs args) {
		String appId = principal.getAppId();
		appRoleAppUserApiService.modifyAppRoleStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除应用角色
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_app_role")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:delete_app_role')")
	public Optional<String> deleteAppRole(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteAppRoleArgs args) {
		String appId = principal.getAppId();

		appRoleAppUserApiService.deleteAppRole(appId, args);
		return Optional.empty();
	}

	/**
	 * 获取应用角色子应用版本
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/get_app_role_subapp_version")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:read')")
	public List<AppRoleSubappVersion> getAppRoleSubappVersion(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody GetAppRoleSubappVersionArgs args) {
		String appId = principal.getAppId();
		return appRoleAppUserApiService.getAppRoleSubappVersion(appId,  args.getEndpointId(), args.getRoleId(), args.getSubappId());
	}

	/**
	 * 删除应用角色权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_app_role_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'app_role:all', 'app_role:delete_app_role_permission')")
	public Optional<String> deleteAppRolePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeleteAppRolePermissionArgs args) {
		String appId = principal.getAppId();

		appRoleAppUserApiService.deleteAppRolePermission(appId, args);
		return Optional.empty();
	}
}
