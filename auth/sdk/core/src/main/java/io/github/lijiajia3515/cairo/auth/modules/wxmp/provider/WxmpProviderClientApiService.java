package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;


public interface WxmpProviderClientApiService {

	/**
	 * 获取公共号信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	WxmpProviderInfo getProviderInfo(GetWxmpProviderInfoArgs args);


	/**
	 * 获取公共号认证信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	WxmpOpenIdInfo getWxmpOpenid(GetWxmpProviderAuthArgs args);

	/**
	 * 获取公众号jsApiTicket信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return WxmpProviderJsApiTicket
	 */
	WxmpJsApiTicket jsApiTicket(GetWxmpJsApiTicketArgs args);

}
