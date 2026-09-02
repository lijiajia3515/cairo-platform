package io.github.lijiajia3515.cairo.auth.modules.biz_log.tenant_subapp_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.DeletedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantSubappMongodb;
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
 * 删除企业终端业务日志 根据 已删除客户端 队列实现
 */
@Slf4j
@Component
public class DeleteTenantSubappBizLogByDeletedTenantAppUserQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteTenantSubappBizLogByDeletedTenantAppUserQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteTenantSubappBizLogByDeletedTenantAppUserQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleted_tenant_subapp_biz_log_deleted_tenant_app_user] message handler start");
			DeletedTenantAppUserMessage deletedUserMessage = objectMapper.readValue(payload, DeletedTenantAppUserMessage.class);
			log.info("[deleted_tenant_subapp_biz_log_deleted_tenant_app_user] ===> 已删除的企业应用用户： TenantId: {} AppId: {} UserId: {} Nickname: {} EventUserId: {} EventTime: {} ",
				deletedUserMessage.getTenantId(),
				deletedUserMessage.getAppId(),
				deletedUserMessage.getUserId(),
				deletedUserMessage.getNickname(),
				deletedUserMessage.getEventUserId(),
				deletedUserMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(BizLogTenantSubappMongodb.FIELD.TENANT_ID).is(deletedUserMessage.getTenantId())
				.and(BizLogTenantSubappMongodb.FIELD.APP_ID).is(deletedUserMessage.getAppId())
				.and(BizLogTenantSubappMongodb.FIELD.USER_ID).is(deletedUserMessage.getUserId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogTenantSubappMongodb> deletedBizLogMongodbList = mongoTemplate.findAllAndRemove(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
					if (!deletedBizLogMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedBizLogMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_TENANT_SUBAPP);
					}
					log.debug("企业子应用业务日志删除成功:  TenantId: {} AppId: {} UserId: {} Nickname: {} DeleteCount {} ",
						deletedUserMessage.getTenantId(),
						deletedUserMessage.getAppId(),
						deletedUserMessage.getUserId(),
						deletedUserMessage.getNickname(),
						deletedBizLogMongodbList.size()
					);
				} catch (Exception e) {
					log.warn("delete tenant app endpoint biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleted_tenant_subapp_biz_log_deleted_tenant_app_user] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleted_tenant_subapp_biz_log_deleted_tenant_app_user] message handler error", e);
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
