package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.permission;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants;
import io.github.lijiajia3515.cairo.auth.framework.context.CairoContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.MetadataPermission;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.CreatePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.DeletePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.GetPermissionPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.ModifyPermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.permission.MovePermissionArgs;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
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
 * [cairo-web-manage/api] action permission controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/permission")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class PermissionCairoWebManageApiController {

	private final PermissionCairoWebManageApiService permissionCairoWebManageApiService;

	/**
	 * 获取功能权限list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 功能权限集合
	 */
	@PostMapping("/get_permission_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:read')")
	@CairoContext
	public List<MetadataPermission> getPermissionList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody(required = false) GetPermissionListArgs args) {

		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		if (args == null) {
			args = new GetPermissionListArgs();
		}
		return permissionCairoWebManageApiService.getPermissionList(appId, endpointId,subappId, subappVersion, args);
	}

	/**
	 * 获取功能权限list page
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 功能权限分页集合
	 */
	@PostMapping("/get_permission_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:read')")
	@CairoContext
	public Page<MetadataPermission> getPermissionPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody(required = false) GetPermissionPageListArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		if (args == null) {
			args = new GetPermissionPageListArgs();
		}
		return permissionCairoWebManageApiService.getPermissionPageList(appId, endpointId,subappId, subappVersion, args);
	}

	/**
	 * 创建功能权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/create_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:write')")
	@CairoContext
	public Optional<String> createPermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody CreatePermissionArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		permissionCairoWebManageApiService.createPermission(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}

	/**
	 * 修改功能权限信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:write')")
	@CairoContext
	public Optional<String> modifyPermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody ModifyPermissionArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		permissionCairoWebManageApiService.modifyPermission(appId, endpointId,subappId, subappVersion, args);
		return Optional.empty();
	}

	/**
	 * 删除功能权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/delete_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:write')")
	@CairoContext
	public Optional<String> deletePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody DeletePermissionArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		permissionCairoWebManageApiService.deletePermission(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}

	/**
	 * 移动功能权限
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/move_permission")
	@PreAuthorize("hasAnyAuthority('app_admin', 'permission:all', 'permission:move')")
	@CairoContext
	public Optional<String> movePermission(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal, @Validated @RequestBody MovePermissionArgs args) {
		String appId = CairoContextHolder.getValue(CairoContextConstants.APP_ID).orElseThrow(() -> new ParamsErrorBusinessException("appId不能为空"));
		String endpointId = CairoContextHolder.getValue(CairoContextConstants.ENDPOINT_ID).orElseThrow(() -> new ParamsErrorBusinessException("endpointId不能为空"));
		String subappId = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_ID).orElseThrow(() -> new ParamsErrorBusinessException("subappId不能为空"));
		String subappVersion = CairoContextHolder.getValue(CairoContextConstants.SUBAPP_VERSION).orElseThrow(() -> new ParamsErrorBusinessException("subappVersion不能为空"));
		permissionCairoWebManageApiService.movePermission(appId, endpointId, subappId, subappVersion, args);
		return Optional.empty();
	}

}
