package io.github.lijiajia3515.cairo.auth.modules.biz_log.tenant_app_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
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
 * 删除企业终端用户业务日志 根据 已删除客户端 队列实现
 */
@Slf4j
@Component
public class DeleteTenantAppBizLogByDeletedEndpointQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteTenantAppBizLogByDeletedEndpointQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppBizLogByDeletedEndpointQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_app_biz_log_deleted_endpoint] message handler start");
			DeletedEndpointMessage deletedEndpointMessage = objectMapper.readValue(payload, DeletedEndpointMessage.class);
			log.info("[delete_tenant_app_biz_log_deleted_endpoint] ===> 已删除终端： AppId: {} EndpointId: {} EventCairoUserId: {} EventTime: {} ",
				deletedEndpointMessage.getAppId(),
				deletedEndpointMessage.getEndpointId(),
				deletedEndpointMessage.getEventCairoUserId(),
				deletedEndpointMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(BizLogTenantAppMongodb.FIELD.APP_ID).is(deletedEndpointMessage.getAppId())
				.and(BizLogTenantAppMongodb.FIELD.ENDPOINT_ID).is(deletedEndpointMessage.getEndpointId())
				;
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogTenantAppMongodb> deletedEndpointUserBizLogMongodbList = mongoTemplate.findAllAndRemove(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
					if (!deletedEndpointUserBizLogMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedEndpointUserBizLogMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_TENANT_APP);
					}
					log.debug("终端用户级业务日志删除成功: AppId: {} DeletedCount: {}", deletedEndpointMessage.getAppId(), deletedEndpointUserBizLogMongodbList.size());
				} catch (Exception e) {
					log.warn("delete tenant app endpoint biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_tenant_app_biz_log_deleted_endpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_app_biz_log_deleted_endpoint] message handler error", e);
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
