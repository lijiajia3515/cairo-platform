package io.github.lijiajia3515.cairo.auth.modules.login_log.tenant_app_user_login_log.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.TenantAppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.DeletedTenantAppUserMessage;
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
 * 删除终端用户登录日志根据已删除的用户 队列处理器
 */
@Slf4j
@Component
public class DeleteTenantAppUserLoginLogByDeletedTenantAppUserQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteTenantAppUserLoginLogByDeletedTenantAppUserQueueHandler(ObjectMapper objectMapper,
																			  MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppUserLoginLogByDeletedTenantAppUserQueue.getName()}"}
	)
	public void deleteTenantAppUserLoginLogByDeletedTenantAppUserQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_endpoint_user_login_log_by_deleted_user] message handler start");
			DeletedTenantAppUserMessage deletedTenantAppUserMessage = objectMapper.readValue(payload, DeletedTenantAppUserMessage.class);
			log.info("[delete_endpoint_user_login_log_by_deleted_user] ===> 已删除的用户： TenantId: {} AppId: {} UserId: {} Nickname: {}, EventUserId: {} EventTime: {} ",
				deletedTenantAppUserMessage.getTenantId(),
				deletedTenantAppUserMessage.getAppId(),
				deletedTenantAppUserMessage.getUserId(),
				deletedTenantAppUserMessage.getNickname(),
				deletedTenantAppUserMessage.getEventUserId(),
				deletedTenantAppUserMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppUserLoginLogMongodb.FIELD.TENANT_ID).is(deletedTenantAppUserMessage.getTenantId())
				.and(TenantAppUserLoginLogMongodb.FIELD.APP_ID).is(deletedTenantAppUserMessage.getAppId())
				.and(TenantAppUserLoginLogMongodb.FIELD.USER_ID).is(deletedTenantAppUserMessage.getUserId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<TenantAppUserLoginLogMongodb> deletedEndpointUserLoginLogList = mongoTemplate.findAllAndRemove(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
					if (!deletedEndpointUserLoginLogList.isEmpty()) {
						mongoTemplate.insert(deletedEndpointUserLoginLogList, MongodbConstants.DeletedCollection.TENANT_APP_USER_LOGIN_LOG);
					}
					log.debug("终端登录数据数据删除成功: TenantId: {} AppId: {} UserId: {}  DeletedCount: {}",
						deletedTenantAppUserMessage.getTenantId(),
						deletedTenantAppUserMessage.getAppId(),
						deletedTenantAppUserMessage.getUserId(),
						deletedEndpointUserLoginLogList.size()
					);
				} catch (Exception e) {
					log.warn("delete endpoint_user_login_log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_endpoint_user_login_log_by_deleted_user] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_endpoint_user_login_log_by_deleted_user] message handler error", e);
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
