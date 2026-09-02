package io.github.lijiajia3515.cairo.auth.modules.account.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 发送注册成功短信 处理类
 */
@Slf4j
@Component
public class SendMsgByCreatedAccountQueueHandler {

	private final CairoSecurityProperties cairoSecurityProperties;
	private final ObjectMapper objectMapper;
	private final SmsMsgClientApiService smsMsgClientApiService;

	public SendMsgByCreatedAccountQueueHandler(CairoSecurityProperties cairoSecurityProperties, ObjectMapper objectMapper, SmsMsgClientApiService smsMsgClientApiService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.objectMapper = objectMapper;
		this.smsMsgClientApiService = smsMsgClientApiService;
	}

	/**
	 * 业务处理
	 *
	 * @param headers headers
	 * @param payload payload
	 * @param message message
	 * @param channel channel
	 */
	@RabbitListener(
		queues = {"#{sendMsgByCreatedAccountQueue.getName()}"}
	)
	public void sendMsgByCreatedAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[send_account_sms] message handler start");
			CreatedAccountMessage createdAccountMessage = objectMapper.readValue(payload, CreatedAccountMessage.class);
			log.info("注册用户：AccountId: {} Name: {} PhoneNumber: {} Username: {} Password: {} EventAccountId: {} EventTime: {}",
				createdAccountMessage.getAccountId(),
				createdAccountMessage.getNickname(),
				createdAccountMessage.getPhoneNumber(),
				createdAccountMessage.getUsername(),
				createdAccountMessage.getPassword(),
				createdAccountMessage.getEventAccountId(),
				createdAccountMessage.getEventTime()
			);
			if (createdAccountMessage.getPhoneNumber() != null) {
				Map<String, String> smsMesssageArgs = new HashMap<>();
				smsMesssageArgs.put(CairoAuthSmsConstants.RegisterAccountSuccess.PARAM_NAME, Optional.ofNullable(createdAccountMessage.getNickname()).orElse(createdAccountMessage.getPhoneNumber()));
				smsMesssageArgs.put(CairoAuthSmsConstants.RegisterAccountSuccess.PARAM_USERNAME, Optional.ofNullable(createdAccountMessage.getUsername()).orElse(createdAccountMessage.getPhoneNumber()));
				smsMesssageArgs.put(CairoAuthSmsConstants.RegisterAccountSuccess.PARAM_PASSWORD, Optional.ofNullable(createdAccountMessage.getPassword()).orElse("******"));

				// 调用系统服务，发送短信消息
				smsMsgClientApiService.sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
					.phoneNumber(createdAccountMessage.getPhoneNumber())
					.appId(cairoSecurityProperties.getCairoAppId())
					.bizId(CairoAuthSmsConstants.RegisterAccountSuccess.BIZ_ID)
					.args(smsMesssageArgs)
					.build()
				);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[send_account_sms] message handler end");
		} catch (RuntimeException e) {
			log.info("[send_account_sms] message handler end ", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误，重新投递一次
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}

	}
}
