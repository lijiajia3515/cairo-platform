package io.github.lijiajia3515.cairo.auth.api.client.tenant;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
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
 * [client/api] tenant controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantClientApiController {

	private final TenantClientApiService tenantClientApiService;

	/**
	 * 获取租户列表
	 *
	 * @param args 参数
	 * @return 租户 列表模式
	 */
	@PostMapping("/get_tenant_list")
	@PreAuthorize("hasAnyAuthority('tenant:all', 'tenant:read')")
	public List<Tenant> getTenantList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetTenantArgs args) {
		return tenantClientApiService.getTenantList(args);
	}

	/**
	 * 获取单租户
	 *
	 * @param args 参数
	 * @return 租户
	 */
	@PostMapping("/get_tenant_info")
	@PreAuthorize("hasAnyAuthority('tenant:all', 'tenant:read')")
	public Tenant getTenantInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetTenantInfoArgs args) {
		return tenantClientApiService.getTenantInfo(args);
	}

}
