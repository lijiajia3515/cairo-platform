
package io.github.lijiajia3515.cairo.auth.modules.app_user.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
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
 * 发送注册应用用户成功消息 队列处理器
 */

@Slf4j
@Component
public class SendMsgByCreatedAppUserQueueHandler {

	private final SmsMsgClientApiService smsMsgClientApiService;

	private final ObjectMapper objectMapper;

	private final MongoTemplate readMongoTemplate;

	public SendMsgByCreatedAppUserQueueHandler(SmsMsgClientApiService smsMsgClientApiService,
												   ObjectMapper objectMapper,@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.smsMsgClientApiService = smsMsgClientApiService;
		this.objectMapper = objectMapper;
		this.readMongoTemplate = readMongoTemplate;
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
		queues = {"#{sendMsgByCreatedAppUserQueue.getName()}"}
	)
	public void sendMsgByCreatedAppUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedAppUserMessage createdAppUserMessage = objectMapper.readValue(payload, CreatedAppUserMessage.class);
			log.debug("[send_msg_by_created_app_user] message handler start: AppId: {} AccountId: {} UserId: {} Nickname: {}",
				createdAppUserMessage.getAppId(),
				createdAppUserMessage.getAccountId(),
				createdAppUserMessage.getUserId(),
				createdAppUserMessage.getNickname()
			);

			if (createdAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_created_app_user] message handler end: AppId: {} AccountId: {} UserId: {} Nickname: {} ",
					createdAppUserMessage.getAppId(),
					createdAppUserMessage.getAccountId(),
					createdAppUserMessage.getUserId(),
					createdAppUserMessage.getNickname()
				);
				return;
			}
			//账号查询
			Query accountQuery = Query.query(Criteria
				.where(AccountMongodb.FIELD.ACCOUNT_ID).is(createdAppUserMessage.getAccountId()));
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			String smsAccount = String.format("%s(%s)", Optional.ofNullable(account).map(AccountMongodb::getNickname).filter(x->!x.isBlank()).orElse("****"), createdAppUserMessage.getAccountId());
			String smsUser = String.format("%s(%s)", Optional.ofNullable(createdAppUserMessage.getNickname()).orElse("****"), createdAppUserMessage.getUserId());

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(createdAppUserMessage.getAccountId())
				.appId(createdAppUserMessage.getAppId())
				.bizId(CairoAuthSmsConstants.RegisterAppUserSuccess.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.RegisterAppUserSuccess.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.RegisterAppUserSuccess.PARAM_USER, smsUser);
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_created_app_user] message handler end:  AppId: {} UserId: {}  AccountId: {} ", createdAppUserMessage.getAppId(), createdAppUserMessage.getUserId(), createdAppUserMessage.getAccountId());
		} catch (Exception e) {
			log.info("[send_msg_by_created_app_user] handler error", e);
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
