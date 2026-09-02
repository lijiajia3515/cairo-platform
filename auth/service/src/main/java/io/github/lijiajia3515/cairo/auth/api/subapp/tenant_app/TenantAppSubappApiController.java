package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.MetadataTenantApp;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.CreateTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.DeleteTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.ModifyTenantAppInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app.ModifyTenantAppStatusArgs;
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
 * [subapp_user/api] tenant app controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/tenant_app")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppSubappApiController {

	private final TenantAppSubappApiService tenantAppSubappApiService;

	/**
	 * 获取租户列表
	 *
	 * @param args 参数
	 * @return 租户 列表模式
	 */
	@PostMapping("/get_tenant_app_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')")
	public List<MetadataTenantApp> getTenantList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                                 @Validated @RequestBody GetTenantAppArgs args) {
		String appId = principal.getAppId();
		return tenantAppSubappApiService.getTenantAppList(appId,args);
	}

	/**
	 * 获取租户分页列表
	 *
	 * @param args 参数
	 * @return 租户 分页模式
	 */
	@PostMapping("/get_tenant_app_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:read')")
	public Page<MetadataTenantApp> getTenantPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                                     @Validated @RequestBody GetTenantAppArgs args) {
		String appId = principal.getAppId();
		return tenantAppSubappApiService.getTenantAppPageList(appId,args);
	}


	/**
	 * 创建租户
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 租户
	 */
	@PostMapping("/create_tenant_app")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:create_tenant_app')")
	public Optional<String> createTenantApp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody CreateTenantAppArgs args) {
		String appId = principal.getAppId();
		tenantAppSubappApiService.createTenantApp(appId,args);
		return Optional.empty();
	}

	/**
	 * 修改企业应用信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 租户
	 */
	@PostMapping("/modify_tenant_app_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_info')")
	public Optional<String> modifyTenantAppInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody ModifyTenantAppInfoArgs args) {
		String appId = principal.getAppId();
		tenantAppSubappApiService.modifyTenantAppInfo(appId,args);
		return Optional.empty();
	}

	/**
	 * 修改企业应用状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/modify_tenant_app_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:modify_tenant_app_status')")
	public Optional<String> modifyTenantAppStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody ModifyTenantAppStatusArgs args) {
		String appId = principal.getAppId();
		tenantAppSubappApiService.modifyTenantAppStatus(appId,args);
		return Optional.empty();
	}

	/**
	 * 删除企业应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/delete_tenant_app")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app:all', 'tenant_app:delete_tenant_app')")
	public Optional<String> deleteTenantApp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody DeleteTenantAppArgs args) {
		String appId = principal.getAppId();
		tenantAppSubappApiService.deleteTenantApp(appId,args);
		return Optional.empty();
	}

}
