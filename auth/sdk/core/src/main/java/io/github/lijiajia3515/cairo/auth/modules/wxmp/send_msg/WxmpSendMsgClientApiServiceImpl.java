package io.github.lijiajia3515.cairo.auth.modules.wxmp.send_msg;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.template_msg.SendWxmpMsgByArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class WxmpSendMsgClientApiServiceImpl implements WxmpSendMsgClientApiService {

	private final WxmpSendMsgClientApiFeignClient wxmpSendMsgClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public WxmpSendMsgClientApiServiceImpl(WxmpSendMsgClientApiFeignClient wxmpSendMsgClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.wxmpSendMsgClientApiFeignClient = wxmpSendMsgClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public String sendMsgByAppUser(SendWxmpMsgByArgs args) {
		try {
			ResponseEntity<BusinessResult<String>> businessResultResponseEntity = wxmpSendMsgClientApiFeignClient.sendMsgByAppUser(cairoOAuthClientSdkService.getHeaderAuthorization(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("sendMsgByAppUser：", e);
			throw new ConflictBusinessException("应用用户发送微信消息失败");
		}
	}

	@Override
	public Optional<String> sendMsg(SendWxmpMsgArgs args) {
		try {
			ResponseEntity<BusinessResult<Optional<String>>> businessResultResponseEntity = wxmpSendMsgClientApiFeignClient.sendMsg(cairoOAuthClientSdkService.getHeaderAuthorization(), args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(null);
		} catch (Exception e) {
			log.info("sendMsg：", e);
			throw new ConflictBusinessException("发送微信消息失败");
		}
	}
}
