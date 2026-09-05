package io.github.lijiajia3515.cairo.auth.modules.biz_log.tenant_app_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.client.DeletedClientMessage;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantAppMongodb;
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
public class DeleteTenantAppBizLogByDeletedClientQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteTenantAppBizLogByDeletedClientQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppBizLogByDeletedClientQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleted_tenant_app_biz_log_deleted_client] message handler start");
			DeletedClientMessage deletedClientMessage = objectMapper.readValue(payload, DeletedClientMessage.class);
			log.info("[deleted_tenant_app_biz_log_deleted_client] ===> 已删除的客户端： AppId: {} ClientId: {} EventCairoUserId: {} EventTime: {} ",
				deletedClientMessage.getAppId(),
				deletedClientMessage.getClientId(),
				deletedClientMessage.getEventCairoUserId(),
				deletedClientMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(BizLogTenantAppMongodb.FIELD.APP_ID).is(deletedClientMessage.getAppId())
				.and(BizLogTenantAppMongodb.FIELD.CLIENT_ID).is(deletedClientMessage.getClientId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogTenantAppMongodb> deletedTenantAppBizLogMongodbList = mongoTemplate.findAllAndRemove(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
					if (!deletedTenantAppBizLogMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedTenantAppBizLogMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_TENANT_APP);
					}
					log.debug("用户级业务日志删除成功: AppId: {} ClientId: {} DeletedCount: {}", deletedClientMessage.getAppId(), deletedClientMessage.getClientId(), deletedTenantAppBizLogMongodbList.size());
				} catch (Exception e) {
					log.warn("delete tenant app endpoint biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleted_tenant_app_biz_log_deleted_client] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleted_tenant_app_biz_log_deleted_client] message handler error", e);
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
