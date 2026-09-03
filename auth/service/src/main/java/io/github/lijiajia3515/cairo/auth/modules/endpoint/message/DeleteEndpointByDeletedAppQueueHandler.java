package io.github.lijiajia3515.cairo.auth.modules.endpoint.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
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
import java.util.stream.Collectors;


/**
 * 创建用户日志 队列 处理器
 */
@Slf4j
@Component
public class DeleteEndpointByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final FileCommonService fileCommonService;

	public DeleteEndpointByDeletedAppQueueHandler(ObjectMapper objectMapper,
													 MongoTemplate mongoTemplate,
													 TransactionTemplate transactionTemplate,
													 RabbitTemplate rabbitTemplate,
													 CairoRabbitmqTool cairoRabbitmqTool,
													 FileCommonService fileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.fileCommonService = fileCommonService;
	}

	@RabbitListener(
		queues = {"#{deleteEndpointByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_endpoint_by_deleted_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_endpoint_by_deleted_app] ===> 已删除应用： AppId: {} EventCairoUserId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventCairoUserId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria.where(EndpointMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());
			Query query = Query.query(criteria);
			List<EndpointMongodb> deleteEndpointList = mongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

			deleteEndpointList.forEach(deleteEndpoint -> {
				try {
					EndpointMongodb deletedEndpoint = transactionTemplate.execute(status -> {
						try {
							Criteria oneCriteria = Criteria
								.where(EndpointMongodb.FIELD.APP_ID).is(deleteEndpoint.getAppId())
								.and(EndpointMongodb.FIELD.ENDPOINT_ID).is(deleteEndpoint.getEndpointId());
							Query oneQuery = Query.query(oneCriteria);
							Update update = new Update();
							update.currentDate(EndpointMongodb.FIELD.METADATA.UPDATE_TIME);
							update.set(EndpointMongodb.FIELD.METADATA.UPDATE_USER_ID, deletedAppMessage.getEventCairoUserId());

							UpdateResult updateResult = mongoTemplate.updateFirst(oneQuery, update, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
							EndpointMongodb deleteEndpointMongodb = mongoTemplate.findAndRemove(oneQuery, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
							if (deleteEndpointMongodb != null) {
								mongoTemplate.insert(deleteEndpointMongodb, MongodbConstants.DeletedCollection.ENDPOINT);
							}
							return deleteEndpointMongodb;
						} catch (Exception e) {
							log.warn("删除终端失败", e);
							return null;
						}
					});
					if (deletedEndpoint != null) {
						DeletedEndpointMessage deletedEndpointMessage = DeletedEndpointMessage.builder()
							.appId(deletedEndpoint.getAppId())
							.endpointId(deletedEndpoint.getEndpointId())
							.eventCairoUserId(deletedAppMessage.getEventCairoUserId())
							.eventTime(LocalDateTime.now())
							.build();
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_ENDPOINT, deletedEndpointMessage.getAppId()),
							objectMapper.writeValueAsString(deletedEndpointMessage),
							new CorrelationData(CoreConstants.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.warn("e", e);
				}
			});
			//删除图标
			fileCommonService.deletePublicFile(deletedAppMessage.getAppId().concat("/").concat(FileKeyPrefixConstants.ENDPOINT_ICON_PREFIX), deleteEndpointList.stream().map(EndpointMongodb::getIcon).collect(Collectors.toList()));

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_endpoint_by_deleted_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_endpoint_by_deleted_app] message handler error", e);
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
