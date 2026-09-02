package io.github.lijiajia3515.cairo.auth.modules.sms.message.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgArgs;
import io.github.lijiajia3515.cairo.auth.modules.sms.message.send_msg.SendMsgSmsService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sms.template.SmsTemplate;
import io.github.lijiajia3515.cairo.auth.modules.sms.template.SmsTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.sms.template.SendPhoneNumberSmsMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * 发送消息队列处理
 */
@Slf4j
@Component
public class SendMsgByPhoneNumberQueueHandler {

	private final SmsTemplateCommonService smsTemplateCommonService;
	private final SendMsgSmsService sendMsgSmsService;
	private final ObjectMapper objectMapper;

	public SendMsgByPhoneNumberQueueHandler(SmsTemplateCommonService smsTemplateCommonService, SendMsgSmsService sendMsgSmsService, ObjectMapper objectMapper) {
		this.smsTemplateCommonService = smsTemplateCommonService;
		this.sendMsgSmsService = sendMsgSmsService;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{sendMsgByPhoneNumberQueue.getName()}"}
	)
	public void sendMsgByPhoneNumberQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			SendPhoneNumberSmsMsg sendPhoneNumberSmsMsg = objectMapper.readValue(payload, SendPhoneNumberSmsMsg.class);
			log.debug("[send_msg_by_phone_number] message handler start");
			SmsTemplate smsTemplate = smsTemplateCommonService.getSmsTemplate(sendPhoneNumberSmsMsg.getAppId(), sendPhoneNumberSmsMsg.getBizId());
			if (smsTemplate == null) {
				log.info("消息模板不存在： AppId: {} BizId: {}", sendPhoneNumberSmsMsg.getAppId(), sendPhoneNumberSmsMsg.getBizId());
				return;
			}

			// 异步发送消息
			sendMsgSmsService.sendMsg(SendMsgArgs.builder()
				.phoneNumber(sendPhoneNumberSmsMsg.getPhoneNumber())
				.bizSign(sendPhoneNumberSmsMsg.getSign())
				.bizArgs(sendPhoneNumberSmsMsg.getParams())
				.smsTemplate(smsTemplate)
				.build()
			);

			// 消费成功！
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_phone_number] message handler end");
		} catch (Exception e) {
			log.info("[send_msg_by_phone_number] error: ", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
		log.info("[send_msg_by_phone_number] end");
	}
}
