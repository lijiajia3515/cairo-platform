package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


/**
 * wxmpProvider Client Feign client
 */
@FeignClient(
	contextId = "wxmpProviderClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/wxmp_provider",
	fallbackFactory = WxmpProviderClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface WxmpProviderClientApiFeignClient {

	/**
	 * 获取公共号信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_provider_info")
	ResponseEntity<BusinessResult<WxmpProviderInfo>> getProviderInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody GetWxmpProviderInfoArgs args);

	/**
	 * 获取公共号认证信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return empty
	 */
	@PostMapping("/get_wxmp_openid")
	ResponseEntity<BusinessResult<WxmpOpenIdInfo>> getWxmpOpenid(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody GetWxmpProviderAuthArgs args);

	/**
	 * 获取公众号jsApiTicket信息
	 * 权限:wxmp_provider:read | wxmp_provider:all
	 *
	 * @param args      参数
	 * @return WxmpProviderJsApiTicket
	 */
	@PostMapping("/js_api_ticket")
	ResponseEntity<BusinessResult<WxmpJsApiTicket>> jsApiTicket(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @Validated @RequestBody GetWxmpJsApiTicketArgs args);

}
