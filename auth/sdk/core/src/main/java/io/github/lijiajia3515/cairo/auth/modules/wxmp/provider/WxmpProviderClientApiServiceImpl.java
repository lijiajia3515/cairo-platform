package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class WxmpProviderClientApiServiceImpl implements WxmpProviderClientApiService {

	private final WxmpProviderClientApiFeignClient wxmpProviderClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public WxmpProviderClientApiServiceImpl(WxmpProviderClientApiFeignClient wxmpProviderClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.wxmpProviderClientApiFeignClient = wxmpProviderClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public WxmpProviderInfo getProviderInfo(GetWxmpProviderInfoArgs args) {
		try {
			ResponseEntity<BusinessResult<WxmpProviderInfo>> businessResultResponseEntity = wxmpProviderClientApiFeignClient.getProviderInfo(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("getWxmpProvider：", e);
			throw new ConflictBusinessException("获取微信公众号信息失败");
		}
	}

	@Override
	public WxmpOpenIdInfo getWxmpOpenid(GetWxmpProviderAuthArgs args) {
		try {
		ResponseEntity<BusinessResult<WxmpOpenIdInfo>> businessResultResponseEntity = wxmpProviderClientApiFeignClient.getWxmpOpenid(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
		return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
	} catch (Exception e) {
		log.info("getWxmpProvider：", e);
		throw new ConflictBusinessException("获取微信公众号认证信息失败");
	}
	}

	@Override
	public WxmpJsApiTicket jsApiTicket(GetWxmpJsApiTicketArgs args) {
		try {
			ResponseEntity<BusinessResult<WxmpJsApiTicket>> businessResultResponseEntity = wxmpProviderClientApiFeignClient.jsApiTicket(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("jsApiTicket：", e);
			throw new ConflictBusinessException("获取微信公众号jsApiTicket信息失败");
		}
	}
}
