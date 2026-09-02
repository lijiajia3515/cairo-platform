package io.github.lijiajia3515.cairo.auth.modules.tenant_app.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
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
 * 创建用户日志 队列 处理器
 */
@Slf4j
@Component
public class DeleteTenantAppByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final EndpointCommonService endpointCommonService;

	public DeleteTenantAppByDeletedAppQueueHandler(ObjectMapper objectMapper,
												   MongoTemplate mongoTemplate,
												   TransactionTemplate transactionTemplate,
												   MongoTemplate readMongoTemplate, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool, EndpointCommonService endpointCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.endpointCommonService = endpointCommonService;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_app_by_deleted_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_tenant_app_by_deleted_app] ===> 已删除应用： AppId: {} EventCairoUserId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventCairoUserId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where("app_id").is(deletedAppMessage.getAppId());

			Query query = Query.query(criteria);

			List<TenantAppMongodb> tenantAppMongodbList = mongoTemplate.find(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
			tenantAppMongodbList.forEach(deleteTenantApp -> {
				try {
					TenantAppMongodb deletedTenantApp = transactionTemplate.execute(status -> {
						try {
							Criteria oneCriteria = Criteria
								.where(TenantAppMongodb.FIELD.TENANT_ID).is(deleteTenantApp.getTenantId())
								.and(TenantAppMongodb.FIELD.APP_ID).is(deleteTenantApp.getAppId());
							Query oneQuery = Query.query(oneCriteria);
							Update update = new Update();
							update.currentDate(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME);
							update.set(TenantAppMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, null);

							UpdateResult updateResult = mongoTemplate.updateFirst(oneQuery, update, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
							TenantAppMongodb deletedTenantMongodb = mongoTemplate.findAndRemove(oneQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
							if (deletedTenantMongodb != null) {
								mongoTemplate.insert(deletedTenantMongodb, MongodbConstants.DeletedCollection.TENANT_APP);
								log.debug("企业应用删除成功: TenantId: {} AppId: {} UpdateTime: {}",
									deleteTenantApp.getTenantId(),
									deleteTenantApp.getAppId(),
									deleteTenantApp.getMetadata().getUpdateTime()
								);
							}
							return deletedTenantMongodb;
						} catch (Exception e) {
							log.warn("删除企业应用失败", e);
							status.setRollbackOnly();
							return null;
						}
					});
					if (deletedTenantApp != null) {
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_APP, deletedTenantApp.getTenantId(), deletedTenantApp.getAppId()),
							objectMapper.writeValueAsString(
								DeletedTenantAppMessage.builder()
									.tenantId(deletedTenantApp.getTenantId())
									.appId(deletedTenantApp.getAppId())
									.eventAccountId(null)
									.eventTime(LocalDateTime.now())
									.build()
							),
							new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
						);
					}
				} catch (Exception e) {
					log.warn("e", e);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_app_by_deleted_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_app_by_deleted_app] message handler error", e);
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
