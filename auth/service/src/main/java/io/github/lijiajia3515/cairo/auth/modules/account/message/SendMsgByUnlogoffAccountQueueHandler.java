package io.github.lijiajia3515.cairo.auth.modules.account.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.message.account.UnlogoffAccountMessage;
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
public class SendMsgByUnlogoffAccountQueueHandler {
	private final CairoSecurityProperties cairoSecurityProperties;

	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;

	public SendMsgByUnlogoffAccountQueueHandler(CairoSecurityProperties cairoSecurityProperties,
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
	 * @throws java.io.IOException 1
	 */
	@RabbitListener(
		queues = {"#{sendMsgByUnlogoffAccountQueue.getName()}"}
	)
	public void sendMsgByUnlogoffAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			UnlogoffAccountMessage unlogoffAccountMessage = objectMapper.readValue(payload, UnlogoffAccountMessage.class);
			log.debug("[send_msg_by_unlogoff_account] message handler start: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", unlogoffAccountMessage.getAccountId(), unlogoffAccountMessage.getNickname(), unlogoffAccountMessage.getPhoneNumber(), unlogoffAccountMessage.getEmail(), unlogoffAccountMessage.getUsername());


			if (unlogoffAccountMessage.getPhoneNumber() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_unlogoff_account] message handler end: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", unlogoffAccountMessage.getAccountId(), unlogoffAccountMessage.getNickname(), unlogoffAccountMessage.getPhoneNumber(), unlogoffAccountMessage.getEmail(), unlogoffAccountMessage.getUsername());
				return;
			}

			String smsAccount = String.format("%s(%s)", Optional.of(unlogoffAccountMessage).map(UnlogoffAccountMessage::getNickname).filter(x -> !x.isBlank()).orElse("****"), Optional.of(unlogoffAccountMessage).map(UnlogoffAccountMessage::getAccountId).filter(x -> !x.isBlank()).orElse("****"));
			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByPhoneNumber(SendPhoneNumberSmsMsgArgs.builder()
				.phoneNumber(unlogoffAccountMessage.getPhoneNumber())
				.appId(cairoSecurityProperties.getCairoAppId())
				.bizId(CairoAuthSmsConstants.UnlogoffAccount.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.UnlogoffAccount.PARAM_ACCOUNT, smsAccount);
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_unlogoff_account] message handler end: AccountId: {} NickName: {} PhoneNumber: {} Email: {} Username: {}", unlogoffAccountMessage.getAccountId(), unlogoffAccountMessage.getNickname(), unlogoffAccountMessage.getPhoneNumber(), unlogoffAccountMessage.getEmail(), unlogoffAccountMessage.getUsername());
		} catch (Exception e) {
			log.info("[send_msg_by_unlogoff_account] handler error", e);
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
