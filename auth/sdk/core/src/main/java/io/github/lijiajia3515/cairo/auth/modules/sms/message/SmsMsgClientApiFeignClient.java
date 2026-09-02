package io.github.lijiajia3515.cairo.auth.modules.sms.message;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.modules.HeaderConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * client-api sms message feign client
 */
@FeignClient(
	contextId = "smsMsgClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/sms_msg",
	fallbackFactory = SmsMsgClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface SmsMsgClientApiFeignClient {

	/**
	 * 根据手机号发送短信
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_phone_number
	 *
	 * @param args 参数
	 * @return empty
	 */
	@PostMapping("/send_msg_by_phone_number")
	ResponseEntity<BusinessResult<SmsMsgResult>> sendMsgByPhoneNumber(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																			  @RequestHeader(HeaderConstants.TIMESTAMP_HEADER_NAME) String timestamp,
																			  @RequestHeader(HeaderConstants.NONCE_HEADER_NAME) String nonce,
																			  @RequestHeader(HeaderConstants.SIGN_HEADER_NAME) String sign,
																			  @RequestBody SendPhoneNumberSmsMsgArgs args);

	/**
	 * 批量发送短信消息根据手机号
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_phone_number
	 *
	 * @param argsList 参数
	 * @return empty
	 */
	@PostMapping("/send_batch_message_by_phone_number")
	ResponseEntity<BusinessResult<List<SmsMsgResult>>> sendBatchMessageByPhoneNumber(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																						 @RequestHeader(HeaderConstants.TIMESTAMP_HEADER_NAME) String timestamp,
																						 @RequestHeader(HeaderConstants.NONCE_HEADER_NAME) String nonce,
																						 @RequestHeader(HeaderConstants.SIGN_HEADER_NAME) String sign,
																						 @RequestBody List<SendPhoneNumberSmsMsgArgs> argsList);

	/**
	 * 根据手机号发送短信
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_account
	 *
	 * @param args 参数
	 * @return empty
	 */
	@PostMapping("/send_msg_by_account")
	ResponseEntity<BusinessResult<SmsMsgResult>> sendMsgByAccount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																		  @RequestHeader(HeaderConstants.TIMESTAMP_HEADER_NAME) String timestamp,
																		  @RequestHeader(HeaderConstants.NONCE_HEADER_NAME) String nonce,
																		  @RequestHeader(HeaderConstants.SIGN_HEADER_NAME) String sign,
																		  @RequestBody SendAccountSmsMsgArgs args);

	/**
	 * 批量发送短信消息根据手机号
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_account
	 *
	 * @param argsList 参数
	 * @return empty
	 */
	@PostMapping("/send_batch_message_by_account")
	ResponseEntity<BusinessResult<List<SmsMsgResult>>> sendBatchMessageByAccount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																					 @RequestHeader(HeaderConstants.TIMESTAMP_HEADER_NAME) String timestamp,
																					 @RequestHeader(HeaderConstants.NONCE_HEADER_NAME) String nonce,
																					 @RequestHeader(HeaderConstants.SIGN_HEADER_NAME) String sign,
																					 @RequestBody List<SendAccountSmsMsgArgs> argsList);
}
