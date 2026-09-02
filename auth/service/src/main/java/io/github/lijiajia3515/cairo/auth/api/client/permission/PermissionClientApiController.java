package io.github.lijiajia3515.cairo.auth.api.client.permission;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
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

@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/permission")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class PermissionClientApiController {

	private final PermissionClientApiService permissionClientApiService;

	/**
	 * 获取功能权限list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 功能权限集合
	 */
	@PostMapping("/get_permission_list")
	@PreAuthorize("hasAnyAuthority('permission:all', 'permission:read')")
	@CairoContext
	public List<Permission> getPermissionList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody(required = false) GetPermissionListArgs args) {

		String appId = principal.getAppId();
		String endpointId = args.getEndpointId();
		String subappId = args.getSubappId();
		String subappVersion = args.getSubappVersion();
		return permissionClientApiService.getPermissionList(appId, endpointId, subappId,subappVersion, args);
	}

	/**
	 * 获取我的功能权限list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 功能权限集合
	 */
	@PostMapping("/get_my_permission_list")
	@PreAuthorize("hasAnyAuthority('permission:all', 'permission:read')")
	@CairoContext
	public List<Permission> getMyPermissionList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody(required = false) GetPermissionListArgs args) {

		String appId = principal.getAppId();
		String endpointId = args.getEndpointId();
		String subappId = args.getSubappId();
		String subappVersion = args.getSubappVersion();
		return permissionClientApiService.getMyPermissionList(appId, endpointId,  subappId,subappVersion, args);
	}
}
