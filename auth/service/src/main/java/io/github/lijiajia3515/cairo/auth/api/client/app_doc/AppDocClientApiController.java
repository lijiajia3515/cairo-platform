package io.github.lijiajia3515.cairo.auth.api.client.app_doc;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_doc.GetPreviewAppDocTokenArgs;
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
 * [client/api] app doc controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/app_doc")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class AppDocClientApiController {
	private final AppDocClientApiService appDocClientApiService;

	/**
	 * 获取预览文档token
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return weboffice token
	 */
	@PostMapping("/get_preview_app_doc_token")
	@PreAuthorize("hasAnyAuthority('app_doc:all', 'app_doc:preview')")
	public WebOfficeDocToken getPreviewTenantDocToken(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetPreviewAppDocTokenArgs args) {
		return appDocClientApiService.getPreviewAppDocToken(principal.getAppId(), args.getUserId(), args.getFilepath());
	}

	/**
	 * 获取编辑文档token
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return weboffice token
	 */
	@PostMapping("/get_edit_app_doc_token")
	@PreAuthorize("hasAnyAuthority('app_doc:all', 'app_doc:edit')")
	public WebOfficeDocToken getEditAppDocToken(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @RequestBody GetPreviewAppDocTokenArgs args) {
		return appDocClientApiService.getEditAppDocToken(principal.getAppId(), args.getUserId(), args.getFilepath());
	}
}
