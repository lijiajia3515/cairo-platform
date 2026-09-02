package io.github.lijiajia3515.cairo.auth.api.client.app_role;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
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
 * [client/api] app role controller
 */
@Slf4j
@RestController
@RequestMapping("/client_api/app_role")
@Validated
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AppRoleClientApiController {
	private final AppRoleClientApiService appRoleClientApiService;

	@PostMapping("/get_app_role_list")
	@PreAuthorize("hasAnyAuthority('role:all', 'role:read')")
	public List<MetadataAppRole> getAppRoleList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
												@Validated @RequestBody GetAppRoleArgs args) {
		String appId = principal.getAppId();
		return appRoleClientApiService.getRoleList(appId, args);
	}

	@PostMapping("/get_app_role_page_list")
	@PreAuthorize("hasAnyAuthority('role:all', 'role:read')")
	public Page<MetadataAppRole> getAppRolePageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
													@Validated @RequestBody GetAppRoleArgs args) {
		String appId = principal.getAppId();
		return appRoleClientApiService.getAppRolePageList(appId, args);
	}
}
