package io.github.lijiajia3515.cairo.auth.modules.app_user.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffSuccessAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 给应用用户发送注销成功消息 队列处理器
 */
@Slf4j
@Component
public class SendMsgByLogoffSuccessAppUserQueueHandler {

	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;
	private final MongoTemplate readMongoTemplate;

	public SendMsgByLogoffSuccessAppUserQueueHandler(SmsMsgClientApiService smsMsgClientApiService,
														 ObjectMapper objectMapper,
														 @Qualifier("readMongoTemplate")MongoTemplate readMongoTemplate) {
		this.smsMsgClientApiService = smsMsgClientApiService;
		this.objectMapper = objectMapper;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * @param headers
	 * @param payload
	 * @param message
	 * @param channel
	 * @throws IOException
	 */
	@RabbitListener(
		queues = {"#{sendMsgByLogoffSuccessAppUserQueue.getName()}"}
	)
	public void sendMsgByLogoffSuccessAppUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffSuccessAppUserMessage logoffSuccessAppUserMessage = objectMapper.readValue(payload, LogoffSuccessAppUserMessage.class);
			log.debug("[send_msg_by_logoff_success_app_user] message handler start:  AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
			if (logoffSuccessAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_logoff_success_app_user] message handler end: AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
				return;
			}

			//账号查询
			Query accountQuery = Query.query(Criteria
				.where(AccountMongodb.FIELD.ACCOUNT_ID).is(logoffSuccessAppUserMessage.getAccountId()));
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);


			String smsAccount = String.format("%s(%s)",  Optional.ofNullable(account).map(AccountMongodb::getNickname).filter(x->!x.isBlank()).orElse("****"), logoffSuccessAppUserMessage.getAccountId());
			String smsUser = String.format("%s(%s)", Optional.ofNullable(logoffSuccessAppUserMessage.getNickname()).orElse("****"), logoffSuccessAppUserMessage.getUserId());

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(logoffSuccessAppUserMessage.getAccountId())
				.appId(logoffSuccessAppUserMessage.getAppId())
				.bizId(CairoAuthSmsConstants.LogoffAppUserSuccess.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.LogoffAppUserSuccess.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.LogoffAppUserSuccess.PARAM_USER, smsUser);
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_logoff_success_app_user] message handler end: AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
		} catch (Exception e) {
			log.info("[send_msg_by_logoff_success_app_user] handler error", e);
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
