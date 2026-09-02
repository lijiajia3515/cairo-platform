package io.github.lijiajia3515.cairo.auth.modules.wxmp.provider;


import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;
import org.springframework.http.ResponseEntity;


public class WxmpProviderClientApiFallbackFeignClient implements WxmpProviderClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-微信公众号子应用认证故障");


	@Override
	public ResponseEntity<BusinessResult<WxmpProviderInfo>> getProviderInfo(String authorization, GetWxmpProviderInfoArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<WxmpOpenIdInfo>> getWxmpOpenid(String authorization, GetWxmpProviderAuthArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<WxmpJsApiTicket>> jsApiTicket(String authorization, GetWxmpJsApiTicketArgs args) {
		throw EX;
	}
}
