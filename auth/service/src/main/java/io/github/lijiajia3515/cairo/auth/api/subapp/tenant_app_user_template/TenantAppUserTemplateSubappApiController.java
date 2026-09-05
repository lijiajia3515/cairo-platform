package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.MetadataTenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.CreateAccountAndTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.CreateTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.DeleteTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.ModifyTenantAppUserTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.ModifyTenantAppUserTemplateStatusArgs;
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
 * [subapp_user/api] tenant_app_user_template controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/subapp_user_api/tenant_app_user_template")
@CairoSecurity(type = CairoSecurityType.SUBAPP_USER)
@RequiredArgsConstructor
@BusinessResultBody
public class TenantAppUserTemplateSubappApiController {
	private final TenantAppUserTemplateSubappApiService tenantAppUserTemplateSubappApiService;


	/**
	 * 获取企业应用级用户模板列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业应用级用户模板列表
	 */
	@PostMapping("/get_tenant_app_user_template_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')")
	public List<MetadataTenantAppUserTemplate> getTenantAppUserTemplateList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																			@Validated @RequestBody(required = false) GetTenantAppUserTemplateListArgs args) {
		String appId = principal.getAppId();
		return tenantAppUserTemplateSubappApiService.getTenantAppUserTemplateList(appId, args);
	}

	/**
	 * 获取企业应用级用户模板分页列表
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 企业应用级用户模板分页列表
	 */
	@PostMapping("/get_tenant_app_user_template_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:read')")
	public Page<MetadataTenantAppUserTemplate> getUserPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															   @Validated @RequestBody GetTenantAppUserTemplateListArgs args) {
		String appId = principal.getAppId();
		return tenantAppUserTemplateSubappApiService.getUserPageList(appId, args);
	}


	/**
	 * 企业应用级用户模板接口，创建企业应用级用户模板
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_tenant_app_user_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:create_tenant_app_user_template')")
	public Optional<String> createTenantAppUserTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														@Validated @RequestBody CreateTenantAppUserTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppUserTemplateSubappApiService.createTenantAppUserTemplate(appId, args);
		return Optional.empty();
	}


	/**
	 * 创建账号并且创建企业应用级用户模板
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/create_account_and_tenant_app_user_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:create_tenant_app_user_template')")
	public Optional<String> createTenantAppUserTemplateAndAccount(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
																  @Validated @RequestBody CreateAccountAndTenantAppUserTemplateArgs args) {
		String appId = principal.getAppId();
		tenantAppUserTemplateSubappApiService.createAccountAndTenantAppUserTemplate(appId, args);
		return Optional.empty();
	}


	/**
	 * 修改企业应用级用户模板信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_user_template_info")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:modify_tenant_app_user_template_info')")
	public Optional<String> modifyTenantAppUserTemplateInfo(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															@Validated @RequestBody ModifyTenantAppUserTemplateInfoArgs args) {
		String appId = principal.getAppId();
		tenantAppUserTemplateSubappApiService.modifyTenantAppUserTemplateInfo(appId, args);
		return Optional.empty();
	}


	/**
	 * 修改企业应用级用户模板状态
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/modify_tenant_app_user_template_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:modify_tenant_app_user_template_status')")
	public Optional<String> modifyTenantAppUserTemplateStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody ModifyTenantAppUserTemplateStatusArgs args) {
		String appId = principal.getAppId();
		tenantAppUserTemplateSubappApiService.modifyTenantAppUserTemplateStatus(appId, args);
		return Optional.empty();
	}

	/**
	 * 删除企业应用级用户模板
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return 空
	 */
	@PostMapping("/delete_tenant_app_user_template")
	@PreAuthorize("hasAnyAuthority('app_admin', 'tenant_app_user_template:all', 'tenant_app_user_template:delete_tenant_app_user_template')")
	public Optional<String> deleteTenantAppUserTemplate(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														@Validated @RequestBody DeleteTenantAppUserTemplateArgs args) {
		String appId = principal.getAppId();

		tenantAppUserTemplateSubappApiService.deleteTenantAppUserTemplate(appId, args);
		return Optional.empty();
	}


}
