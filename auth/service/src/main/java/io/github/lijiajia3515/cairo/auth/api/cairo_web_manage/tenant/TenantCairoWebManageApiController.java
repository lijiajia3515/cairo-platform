package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.tenant;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAppUserPrincipal;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.MetadataTenant;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.CreateTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.DeleteTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantOwnerArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant.ModifyTenantStatusArgs;
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
 * [cairo-web-manage/api] tenant controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/tenant")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantCairoWebManageApiController {

	private final TenantCairoWebManageApiService tenantCairoWebManageApiService;

	/**
	 * 获取企业列表
	 *
	 * @param args 参数
	 * @return 企业 列表模式
	 */
	@PostMapping("/get_tenant_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')")
	public List<MetadataTenant> getTenantList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                              @Validated @RequestBody GetTenantArgs args) {
		return tenantCairoWebManageApiService.getTenantList(args);
	}

	/**
	 * 获取企业分页
	 *
	 * @param args 参数
	 * @return 企业 分页模式
	 */
	@PostMapping("/get_tenant_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:read')")
	public Page<MetadataTenant> getTenantPageList(@AuthenticationPrincipal CairoOAuthAppUserPrincipal principal,
												  @Validated @RequestBody GetTenantArgs args) {
		return tenantCairoWebManageApiService.getTenantPageList(args);
	}

	/**
	 * 创建企业
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/create_tenant")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:create_tenant')")
	public Optional<String> createTenant(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @Validated @RequestBody CreateTenantArgs args) {
		if (args.getAliasName() != null && !args.getAliasName().isBlank()){
			args.setAliasName(null);
		}
		tenantCairoWebManageApiService.createTenant(args);
		return Optional.empty();
	}

	/**
	 * 修改企业
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_tenant_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_info')")
	public Optional<String> modifyTenantInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											 @Validated @RequestBody ModifyTenantInfoArgs args) {
		tenantCairoWebManageApiService.modifyTenantInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改企业拥有着
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/modify_tenant_owner")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_owner')")
	public Optional<String> modifyTenantOwner(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											  @Validated @RequestBody ModifyTenantOwnerArgs args) {
		tenantCairoWebManageApiService.modifyTenantOwner(args);
		return Optional.empty();
	}

	/**
	 * 修改企业状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业
	 */
	@PostMapping("/modify_tenant_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:modify_tenant_status')")
	public Optional<String> modifyTenantStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											   @Validated @RequestBody ModifyTenantStatusArgs args) {
		tenantCairoWebManageApiService.modifyTenantStatus(args);
		return Optional.empty();
	}


	/**
	 * 删除企业
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/delete_tenant")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant:all', 'tenant:delete_tenant')")
	public Optional<String> deleteTenant(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
										 @Validated @RequestBody DeleteTenantArgs args) {
		tenantCairoWebManageApiService.deleteTenant(args);
		return Optional.empty();
	}


}
