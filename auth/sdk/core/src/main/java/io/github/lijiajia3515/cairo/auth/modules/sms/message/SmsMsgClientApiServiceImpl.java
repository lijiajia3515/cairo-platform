package io.github.lijiajia3515.cairo.auth.modules.sms.message;

import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.framework.sdk.CairoOAuthClientSdkService;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignResp;
import io.github.lijiajia3515.cairo.auth.framework.sdk.sign.SignSdkTools;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SmsMsgClientApiServiceImpl implements SmsMsgClientApiService {

	private final SmsMsgClientApiFeignClient smsMsgClientApiFeignClient;
	private final CairoOAuthClientSdkService cairoOAuthClientSdkService;

	public SmsMsgClientApiServiceImpl(SmsMsgClientApiFeignClient smsMsgClientApiFeignClient, CairoOAuthClientSdkService cairoOAuthClientSdkService) {
		this.smsMsgClientApiFeignClient = smsMsgClientApiFeignClient;
		this.cairoOAuthClientSdkService = cairoOAuthClientSdkService;
	}

	@Override
	public SmsMsgResult sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs args) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<SmsMsgResult>> businessResultResponseEntity = smsMsgClientApiFeignClient.sendMsgByPhoneNumber(cairoOAuthClientSdkService.getHeaderAuthorization(),sign.getTimestamp(),sign.getNonce(),sign.getSign(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(SmsMsgResult.builder().build());
		} catch (Exception e) {
			log.info("sendMsgByPhoneNumber：", e);
			throw new ConflictBusinessException("根据手机号发送短信失败");
		}
	}

	@Override
	public List<SmsMsgResult> sendBatchMessageByPhoneNumber(List<SendPhoneNumberSmsMsgArgs> argsList) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<List<SmsMsgResult>>> businessResultResponseEntity = smsMsgClientApiFeignClient.sendBatchMessageByPhoneNumber(cairoOAuthClientSdkService.getHeaderAuthorization(),sign.getTimestamp(),sign.getNonce(),sign.getSign(),argsList);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("sendBatchMessageByPhoneNumber：", e);
			throw new ConflictBusinessException("批量发送短信消息根据手机号失败");
		}
	}

	@Override
	public SmsMsgResult sendMsgByAccount(SendAccountSmsMsgArgs args) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<SmsMsgResult>> businessResultResponseEntity = smsMsgClientApiFeignClient.sendMsgByAccount(cairoOAuthClientSdkService.getHeaderAuthorization(),sign.getTimestamp(),sign.getNonce(),sign.getSign(),args);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(SmsMsgResult.builder().build());
		} catch (Exception e) {
			log.info("sendMsgByAccount：", e);
			throw new ConflictBusinessException("根据手机号发送短信");
		}
	}

	@Override
	public List<SmsMsgResult> sendBatchMessageByAccount(List<SendAccountSmsMsgArgs> argsList) {
		try {
			SignResp sign = SignSdkTools.sign();
			ResponseEntity<BusinessResult<List<SmsMsgResult>>> businessResultResponseEntity = smsMsgClientApiFeignClient.sendBatchMessageByAccount(cairoOAuthClientSdkService.getHeaderAuthorization(),sign.getTimestamp(),sign.getNonce(),sign.getSign(),argsList);
			return Optional.ofNullable(businessResultResponseEntity.getBody()).map(BusinessResult::getData).orElse(Collections.emptyList());
		} catch (Exception e) {
			log.info("sendBatchMessageByAccount：", e);
			throw new ConflictBusinessException("批量发送短信消息根据手机号失败");
		}
	}
}
