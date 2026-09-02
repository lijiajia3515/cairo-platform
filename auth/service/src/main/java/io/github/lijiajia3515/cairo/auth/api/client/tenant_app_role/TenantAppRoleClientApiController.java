package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
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
 * [client/api] tenant app role controller
 */
@Slf4j
@RestController
@RequestMapping("/client_api/tenant_app_role")
@Validated
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppRoleClientApiController {
	private final TenantAppRoleClientApiService tenantAppRoleClientApiService;

	@PostMapping("/get_tenant_app_role_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_role:all', 'tenant_app_role:read')")
	public List<MetadataTenantAppRole> getTenantAppRoleList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
                                                            @Validated @RequestBody GetTenantAppRoleArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleClientApiService.getTenantAppRoleList(appId, args);
	}

	@PostMapping("/get_tenant_app_role_page_list")
	@PreAuthorize("hasAnyAuthority('tenant_app_role:all', 'tenant_app_role:read')")
	public Page<MetadataTenantAppRole> getTenantAppRolePageList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
                                                                @Validated @RequestBody GetTenantAppRoleArgs args) {
		String appId = principal.getAppId();
		return tenantAppRoleClientApiService.getTenantAppRolePageList(appId, args);
	}
}
