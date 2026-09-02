package io.github.lijiajia3515.cairo.auth.api.client.tenant_subapp_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.GetTenantSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.TenantSubappUserAuthorizationModel;
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

/**
 * [client/api] tenant app subapp user authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_subapp_user_authorization")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantSubappUserAuthorizationClientApiController {

	private final TenantSubappUserAuthorizationClientApiService tenantSubappUserAuthorizationClientApiService;

	/**
	 * 获取企业子应用授权
	 *
	 * @param principal 品正
	 * @return 企业子应用授权信息
	 */
	@PostMapping("/get_tenant_subapp_user_authorization")
	@PreAuthorize("hasAnyAuthority('tenant_subapp_user_authorization:all', 'tenant_subapp_user_authorization:get_tenant_subapp_user_authorization')")
	public TenantSubappUserAuthorizationModel getAppUserAuthorization(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetTenantSubappUserAuthorizationArgs args) {
		return tenantSubappUserAuthorizationClientApiService.getTenantSubappUserAuthorization(args);
	}
}
