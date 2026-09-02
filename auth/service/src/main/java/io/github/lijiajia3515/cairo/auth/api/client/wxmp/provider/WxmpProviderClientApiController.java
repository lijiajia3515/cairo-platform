package io.github.lijiajia3515.cairo.auth.api.client.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthClientPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurity;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityType;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpJsApiTicketArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpProviderAuthArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpProviderInfoArgs;
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
 * [client/api] wxmp_provider controller
 * 公众号认证
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/client_api/wxmp_provider")
@CairoSecurity(type = CairoSecurityType.CLIENT)
@BusinessResultBody
@RequiredArgsConstructor
public class WxmpProviderClientApiController {

	private final WxmpProviderClientApiService wxmpProviderClientApiService;

	/**
	 * 获取公众号认证信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_provider_info")
	@PreAuthorize("hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')")
	public WxmpProviderInfo getProviderInfo(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetWxmpProviderInfoArgs args) {
		return wxmpProviderClientApiService.getProviderInfo(args);
	}

	/**
	 * 获取公众号认证信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_wxmp_openid")
	@PreAuthorize("hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')")
	public WxmpOpenIdInfo getWxmpOpenId(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetWxmpProviderAuthArgs args) {
		return wxmpProviderClientApiService.getWxmpOpenid(args);
	}

	/**
	 * 获取公众号jsApiTicket信息
	 *
	 * @param principal 凭证
	 * @param args      参数
	 * @return jsapi_ticket
	 */
	@PostMapping("/js_api_ticket")
	@PreAuthorize("hasAnyAuthority('wxmp_provider:all', 'wxmp_provider:read')")
	public WxmpJsApiTicket jsApiTicket(@AuthenticationPrincipal CairoOAuthClientPrincipal principal, @Validated @RequestBody GetWxmpJsApiTicketArgs args) {
		return wxmpProviderClientApiService.jsApiTicket(args);
	}
}
