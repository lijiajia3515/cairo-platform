package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.framework.context.CairoContext;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthSubappUserPrincipal;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.CreateWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.DeleteWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.GetWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.ModifyWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.ModifyWxmpProviderStatusArgs;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider.MetadataWxmpProvider;
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
 * [cairo_web_manage/api] wxmp provider controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/cairo_web_manage_api/wxmp_provider")
@CairoSecurity(type = CairoSecurityType.CAIRO_WEB_MANAGE_USER)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpProviderCairoWebManageApiController {
	private final WxmpProviderCairoWebManageApiService wxmsTemplateCairoEndpointUserApiService;


	/**
	 * 创建微信公众号连接配置 接口
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/create_wxmp_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:create_wxmp_provider')")
	@CairoContext
	public Optional<String> createWxmpProvider(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody CreateWxmpProviderArgs args) {
		wxmsTemplateCairoEndpointUserApiService.createWxmpProvider(args);
		return Optional.empty();
	}

	/**
	 * 修改微信公众号连接配置 接口
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/modify_wxmp_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:modify_wxmp_provider')")
	@CairoContext
	public Optional<String> modifyWxmpProvider(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody ModifyWxmpProviderArgs args) {
		wxmsTemplateCairoEndpointUserApiService.modifyWxmpProvider(args);
		return Optional.empty();
	}

	/**
	 * 修改微信公众号连接配置状态 接口
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/modify_wxmp_provider_status")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:modify_wxmp_provider_status')")
	@CairoContext
	public Optional<String> modifyWxmpProviderStatus(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody ModifyWxmpProviderStatusArgs args) {
		wxmsTemplateCairoEndpointUserApiService.modifyWxmpProviderStatus(args);
		return Optional.empty();
	}

	/**
	 * 删除微信公众号连接配置
	 *
	 * @param args args
	 * @return empty
	 */
	@PostMapping("/delete_wxmp_provider")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:delete_wxmp_provider')")
	@CairoContext
	public Optional<String> deleteWxmpProvider(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,@Validated @RequestBody DeleteWxmpProviderArgs args) {
		wxmsTemplateCairoEndpointUserApiService.deleteWxmpProvider(args);
		return Optional.empty();
	}

	/**
	 * 获取微信公众号连接配置列表
	 *
	 * @param args args
	 * @return 微信公众号连接配置列表
	 */
	@PostMapping("/get_wxmp_provider_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:read')")
	@CairoContext
	public List<MetadataWxmpProvider> getWxmpProviderList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
														  @Validated @RequestBody GetWxmpProviderArgs args) {
		String appId = principal.getAppId();
		return wxmsTemplateCairoEndpointUserApiService.getWxmpProviderList(appId, args);
	}

	/**
	 * 获取微信公众号连接配置分页列表
	 *
	 * @param args args
	 * @return 微信公众号连接配置分页列表
	 */
	@PostMapping("/get_wxmp_provider_page_list")
	@PreAuthorize("hasAnyAuthority('app_admin', 'wxmp_provider:all', 'wxmp_provider:read')")
	@CairoContext
	public Page<MetadataWxmpProvider> getWxmpProviderPageList(@AuthenticationPrincipal CairoOAuthSubappUserPrincipal principal,
															  @Validated @RequestBody GetWxmpProviderArgs args) {
		String appId = principal.getAppId();
		return wxmsTemplateCairoEndpointUserApiService.getWxmpProviderPageList(appId, args);
	}
}
