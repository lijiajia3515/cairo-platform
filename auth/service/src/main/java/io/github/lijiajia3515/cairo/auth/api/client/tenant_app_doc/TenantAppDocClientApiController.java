package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_doc;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc.GetOnlineTenantAppDocArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
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
 * [client/api] tenant app doc controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/tenant_app_doc")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class TenantAppDocClientApiController {
	private final TenantAppDocClientApiService tenantAppDocClientApiService;

	/**
	 * 获取预览文档token
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return weboffice token
	 */
	@PostMapping("/get_preview_tenant_app_doc_token")
	@PreAuthorize("hasAnyAuthority('tenant_app_doc:all', 'tenant_app_doc:preview')")
	public WebOfficeDocToken getPreviewTenantAppDocToken(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetOnlineTenantAppDocArgs args) {
		return tenantAppDocClientApiService.getPreviewTenantAppDocToken(args.getTenantId(), principal.getAppId(), args.getUserId(), args.getFilepath());
	}

	/**
	 * 获取编辑文档token
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return weboffice token
	 */
	@PostMapping("/get_edit_tenant_app_doc_token")
	@PreAuthorize("hasAnyAuthority('tenant_app_doc:all', 'tenant_app_doc:edit')")
	public WebOfficeDocToken getEditTenantAppDocToken(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetOnlineTenantAppDocArgs args) {
		return tenantAppDocClientApiService.getEditTenantAppDocToken(args.getTenantId(), principal.getAppId(), args.getUserId(), args.getFilepath());
	}
}
