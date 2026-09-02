package io.github.lijiajia3515.cairo.auth.modules.account.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.message.account.LogoffAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
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
 * 给已注销的账号发送消息 队列处理器
 */
@Slf4j
@Component
public class SendMsgByLogoffAccountQueueHandler {
	private final CairoSecurityProperties cairoSecurityProperties;
	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;

	public SendMsgByLogoffAccountQueueHandler(CairoSecurityProperties cairoSecurityProperties,
												  SmsMsgClientApiService smsMsgClientApiService,
												  ObjectMapper objectMapper) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.smsMsgClientApiService = smsMsgClientApiService;
		this.objectMapper = objectMapper;
	}

	/**
	 * 业务队列
	 *
	 * @param headers headers
	 * @param payload payload
	 * @param message message
	 * @param channel channel
	 * @throws IOException 1
	 */
	@RabbitListener(
		queues = {"#{sendMsgByLogoffAccountQueue.getName()}"}
	)
	public void sendMsgByLogoffAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffAccountMessage logoffAccountMessage = objectMapper.readValue(payload, LogoffAccountMessage.class);
			log.debug("[send_msg_by_logoff_account] message handler start: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", logoffAccountMessage.getAccountId(), logoffAccountMessage.getNickname(), logoffAccountMessage.getPhoneNumber(), logoffAccountMessage.getEmail(), logoffAccountMessage.getUsername());


			if (logoffAccountMessage.getPhoneNumber() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_logoff_account] message handler end: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", logoffAccountMessage.getAccountId(), logoffAccountMessage.getNickname(), logoffAccountMessage.getPhoneNumber(), logoffAccountMessage.getEmail(), logoffAccountMessage.getUsername());
				return;
			}

			String smsAccount = String.format("%s(%s)", Optional.of(logoffAccountMessage).map(LogoffAccountMessage::getNickname).filter(x -> !x.isBlank()).orElse("****"), Optional.of(logoffAccountMessage).map(LogoffAccountMessage::getAccountId).filter(x -> !x.isBlank()).orElse("****"));
			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
				.phoneNumber(logoffAccountMessage.getPhoneNumber())
				.appId(cairoSecurityProperties.getCairoAppId())
				.bizId(CairoAuthSmsConstants.LogoffAccount.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.LogoffAccount.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.LogoffAccount.PARAM_DAY, "3天");
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_logoff_account] message handler end: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", logoffAccountMessage.getAccountId(), logoffAccountMessage.getNickname(), logoffAccountMessage.getPhoneNumber(), logoffAccountMessage.getEmail(), logoffAccountMessage.getUsername());
		} catch (Exception e) {
			log.info("[send_msg_by_logoff_account] handler error", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误，重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
	}
}
