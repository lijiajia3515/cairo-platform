package io.github.lijiajia3515.cairo.auth.modules.sms.message;

import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;

import java.util.List;

public interface SmsMsgClientApiService {

	/**
	 * 根据手机号发送短信
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_phone_number
	 *
	 * @param args 参数
	 * @return empty
	 */
	SmsMsgResult sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs args);

	/**
	 * 批量发送短信消息根据手机号
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_phone_number
	 *
	 * @param argsList 参数
	 * @return empty
	 */
	List<SmsMsgResult> sendBatchMessageByPhoneNumber(List<SendPhoneNumberSmsMsgArgs> argsList);

	/**
	 * 根据手机号发送短信
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_account
	 *
	 * @param args 参数
	 * @return empty
	 */
	SmsMsgResult sendMsgByAccount(SendAccountSmsMsgArgs args);

	/**
	 * 批量发送短信消息根据手机号
	 * 需要权限： sms_msg:all ｜ sms_msg:send_msg_by_account
	 *
	 * @param argsList 参数
	 * @return empty
	 */
	List<SmsMsgResult> sendBatchMessageByAccount(List<SendAccountSmsMsgArgs> argsList);
}
