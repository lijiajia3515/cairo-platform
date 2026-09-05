package io.github.lijiajia3515.cairo.auth.modules.app_user.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffAppUserMessage;
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
 * 给应用级用户发送注销通知消息 队列处理器
 */

@Slf4j
@Component
public class SendMsgByLogoffAppUserQueueHandler {

	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;
	private final MongoTemplate readMongoTemplate;

	public SendMsgByLogoffAppUserQueueHandler(
		SmsMsgClientApiService smsMsgClientApiService,
		ObjectMapper objectMapper,
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
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
		queues = {"#{sendMsgByLogoffAppUserQueue.getName()}"}
	)
	public void sendMsgByLogoffAppUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffAppUserMessage logoffAppUserMessage = objectMapper.readValue(payload, LogoffAppUserMessage.class);
			log.debug("[send_msg_by_logoff_app_user] message handler start: AppId: {} UserId: {} AccountId: {}",
				logoffAppUserMessage.getAppId(),
				logoffAppUserMessage.getUserId(),
				logoffAppUserMessage.getAccountId());

			if (logoffAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_logoff_app_user] message handler end: AppId: {} UserId: {} AccountId: {}",
					logoffAppUserMessage.getAppId(),
					logoffAppUserMessage.getUserId(),
					logoffAppUserMessage.getAccountId()
				);
				return;
			}

           //账号查询
			Query accountQuery = Query.query(Criteria
				.where(AccountMongodb.FIELD.ACCOUNT_ID).is(logoffAppUserMessage.getAccountId()));
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			String smsAccount = String.format("%s(%s)",  Optional.ofNullable(account).map(AccountMongodb::getNickname).filter(x->!x.isBlank()).orElse("****"), logoffAppUserMessage.getAccountId());
			String smsUser = String.format("%s(%s)", Optional.ofNullable(logoffAppUserMessage.getNickname()).orElse("****"), logoffAppUserMessage.getUserId());

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(logoffAppUserMessage.getAccountId())
				.appId(logoffAppUserMessage.getAppId())
				.bizId(CairoAuthSmsConstants.LogoffAppUser.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.LogoffAppUser.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.LogoffAppUser.PARAM_USER, smsUser);
					put(CairoAuthSmsConstants.LogoffAppUser.PARAM_DAY, "3天");
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_logoff_app_user] message handler end: AppId: {} UserId: {} AccountId: {}",
				logoffAppUserMessage.getAppId(),
				logoffAppUserMessage.getUserId(),
				logoffAppUserMessage.getAccountId()
			);
		} catch (Exception e) {
			log.info("[send_msg_by_logoff_app_user] handler error", e);
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
