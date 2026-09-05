package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;
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
 * [client/api] tenant app user authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app_user_authorization")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppUserAuthorizationClientApiController {

	private final TenantAppUserAuthorizationClientApiService tenantAppUserAuthorizationClientApiService;

	/**
	 * 获取企业应用级用户凭证
	 *
	 * @param principal 用户凭证才能访问
	 * @return 账号凭证
	 */
	@PostMapping("/get_tenant_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')")
	public TenantAppUserAuthorizationModel getTenantAppUserAuthorization(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetTenantAppUserAuthorizationArgs args) {
		return tenantAppUserAuthorizationClientApiService.getTenantAppUserAuthorization(args);
	}

	/**
	 * 获取企业应用级用户凭证
	 *
	 * @param principal 用户凭证才能访问
	 * @return 账号凭证
	 */
	@PostMapping("/get_custom_tenant_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')")
	public TenantAppUserAuthorizationModel getCustomTenantAppUserAuthorization(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetTenantAppUserAuthorizationArgs args) {
		return tenantAppUserAuthorizationClientApiService.getTenantAppUserAuthorization(args);
	}
}
