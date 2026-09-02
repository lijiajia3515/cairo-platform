package io.github.lijiajia3515.cairo.auth.modules.subapp.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.ModifiedEndpointInfoMessage;
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
 * 更新子应用的的Endpoint信息
 */
@Slf4j
@Component
public class ModifySubappByModifiedEndpointInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public ModifySubappByModifiedEndpointInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{modifySubappByModifiedEndpointInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_subapp_by_modified_endpoint_info] message handler start");
			ModifiedEndpointInfoMessage modifiedEndpointInfoMessage = objectMapper.readValue(payload, ModifiedEndpointInfoMessage.class);
			log.info("""
				[modify_subapp_by_modified_endpoint_info][已修改的终端] ===> ：
					Id: {} AppId: {}
					OldEndpointId: {} OldEndpointName: {}
					NewEndpointId: {} NewEndpointName: {}
					EventCairoUserId: {} EventTime: {}
				""",
				modifiedEndpointInfoMessage.getId(),
				modifiedEndpointInfoMessage.getAppId(),
				modifiedEndpointInfoMessage.getOldEndpointId(),
				modifiedEndpointInfoMessage.getOldEndpointName(),
				modifiedEndpointInfoMessage.getNewEndpointId(),
				modifiedEndpointInfoMessage.getNewEndpointName(),
				modifiedEndpointInfoMessage.getEventCairoUserId(),
				modifiedEndpointInfoMessage.getEventTime()
			);

			if (modifiedEndpointInfoMessage.getOldEndpointId().equals(modifiedEndpointInfoMessage.getNewEndpointId())) {
				log.debug("未更新EndpointId, 无需更新Menu");
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[modify_subapp_by_modified_endpoint_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(SubappMongodb.FIELD.APP_ID).is(modifiedEndpointInfoMessage.getAppId())
				.and(SubappMongodb.FIELD.ENDPOINT_ID).is(modifiedEndpointInfoMessage.getOldEndpointId());
			Query query = Query.query(criteria);

			Update update = new Update();
			update.set(SubappMongodb.FIELD.ENDPOINT_ID, modifiedEndpointInfoMessage.getNewEndpointId());
			update.set(SubappMongodb.FIELD.METADATA.UPDATE_USER_ID, modifiedEndpointInfoMessage.getEventCairoUserId());
			update.currentDate(SubappMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateMulti(query, update, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
			log.debug("subapp updateResult: {}", updateResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_subapp_by_modified_endpoint_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_subapp_by_modified_endpoint_info] message handler error", e);
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
