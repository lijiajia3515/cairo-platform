package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.DeletedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_endpoint.DeletedTenantEndpointMessage;
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
 * 删除企业子应用（根据已删除的企业终端）处理器
 */
@Slf4j
@Component
public class DeleteTenantSubappByDeletedTenantEndpointQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	public DeleteTenantSubappByDeletedTenantEndpointQueueHandler(ObjectMapper objectMapper,
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
		queues = {"#{deleteTenantSubappByDeletedTenantEndpointQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_subapp_by_deleted_tenant_endpoint] message handler start");
			DeletedTenantEndpointMessage deletedTenantEndpointMessage = objectMapper.readValue(payload, DeletedTenantEndpointMessage.class);
			log.info("[delete_tenant_subapp_by_deleted_tenant_endpoint] ===> 已删除的企业终端: TenantId: {} AppId: {} EndpointId: {} EventCairoAccountId: {} EventTime: {} ",
				deletedTenantEndpointMessage.getTenantId(),
				deletedTenantEndpointMessage.getAppId(),
				deletedTenantEndpointMessage.getEndpointId(),
				deletedTenantEndpointMessage.getEventAccountId(),
				deletedTenantEndpointMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantSubappMongodb.FIELD.TENANT_ID).is(deletedTenantEndpointMessage.getTenantId())
				.and(TenantSubappMongodb.FIELD.APP_ID).is(deletedTenantEndpointMessage.getAppId())
				.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(deletedTenantEndpointMessage.getEndpointId());
			Query query = Query.query(criteria);
			// 查询企业子应用
			List<TenantSubappMongodb> tenantSubappMongodbs = mongoTemplate.find(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
			tenantSubappMongodbs.forEach(tenantSubappMongodb -> {
				// 删除企业子应用
				transactionTemplate.executeWithoutResult(transactionStatus -> {
					try {
						Criteria deleteCriteria = Criteria
							.where(TenantSubappMongodb.FIELD.TENANT_ID).is(tenantSubappMongodb.getTenantId())
							.and(TenantSubappMongodb.FIELD.APP_ID).is(tenantSubappMongodb.getAppId())
							.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(tenantSubappMongodb.getEndpointId())
							.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(tenantSubappMongodb.getSubappId());
						Query deleteQuery = Query.query(deleteCriteria);
						Update deleteUpdate = new Update();
						deleteUpdate.set(TenantSubappMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);
						deleteUpdate.currentDate(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME);

						UpdateResult updateResult = mongoTemplate.updateFirst(deleteQuery, deleteUpdate, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
						log.debug("updateResult: {}", updateResult);
						TenantSubappMongodb deletedTenantEndpointMongodb = mongoTemplate.findAndRemove(deleteQuery, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
						if (deletedTenantEndpointMongodb != null) {
							mongoTemplate.insert(deletedTenantEndpointMongodb, MongodbConstants.DeletedCollection.TENANT_SUBAPP);
						}
					} catch (Exception e) {
						transactionStatus.setRollbackOnly();
						log.warn("delete delete_tenant_subapp_by_deleted_tenant_endpoint  mongodb warn", e);
					}
				});
				//发送企业子应用删除完成消息
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_SUBAPP, tenantSubappMongodb.getTenantId(), tenantSubappMongodb.getAppId()),
						objectMapper.writeValueAsString(DeletedTenantSubappMessage
							.builder()
							.tenantId(tenantSubappMongodb.getTenantId())
							.appId(tenantSubappMongodb.getAppId())
							.endpointId(tenantSubappMongodb.getEndpointId())
							.subappId(tenantSubappMongodb.getSubappId())
							.eventTime(LocalDateTime.now())
							.build()
						),
						new CorrelationData(CoreConstants.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("send delete_tenant_subapp mq warn", e);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_tenant_subapp_by_deleted_tenant_endpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_subapp_by_deleted_tenant_endpoint] message handler error", e);
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
