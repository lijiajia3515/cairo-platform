package io.github.lijiajia3515.cairo.auth.modules.login_log.account_login_log.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.core.AccountAuthType;
import io.github.lijiajia3515.cairo.auth.domain.message.client.DeletedClientMessage;
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
 * 删除账号登录日志根据已删除的客户端 队列处理器
 */
@Slf4j
@Component
public class DeleteAccountLoginLogByDeletedClientQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteAccountLoginLogByDeletedClientQueueHandler(ObjectMapper objectMapper,
															MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAccountLoginLogByDeletedClientQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_account_login_log_by_deleted_client] message handler start");
			DeletedClientMessage deletedClientMessage = objectMapper.readValue(payload, DeletedClientMessage.class);
			log.info("[delete_account_login_log_by_deleted_client] ===> 已删除客户端： AppId: {} EndpointId: {} ClientId: {}, EventCairoUserId: {} EventTime: {} ",
				deletedClientMessage.getAppId(),
				deletedClientMessage.getEndpointId(),
				deletedClientMessage.getClientId(),
				deletedClientMessage.getEventCairoUserId(),
				deletedClientMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AccountLoginLogMongodb.FIELD.AUTH_TYPE).is(AccountAuthType.OAUTH2.getValue())
				.and(AccountLoginLogMongodb.FIELD.APP_ID).is(deletedClientMessage.getAppId())
				.and(AccountLoginLogMongodb.FIELD.CLIENT_ID).is(deletedClientMessage.getClientId());
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
					log.debug("账号登录日志数据删除成功: AppId: {} ClientId: {} AuthType: OAuth2 DeletedCount: {}",
						deletedClientMessage.getAppId(),
						deletedClientMessage.getClientId(),
						deletedAccountLoginLogList.size()
					);
				} catch (Exception e) {
					log.warn("delete account_login_log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_account_login_log_by_deleted_client] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_account_login_log_by_deleted_client] message handler error", e);
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
