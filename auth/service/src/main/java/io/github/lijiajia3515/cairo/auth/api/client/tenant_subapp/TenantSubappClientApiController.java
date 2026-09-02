package io.github.lijiajia3515.cairo.auth.api.client.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.MetadataTenantSubapp;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp.GetTenantSubappArgs;
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
 * [client/api] tenant app subapp controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_subapp")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantSubappClientApiController {

	private final TenantSubappClientApiService tenantSubappClientApiService;

	/**
	 * 获取企业子应用列表
	 *
	 * @param args 参数
	 * @return 企业子应用  列表模式
	 */
	@PostMapping("/get_tenant_subapp_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_subapp:all', 'tenant_subapp:read')")
	public List<MetadataTenantSubapp> getTenantSubappList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																  @Validated @RequestBody GetTenantSubappArgs args) {
		String tenantId = args.getTenantId();
		String appId = principal.getAppId();
		return tenantSubappClientApiService.getTenantSubappList(tenantId,appId,args);
	}


}
