package io.github.lijiajia3515.cairo.auth.modules.wxmp.template_msg;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.WxmpTemplateMsg;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class WxmpTemplateMsgClientApiServiceImpl implements WxmpTemplateMsgClientApiService {

	private final WxmpTemplateMsgClientApiFeignClient wxmpTemplateMsgClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public WxmpTemplateMsgClientApiServiceImpl(WxmpTemplateMsgClientApiFeignClient wxmpTemplateMsgClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.wxmpTemplateMsgClientApiFeignClient = wxmpTemplateMsgClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public WxmpTemplateMsg getWxmpTemplateMsg(GetTemplateMsgArgs args) {
		try {
			ResponseEntity<BusinessResult<WxmpTemplateMsg>> businessResultResponseEntity = wxmpTemplateMsgClientApiFeignClient.getWxmpTemplateMsg(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("getWxmpTemplateMsg：", e);
			throw new ConflictBusinessException("获取微信模板消息失败");
		}
	}
}
