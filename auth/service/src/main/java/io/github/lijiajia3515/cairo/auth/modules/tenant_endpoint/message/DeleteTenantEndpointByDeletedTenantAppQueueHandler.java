package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.DeletedTenantEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
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
 * 删除企业终端（根据已删除的企业应用）处理器
 */
@Slf4j
@Component
public class DeleteTenantEndpointByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteTenantEndpointByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
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
		queues = {"#{deleteTenantEndpointByDeletedTenantAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_endpoint_by_deleted_tenant_app] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[delete_tenant_endpoint_by_deleted_tenant_app] ===> 已删除企业应用: TenantId: {} AppId: {} EventAccountId: {} EventTime: {} ",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId(),
				deletedTenantAppMessage.getEventAccountId(),
				deletedTenantAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantEndpointMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query query = Query.query(criteria);


			List<TenantEndpointMongodb> deleteTenantEndpointMongodbList = mongoTemplate.find(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
			deleteTenantEndpointMongodbList.forEach(deleteTenantEndpointMongodb -> {
				// 删除企业终端
				transactionTemplate.executeWithoutResult(transactionStatus -> {
					try {
						Criteria deleteCriteria = Criteria
							.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(deleteTenantEndpointMongodb.getTenantId())
							.and(TenantEndpointMongodb.FIELD.APP_ID).is(deleteTenantEndpointMongodb.getAppId())
							.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(deleteTenantEndpointMongodb.getEndpointId());
						Query deleteQuery = Query.query(deleteCriteria);
						Update deleteUpdate = new Update();
						deleteUpdate.set(TenantEndpointMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, deletedTenantAppMessage.getEventAccountId());
						deleteUpdate.currentDate(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME);

						UpdateResult updateResult = mongoTemplate.updateFirst(deleteQuery, deleteUpdate, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
						log.debug("updateResult: {}", updateResult);
						TenantEndpointMongodb deletedTenantEndpointMongodb = mongoTemplate.findAndRemove(deleteQuery, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
						if (deletedTenantEndpointMongodb != null) {
							mongoTemplate.insert(deletedTenantEndpointMongodb, MongodbConstants.DeletedCollection.TENANT_ENDPOINT);
						}
					} catch (Exception e) {
						transactionStatus.setRollbackOnly();
						log.warn("delete tenant app endpoint mongodb warn", e);
					}
				});

				// 发送已删除企业终端消息
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_ENDPOINT, deleteTenantEndpointMongodb.getAppId(), deletedTenantAppMessage.getAppId()),
						objectMapper.writeValueAsString(DeletedTenantEndpointMessage.builder()
							.tenantId(deleteTenantEndpointMongodb.getTenantId())
							.appId(deleteTenantEndpointMongodb.getAppId())
							.endpointId(deleteTenantEndpointMongodb.getEndpointId())
							.eventAccountId(deletedTenantAppMessage.getEventAccountId())
							.eventTime(LocalDateTime.now())
							.build()),
						new CorrelationData(CoreConstants.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("send delete tenant app endpoint mq warn", e);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_tenant_endpoint_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_endpoint_by_deleted_tenant_app] message handler error", e);
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
