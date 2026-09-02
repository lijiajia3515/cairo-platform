package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.permission;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionPageListArgs;
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
 * [tenant_subapp_user/api] action permission
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/permission")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class PermissionTenantSubappApiController {

	private final PermissionTenantSubappApiService permissionTenantSubappApiService;

	/**
	 * 获取功能权限list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return element list
	 */
	@PostMapping("/get_permission_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:read')")
	public List<Permission> getAcitonPermissionList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
														  @Validated @RequestBody(required = false) GetPermissionListArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		if (args == null) {
			args = new GetPermissionListArgs();
		}
		return permissionTenantSubappApiService.getPermissionList(appId, endpointId, principal.getSubappId(), principal.getSubappVersion(), args);
	}

	/**
	 * 获取功能权限 分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return action permission list page
	 */
	@PostMapping("/get_permission_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:read')")
	public Page<Permission> getPermissionPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal,
															  @Validated @RequestBody(required = false) GetPermissionPageListArgs args) {
		String appId = principal.getAppId();
		String endpointId = principal.getEndpointId();
		if (args == null) {
			args = new GetPermissionPageListArgs();
		}
		return permissionTenantSubappApiService.getPermissionPageList(appId, endpointId, principal.getSubappId(), principal.getSubappVersion(), args);
	}

}
