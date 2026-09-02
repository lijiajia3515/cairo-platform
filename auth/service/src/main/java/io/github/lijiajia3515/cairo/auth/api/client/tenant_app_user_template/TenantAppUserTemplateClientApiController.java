package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
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
 * [client_api/api] tenant_app_user_template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app_user_template")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserTemplateClientApiController {
	private final TenantAppUserTemplateClientApiService tenantAppUserTemplateClientApiService;



	/**
	 * 获取企业用户模板列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业用户模板列表
	 */
	@PostMapping("/get_tenant_app_user_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')")
	public List<TenantAppUserTemplate> getTenantAppUserTemplateList(@AuthenticationPrincipal CairoOAuthClientPrincipal principal,
																	@Validated @RequestBody(required = false) GetTenantAppUserTemplateListArgs args) {
		String appId = principal.getAppId();
		return tenantAppUserTemplateClientApiService.getTenantAppUserTemplateList(appId, args);
	}

}
