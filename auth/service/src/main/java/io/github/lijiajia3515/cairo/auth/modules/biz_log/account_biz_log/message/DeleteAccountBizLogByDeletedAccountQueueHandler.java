package io.github.lijiajia3515.cairo.auth.modules.biz_log.account_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogAccountMongodb;
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
import java.util.List;
import java.util.Map;


/**
 * 删除账号级业务日志 根据 已删除账号 队列实现
 */
@Slf4j
@Component
public class DeleteAccountBizLogByDeletedAccountQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteAccountBizLogByDeletedAccountQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteAccountBizLogByDeletedAccountQueue.getName()}"}
	)
	public void deleteOpenBizLogByDeletedAppQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_account_biz_log_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[delete_account_biz_log_deleted_account] ===> 已删除的账号： AccountId: {}, EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Criteria criteria = Criteria.where(BizLogAccountMongodb.FIELD.ACCOUNT_ID).is(deletedAccountMessage.getAccountId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogAccountMongodb.class, MongodbConstants.Collection.BIZ_LOG_ACCOUNT);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogAccountMongodb> deletedBizLogAccountMongodbList = mongoTemplate.findAllAndRemove(query, BizLogAccountMongodb.class, MongodbConstants.Collection.BIZ_LOG_ACCOUNT);
					if (!deletedBizLogAccountMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedBizLogAccountMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_ACCOUNT);
					}
					log.debug("账号级业务日志删除成功: AccountId: {}  DeletedCount: {}", deletedAccountMessage.getAccountId(), deletedBizLogAccountMongodbList.size());
				} catch (Exception e) {
					log.warn("delete account biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_account_biz_log_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_account_biz_log_deleted_account] message handler error", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}

	}
}
