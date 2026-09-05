package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_authorization.TenantAppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_authorization.OfflineTenantAppUserAuthorizationArgs;
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
 * [tenant_subapp_user/api] tenant app user authorization service
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/tenant_subapp_user_api/tenant_app_user_authorization")
@CairoSecurity(type = CairoSecurityType.TENANT_SUBAPP_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppUserAuthorizationTenantSubappApiController {
	private final TenantAppUserAuthorizationTenantSubappApiService tenantAppUserAuthorizationTenantSubappApiService;

	/**
	 * 获取企业应用级用户会话list
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业会话集合
	 */
	@PostMapping("/get_tenant_app_user_authorization_list")
	@PreAuthorize("hasAnyAuthority('app_admin','tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')")
	public List<TenantAppUserAuthorization> getTenantAppUserAuthorizationList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppUserAuthorizationArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppUserAuthorizationTenantSubappApiService.getTenantAppUserAuthorizationList(tenantId, appId, args);
	}

	/**
	 * 获取企业应用级用户会话分页集合
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业会话分页集合
	 */
	@PostMapping("/get_tenant_app_user_authorization_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin','tenant_app_user_authorization:all', 'tenant_app_user_authorization:get_tenant_app_user_authorization')")
	public Page<TenantAppUserAuthorization> getTenantAppUserAuthorizationPageList(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody GetTenantAppUserAuthorizationArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		return tenantAppUserAuthorizationTenantSubappApiService.getTenantAppUserAuthorizationPageList(tenantId, appId, args);
	}

	/**
	 * 下线企业应用级用户会话
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/offline_tenant_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_authorization:all', 'tenant_app_user_authorization:offline')")
	@CairoContext
	public Optional<String> offlineTenantAppUserAuthorization(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal, @Validated @RequestBody OfflineTenantAppUserAuthorizationArgs args) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserAuthorizationTenantSubappApiService.offlineTenantAppUserAuthorization(tenantId, appId, args);
		return Optional.empty();
	}

	/**
	 * 下线所有企业应用级用户会话
	 *
	 * @param principal 凭证
	 * @return empty
	 */
	@PostMapping("/offline_all_tenant_app_user_authorization")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_authorization:all', 'tenant_app_user_authorization:offline_all')")
	@CairoContext
	public Optional<String> offlineAllAccountAuthorization(@AuthenticationPrincipal CairoOAuthTenantSubappUserPrincipal principal) {
		String tenantId = principal.getTenantId();
		String appId = principal.getAppId();
		tenantAppUserAuthorizationTenantSubappApiService.offlineAllTenantAppUserAuthorization(tenantId, appId);
		return Optional.empty();
	}

}
