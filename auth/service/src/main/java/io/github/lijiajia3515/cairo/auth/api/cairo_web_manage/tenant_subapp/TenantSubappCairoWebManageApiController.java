package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.MetadataTenantSubapp;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_subapp.CreateTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_subapp.DeleteTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_subapp.GetTenantSubappArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_subapp.ModifyTenantSubappStatusArgs;
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
 * [cairo_web_manage/api] tenant app subapp controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/tenant_subapp")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantSubappCairoWebManageApiController {

	private final TenantSubappCairoWebManageApiService tenantAppCairoEndpointUserApiService;

	/**
	 * 获取企业子应用列表
	 *
	 * @param args 参数
	 * @return 企业子应用  列表模式
	 */
	@PostMapping("/get_tenant_subapp_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')")
	public List<MetadataTenantSubapp> getTenantSubappList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody GetTenantSubappArgs args) {
		return tenantAppCairoEndpointUserApiService.getTenantSubappList(args);
	}

	/**
	 * 获取企业子应用分页列表
	 *
	 * @param args 参数
	 * @return 企业子应用 分页模式
	 */
	@PostMapping("/get_tenant_subapp_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')")
	public Page<MetadataTenantSubapp> getTenantSubappPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																	  @Validated @RequestBody GetTenantSubappArgs args) {
		return tenantAppCairoEndpointUserApiService.getTenantSubappPageList(args);
	}


	/**
	 * 创建企业子应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业子应用
	 */
	@PostMapping("/create_tenant_subapp")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:create_tenant_subapp')")
	public Optional<String> createTenantSubapp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												   @Validated @RequestBody CreateTenantSubappArgs args) {
		tenantAppCairoEndpointUserApiService.createTenantSubapp(args);
		return Optional.empty();
	}


	/**
	 * 修改企业子应用状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/modify_tenant_subapp_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:modify_tenant_subapp_status')")
	public Optional<String> modifyTenantSubappStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														 @Validated @RequestBody ModifyTenantSubappStatusArgs args) {
		tenantAppCairoEndpointUserApiService.modifyTenantSubappStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除企业子应用
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/delete_tenant_subapp")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:delete_tenant_subapp')")
	public Optional<String> deleteTenantSubapp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												   @Validated @RequestBody DeleteTenantSubappArgs args) {
		tenantAppCairoEndpointUserApiService.deleteTenantSubapp(args);
		return Optional.empty();
	}

}
