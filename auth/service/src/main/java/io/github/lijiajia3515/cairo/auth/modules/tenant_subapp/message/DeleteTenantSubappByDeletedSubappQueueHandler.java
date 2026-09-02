package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.DeletedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.DeletedSubappMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 删除企业子应用根据已删除的子应用 队列处理器
 */
@Slf4j
@Component
public class DeleteTenantSubappByDeletedSubappQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteTenantSubappByDeletedSubappQueueHandler(ObjectMapper objectMapper,
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
		queues = {"#{deleteTenantSubappByDeletedSubappQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_subapp_by_deleted_subapp] message handler start");
			DeletedSubappMessage deletedSubappMessage = objectMapper.readValue(payload, DeletedSubappMessage.class);
			log.info("[delete_tenant_subapp_by_deleted_subapp] ===> 已删除子应用： AppId: {} EndpointId: {} SubappId: {} EventCairoUserId: {} EventTime: {} ",
				deletedSubappMessage.getAppId(),
				deletedSubappMessage.getEndpointId(),
				deletedSubappMessage.getSubappId(),
				deletedSubappMessage.getEventCairoUserId(),
				deletedSubappMessage.getEventTime()
			);
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria criteria = Criteria
						.where(TenantSubappMongodb.FIELD.SUBAPP_ID).is(deletedSubappMessage.getSubappId());
					Query query = Query.query(criteria);
					List<TenantSubappMongodb> tenantSubappMongodbs = mongoTemplate.findAllAndRemove(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
					if (!tenantSubappMongodbs.isEmpty()) {
						mongoTemplate.insert(tenantSubappMongodbs, MongodbConstants.DeletedCollection.TENANT_SUBAPP);
						tenantSubappMongodbs.forEach(tenantSubappMongodb -> {
								log.debug("企业子应用删除成功:TenantId: {} AppId: {} EndpointId: {} SubappId: {} ",
									tenantSubappMongodb.getTenantId(),
									tenantSubappMongodb.getAppId(),
									tenantSubappMongodb.getEndpointId(),
									tenantSubappMongodb.getSubappId());
								//发送删除企业子应用完成消息
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
										new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
									);
								} catch (JsonProcessingException e) {
									log.warn("send delete_tenant_subapp mq warn", e);
								}
							}
						);
					}
				} catch (BusinessException e) {
					status.setRollbackOnly();
				} catch (Exception e) {
					log.debug("delete_tenant_subapp_by_deleted_subapp", e);
					status.setRollbackOnly();
				}

			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_subapp_by_deleted_subapp] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_subapp_by_deleted_subapp] message handler error", e);
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
