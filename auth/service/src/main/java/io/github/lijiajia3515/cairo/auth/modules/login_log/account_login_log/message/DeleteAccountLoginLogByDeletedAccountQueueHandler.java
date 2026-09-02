package io.github.lijiajia3515.cairo.auth.modules.login_log.account_login_log.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
 * 删除账号登录日志根据已删除的账号 队列处理器
 */
@Slf4j
@Component
public class DeleteAccountLoginLogByDeletedAccountQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteAccountLoginLogByDeletedAccountQueueHandler(ObjectMapper objectMapper,
                                                             MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAccountLoginLogByDeletedAccountQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_account_login_log_by_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[delete_account_login_log_by_deleted_account] ===> 已删除的账号： AccountId: {} Nickname: {} JoinTime: {} EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getNickname(),
				deletedAccountMessage.getJoinTime(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AccountLoginLogMongodb.FIELD.ACCOUNT_ID).is(deletedAccountMessage.getAccountId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<AccountLoginLogMongodb> deletedAccountLoginLogList = mongoTemplate.findAllAndRemove(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
					if (!deletedAccountLoginLogList.isEmpty()) {
						mongoTemplate.insert(deletedAccountLoginLogList, MongodbConstants.DeletedCollection.ACCOUNT_LOGIN_LOG);
					}
					log.debug("账号登录数据数据删除成功: AccountId: {} Nickname: {} DeletedCount: {}",
						deletedAccountMessage.getAccountId(),
						deletedAccountMessage.getNickname(),
						deletedAccountLoginLogList.size()
					);
				} catch (Exception e) {
					log.warn("delete account_login_log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_account_login_log_by_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_account_login_log_by_deleted_account] message handler error", e);
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
