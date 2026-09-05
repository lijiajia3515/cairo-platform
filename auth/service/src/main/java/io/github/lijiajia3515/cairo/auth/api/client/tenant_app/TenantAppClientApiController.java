package io.github.lijiajia3515.cairo.auth.api.client.tenant_app;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthTenantAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
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
 * [client/api] tenant app controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppClientApiController {

	private final TenantAppClientApiService tenantAppClientApiService;

	/**
	 * 获取企业列表
	 *
	 * @param args 参数
	 * @return 企业 列表模式
	 */
	@PostMapping("/get_tenant_app_list")
	@PreAuthorize("hasAnyAuthority('tenant_app:all', 'tenant_app:read')")
	public List<TenantApp> getTenantList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
                                         @Validated @RequestBody GetTenantAppArgs args) {
		return tenantAppClientApiService.getTenantAppList(args);
	}

	/**
	 * 获取企业分页列表
	 *
	 * @param args 参数
	 * @return 企业 分页模式
	 */
	@PostMapping("/get_tenant_app_page_list")
	@PreAuthorize("hasAnyAuthority('tenant_app:all', 'tenant_app:read')")
	public Page<TenantApp> getTenantPageList(@AuthenticationPrincipal CairoOAuthTenantAppUserPrincipal principal,
                                                     @Validated @RequestBody GetTenantAppArgs args) {
		return tenantAppClientApiService.getTenantAppPageList(args);
	}

}
