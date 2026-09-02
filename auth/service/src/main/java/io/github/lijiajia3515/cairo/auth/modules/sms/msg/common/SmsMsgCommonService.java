package io.github.lijiajia3515.cairo.auth.modules.sms.message.common;

import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.common.args.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgSmsService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.modules.sms.template.SmsTemplateCommonService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Component
public class SmsMsgCommonService {
	private final SmsTemplateCommonService smsTemplateCommonService;
	private final SendMsgSmsService sendMsgSmsService;

	public SmsMsgCommonService(SmsTemplateCommonService smsTemplateCommonService,
								   SendMsgSmsService sendMsgSmsService) {
		this.smsTemplateCommonService = smsTemplateCommonService;
		this.sendMsgSmsService = sendMsgSmsService;
	}

	@NewSpan
	@BizLog(
		bizId = "sms_msg:send_msg_by_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs args) {
		SendMsgArgs sendMsgArgs = null;
		try {
			SmsTemplate smsTemplate = smsTemplateCommonService.getSmsTemplate(args.getAppId(), args.getBizId());
			SendMsgArgs.SendMsgArgsBuilder builder = SendMsgArgs.builder();
			if (args.getMsgId() != null) {
				builder.msgId(args.getMsgId());
			}

			builder
				.appId(args.getAppId())
				.phoneNumber(args.getPhoneNumber())
				.time(args.getTime())
				.bizId(args.getBizId())
				.bizSign(args.getSign())
				.bizArgs(args.getArgs())
				.smsTemplate(smsTemplate)
				.build();
			sendMsgArgs = builder.build();

			// 异步发送消息
			sendMsgSmsService.sendMsg(sendMsgArgs);
		} catch (Exception e) {
			log.warn("send message by phone number: ", e);
		}
	}
}
