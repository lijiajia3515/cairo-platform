package io.github.lijiajia3515.cairo.auth.modules.biz_log.subapp_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.DeletedSubappMessage;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogSubappMongodb;
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
 * 删除子应用业务日志 根据 已删除子应用 队列实现
 */
@Slf4j
@Component
public class DeleteSubappBizLogByDeletedSubappQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteSubappBizLogByDeletedSubappQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteSubappBizLogByDeletedSubappQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleted_subapp_biz_log_deleted_subapp] message handler start");
			DeletedSubappMessage deletedSubappMessage = objectMapper.readValue(payload, DeletedSubappMessage.class);
			log.info("[deleted_subapp_biz_log_deleted_subapp] ===> 已删除的子应用： AppId: {} EndpointId: {} SubappId: {} EventCairoUserId: {} EventTime: {} ",
				deletedSubappMessage.getAppId(),
				deletedSubappMessage.getEndpointId(),
				deletedSubappMessage.getSubappId(),
				deletedSubappMessage.getEventCairoUserId(),
				deletedSubappMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(BizLogSubappMongodb.FIELD.APP_ID).is(deletedSubappMessage.getAppId())
				.and(BizLogSubappMongodb.FIELD.ENDPOINT_ID).is(deletedSubappMessage.getEndpointId())
				.and(BizLogSubappMongodb.FIELD.SUBAPP_ID).is(deletedSubappMessage.getSubappId());
			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<BizLogSubappMongodb> deletedSubappBizLogMongodbList = mongoTemplate.findAllAndRemove(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
					if (!deletedSubappBizLogMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedSubappBizLogMongodbList, MongodbConstants.DeletedCollection.BIZ_LOG_SUBAPP);
					}
					log.debug("子应用级业务日志删除成功: AppId: {} EndpointId: {} SubappId: {} DeletedCount: {}", deletedSubappMessage.getAppId(), deletedSubappMessage.getEndpointId(), deletedSubappMessage.getSubappId(), deletedSubappBizLogMongodbList.size());
				} catch (Exception e) {
					log.warn("delete app subapp biz log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleted_subapp_biz_log_deleted_subapp] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleted_subapp_biz_log_deleted_subapp] message handler error", e);
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
