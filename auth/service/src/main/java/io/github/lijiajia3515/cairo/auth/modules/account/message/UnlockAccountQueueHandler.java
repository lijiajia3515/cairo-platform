package io.github.lijiajia3515.cairo.auth.modules.account.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.account.UnlockAccountMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * 解锁账号 队列处理器
 */
@Slf4j
@Component
public class UnlockAccountQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public UnlockAccountQueueHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
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
		queues = {"#{unlockAccountQueueQueue.getName()}"}
	)
	public void unlockAccountQueueQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			UnlockAccountMessage unlockAccountMessage = objectMapper.readValue(payload, UnlockAccountMessage.class);
			log.debug("[unlock_account] message handler start: AccountId: {} LockTime: {} UnLockTime: {}",
				unlockAccountMessage.getAccountId(), unlockAccountMessage.getLockedTime(), unlockAccountMessage.getUnlockTime()
			);

			if (unlockAccountMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[unlock_account] message handler end: AccountId: {} LockTime: {} UnLockTime: {}",
					unlockAccountMessage.getAccountId(), unlockAccountMessage.getLockedTime(), unlockAccountMessage.getUnlockTime()
				);
				return;
			}

			Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(unlockAccountMessage.getAccountId())
				.and(AccountMongodb.FIELD.LOCKED).is(true)
				.and(AccountMongodb.FIELD.LOCKED_TIME).lte(unlockAccountMessage.getLockedTime());

			Query query = Query.query(criteria);
			Update update = Update.update(AccountMongodb.FIELD.LOCKED, false);
			update.set(AccountMongodb.FIELD.LOCKED_TIME, null);
			update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			log.debug("updateResult: {}", updateResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[unlock_account] message handler end: AccountId: {} LockTime: {} UnLockTime: {}",
				unlockAccountMessage.getAccountId(), unlockAccountMessage.getLockedTime(), unlockAccountMessage.getUnlockTime()
			);
		} catch (Exception e) {
			log.info("[unlock_account] handler error", e);
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
