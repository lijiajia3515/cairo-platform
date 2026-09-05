package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.MetadataTenantEndpoint;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.CreateTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.DeleteTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.GetTenantEndpointArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.ModifyTenantEndpointInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.tenant_endpoint.ModifyTenantEndpointStatusArgs;
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
 * [cairo-web-manage/api] tenant app controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/tenant_endpoint")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantEndpointCairoWebManageApiController {

	private final TenantEndpointCairoWebManageApiService tenantEndpointCairoWebManageApiService;

	/**
	 * 获取企业列表
	 *
	 * @param args 参数
	 * @return 企业 列表模式
	 */
	@PostMapping("/get_tenant_endpoint_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')")
	public List<MetadataTenantEndpoint> getTenantList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
                                                         @Validated @RequestBody GetTenantEndpointArgs args) {
		return tenantEndpointCairoWebManageApiService.getTenantEndpointList(args);
	}

	/**
	 * 获取企业分页列表
	 *
	 * @param args 参数
	 * @return 企业 分页模式
	 */
	@PostMapping("/get_tenant_endpoint_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:read')")
	public Page<MetadataTenantEndpoint> getTenantPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															 @Validated @RequestBody GetTenantEndpointArgs args) {
		return tenantEndpointCairoWebManageApiService.getTenantEndpointPageList(args);
	}


	/**
	 * 创建企业终端
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业
	 */
	@PostMapping("/create_tenant_endpoint")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:create_tenant_endpoint')")
	public Optional<String> createTenantEndpoint(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
													@Validated @RequestBody CreateTenantEndpointArgs args) {
		tenantEndpointCairoWebManageApiService.createTenantEndpoint(args);
		return Optional.empty();
	}

	/**
	 * 修改企业终端信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业
	 */
	@PostMapping("/modify_tenant_endpoint_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_info')")
	public Optional<String> modifyTenantAppInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												@Validated @RequestBody ModifyTenantEndpointInfoArgs args) {
		tenantEndpointCairoWebManageApiService.modifyTenantEndpointInfo(args);
		return Optional.empty();
	}

	/**
	 * 修改企业终端状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/modify_tenant_endpoint_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:modify_tenant_endpoint_status')")
	public Optional<String> modifyTenantAppStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
												  @Validated @RequestBody ModifyTenantEndpointStatusArgs args) {
		tenantEndpointCairoWebManageApiService.modifyTenantEndpointStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除企业终端
	 *
	 * @param principal 凭证
	 * @param args      参数
	 */
	@PostMapping("/delete_tenant_endpoint")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_endpoint:all', 'tenant_endpoint:delete_tenant_endpoint')")
	public Optional<String> deleteTenantApp(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
											@Validated @RequestBody DeleteTenantEndpointArgs args) {
		tenantEndpointCairoWebManageApiService.deleteTenantEndpoint(args);
		return Optional.empty();
	}

}
