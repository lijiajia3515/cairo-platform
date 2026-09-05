package io.github.lijiajia3515.cairo.auth.modules.biz_log.app_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogAppMongodb;
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
 * 删除应用级用户业务日志 根据 已删除客户端 队列实现
 */
@Slf4j
@Component
public class DeleteAppBizLogByDeletedEndpointQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteAppBizLogByDeletedEndpointQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteAppBizLogByDeletedEndpointQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_biz_log_deleted_endpoint] message handler start");
			DeletedEndpointMessage deletedEndpointMessage = objectMapper.readValue(payload, DeletedEndpointMessage.class);
			log.info("[delete_app_biz_log_deleted_endpoint] ===> 已删除终端： AppId: {} EndpointId: {} EventCairoUserId: {} EventTime: {} ",
				deletedEndpointMessage.getAppId(),
				deletedEndpointMessage.getEndpointId(),
				deletedEndpointMessage.getEventCairoUserId(),
				deletedEndpointMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(BizLogAppMongodb.FIELD.APP_ID).is(deletedEndpointMessage.getAppId())
				.and(BizLogAppMongodb.FIELD.ENDPOINT_ID).is(deletedEndpointMessage.getEndpointId())
				;
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogAppMongodb> deletedAppBizLogMongodbList = mongoTemplate.findAllAndRemove(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
					if (!deletedAppBizLogMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedAppBizLogMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_APP);
					}
					log.debug("应用级用户级业务日志删除成功: AppId: {} DeletedCount: {}", deletedEndpointMessage.getAppId(), deletedAppBizLogMongodbList.size());
				} catch (Exception e) {
					log.warn("delete app endpoint biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_app_biz_log_deleted_endpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_biz_log_deleted_endpoint] message handler error", e);
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
