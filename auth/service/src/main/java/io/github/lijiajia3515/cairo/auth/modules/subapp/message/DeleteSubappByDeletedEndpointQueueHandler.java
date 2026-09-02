package io.github.lijiajia3515.cairo.auth.modules.subapp.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.DeletedSubappMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 删除子应用根据删除终端
 */
@Slf4j
@Component
public class DeleteSubappByDeletedEndpointQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteSubappByDeletedEndpointQueueHandler(ObjectMapper objectMapper,
														 MongoTemplate mongoTemplate,
														 TransactionTemplate transactionTemplate,
														 RabbitTemplate rabbitTemplate,
														 CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@RabbitListener(
		queues = {"#{deleteSubappByDeletedEndpointQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_subapp_by_deleted_endpoint] message handler start");
			DeletedEndpointMessage deletedEndpointMessage = objectMapper.readValue(payload, DeletedEndpointMessage.class);
			log.info("[delete_subapp_by_deleted_endpoint] ===> 已删除终端： AppId: {} EndpointId: {}  EventCairoUserId: {} EventTime: {} ",
				deletedEndpointMessage.getAppId(),
				deletedEndpointMessage.getEndpointId(),
				deletedEndpointMessage.getEventCairoUserId(),
				deletedEndpointMessage.getEventTime()
			);
			Criteria criteria = Criteria
				.where(SubappMongodb.FIELD.APP_ID).is(deletedEndpointMessage.getAppId())
				.and(SubappMongodb.FIELD.ENDPOINT_ID).is(deletedEndpointMessage.getEndpointId());
			Query query = Query.query(criteria);
			List<SubappMongodb> deletedSubappMongodbList = mongoTemplate.find(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
			deletedSubappMongodbList.forEach(deleteSubapp -> {
				try {
					SubappMongodb deletedSubapp = transactionTemplate.execute(status -> {
						try {
							Criteria oneCriteria = Criteria
								.where(SubappMongodb.FIELD.APP_ID).is(deleteSubapp.getAppId())
								.and(SubappMongodb.FIELD.ENDPOINT_ID).is(deleteSubapp.getEndpointId())
								.and(SubappMongodb.FIELD.SUBAPP_ID).is(deleteSubapp.getSubappId());
							Query oneQuery = Query.query(oneCriteria);
							Update update = new Update();
							update.currentDate(SubappMongodb.FIELD.METADATA.UPDATE_TIME);
							update.set(SubappMongodb.FIELD.METADATA.UPDATE_USER_ID, deletedEndpointMessage.getEventCairoUserId());

							UpdateResult updateResult = mongoTemplate.updateFirst(oneQuery, update, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
							SubappMongodb deleteSubappMongodb = mongoTemplate.findAndRemove(oneQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
							if (deleteSubappMongodb != null) {
								mongoTemplate.insert(deleteSubappMongodb, MongodbConstants.DeletedCollection.SUBAPP);
							}
							return deleteSubappMongodb;
						} catch (Exception e) {
							log.warn("删除子应用失败", e);
							return null;
						}
					});
					if (deletedSubapp != null) {
						DeletedSubappMessage deletedSubappMessage = DeletedSubappMessage.builder()
							.appId(deletedSubapp.getAppId())
							.endpointId(deletedSubapp.getEndpointId())
							.subappId(deletedSubapp.getSubappId())
							.eventCairoUserId(deletedEndpointMessage.getEventCairoUserId())
							.eventTime(LocalDateTime.now())
							.build();
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SUBAPP, deletedSubappMessage.getAppId()),
							objectMapper.writeValueAsString(deletedSubappMessage),
							new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.warn("e", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_subapp_by_deleted_endpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_subapp_by_deleted_endpoint] message handler error", e);
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
