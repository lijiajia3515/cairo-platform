package io.github.lijiajia3515.cairo.auth.modules.sms.message;

import io.github.lijiajia3515.cairo.core.exception.ErrorBusinessException;
import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * client-api sms message fallback feign client
 */
public class SmsMsgClientApiFallbackFeignClient implements SmsMsgClientApiFeignClient {

	public static final RuntimeException EX = new ErrorBusinessException("系统服务-系统短信消息服务异常");


	@Override
	public ResponseEntity<BusinessResult<SmsMsgResult>> sendMsgByPhoneNumber(String authorization,String timestamp,String nonce,String sign,SendPhoneNumberSmsMsgArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<SmsMsgResult>>> sendBatchMessageByPhoneNumber(String authorization,String timestamp,String nonce,String sign,List<SendPhoneNumberSmsMsgArgs> argsList) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<SmsMsgResult>> sendMsgByAccount(String authorization,String timestamp,String nonce,String sign,SendAccountSmsMsgArgs args) {
		throw EX;
	}

	@Override
	public ResponseEntity<BusinessResult<List<SmsMsgResult>>> sendBatchMessageByAccount(String authorization,String timestamp,String nonce,String sign,List<SendAccountSmsMsgArgs> argsList) {
		throw EX;
	}
}
