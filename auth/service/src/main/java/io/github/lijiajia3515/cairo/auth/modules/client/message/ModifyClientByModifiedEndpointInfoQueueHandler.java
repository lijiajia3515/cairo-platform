package io.github.lijiajia3515.cairo.auth.modules.client.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.ModifiedEndpointInfoMessage;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCacheTool;
import io.github.lijiajia3515.cairo.auth.domain.message.client.ModifiedClientInfoMessage;
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
 * 更新客户端根据已修改的终端信息 队列处理器
 */
@Slf4j
@Component
public class ModifyClientByModifiedEndpointInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final ClientCacheTool clientCacheTool;

	public ModifyClientByModifiedEndpointInfoQueueHandler(ObjectMapper objectMapper,
															 MongoTemplate mongoTemplate,
															 TransactionTemplate transactionTemplate,
															 RabbitTemplate rabbitTemplate,
															 CairoRabbitmqTool cairoRabbitmqTool,
															 ClientCacheTool clientCacheTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.clientCacheTool = clientCacheTool;
	}

	@RabbitListener(
		queues = {"#{modifyClientByModifiedEndpointInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_client_by_modified_endpoint_info] message handler start");
			ModifiedEndpointInfoMessage modifiedEndpointInfoMessage = objectMapper.readValue(payload, ModifiedEndpointInfoMessage.class);
			log.info("[modify_client_by_modified_endpoint_info] ===> 已修改的终端：Id: {} AppId: {} OldEndpointId: {} OldEndpointName: {} NewEndpointId: {} NewEndpointName: {} EventCairoUserId: {} EventTime: {} ",
				modifiedEndpointInfoMessage.getAppId(),
				modifiedEndpointInfoMessage.getOldEndpointId(),
				modifiedEndpointInfoMessage.getOldEndpointName(),
				modifiedEndpointInfoMessage.getNewEndpointId(),
				modifiedEndpointInfoMessage.getNewEndpointName(),
				modifiedEndpointInfoMessage.getEventCairoUserId(),
				modifiedEndpointInfoMessage.getEventCairoUserId(),
				modifiedEndpointInfoMessage.getEventTime()
			);

			if (modifiedEndpointInfoMessage.getOldEndpointId().equals(modifiedEndpointInfoMessage.getNewEndpointId())) {
				log.debug("未更新EndpointId, 无需更新Client");
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[modify_client_by_modified_endpoint_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(ClientMongodb.FIELD.APP_ID).is(modifiedEndpointInfoMessage.getAppId())
				.and(ClientMongodb.FIELD.ENDPOINT_ID).is(modifiedEndpointInfoMessage.getOldEndpointId());

			Query query = Query.query(criteria);
			List<ClientMongodb> modifyClientMongodbList = mongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

			modifyClientMongodbList.forEach(modifyClient -> {
				try {
					ClientMongodb modifiedClient = transactionTemplate.execute(status -> {
						try {
							Criteria updateCriteria = Criteria
								.where(ClientMongodb.FIELD.ID).is(modifyClient.getId());
							Query updateQuery = Query.query(updateCriteria);

							Update update = new Update();
							update.set(ClientMongodb.FIELD.ENDPOINT_ID, modifiedEndpointInfoMessage.getNewEndpointId());
							update.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, modifiedEndpointInfoMessage.getEventCairoUserId());
							update.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);

							ClientMongodb modifyiedClientMongodb = mongoTemplate.findAndModify(updateQuery, update, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
							return modifyiedClientMongodb;
						} catch (Exception e) {
							log.warn("删除客户端失败", e);
							status.setRollbackOnly();
							return null;
						}
					});
					if (modifiedClient != null) {
						clientCacheTool.removeCache(modifiedClient.getId(), modifyClient.getClientId());
						ModifiedClientInfoMessage modifiedClientInfoMessage = ModifiedClientInfoMessage.builder()
							.id(modifiedClient.getId())
							.appId(modifiedClient.getAppId())
							.endpointId(modifiedClient.getEndpointId())
							.clientId(modifiedClient.getClientId())
							.eventCairoUserId(modifiedEndpointInfoMessage.getEventCairoUserId())
							.eventTime(LocalDateTime.now())
							.build();

						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_CLIENT_INFO, modifiedClientInfoMessage.getAppId()),
							objectMapper.writeValueAsString(modifiedClientInfoMessage),
							new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.warn("修改客户端失败: Id: {} AppId: {} EndpointId: {} ClientId: {}  ClientName： {} 异常: {}",
						modifyClient.getId(),
						modifyClient.getAppId(),
						modifyClient.getEndpointId(),
						modifyClient.getClientId(),
						modifyClient.getClientName(),
						e.getMessage()
					);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_client_by_modified_endpoint_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_client_by_modified_endpoint_info] message handler error", e);
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
