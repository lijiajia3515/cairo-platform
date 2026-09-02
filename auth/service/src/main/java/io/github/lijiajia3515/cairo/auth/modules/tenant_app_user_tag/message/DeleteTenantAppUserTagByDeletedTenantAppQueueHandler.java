package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
import java.util.List;
import java.util.Map;


/**
 * 删除用户标签根据已删除的企业应用 队列 处理器
 */
@Slf4j
@Component
public class DeleteTenantAppUserTagByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final EndpointCommonService endpointCommonService;

	public DeleteTenantAppUserTagByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
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
		queues = {"#{deleteTenantAppUserTagByDeletedTenantAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_user_tag_by_deleted_tenant_app] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[delete_user_tag_by_deleted_tenant_app] ===> 已删除企业应用： TenantId: {} AppId: {} EventAccountId: {} EventTime: {} ",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId(),
				deletedTenantAppMessage.getEventAccountId(),
				deletedTenantAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<TenantAppUserTagMongodb> deletedTenantAppUserTagMongodbList = mongoTemplate.find(query, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
			deletedTenantAppUserTagMongodbList.forEach(tenantAppUserTagMongodb -> {
				try {
					TenantAppUserTagMongodb deletedUserTag = transactionTemplate.execute(status -> {
						try {
							Criteria updateCriteria = Criteria
								.where(TenantAppUserTagMongodb.FIELD.TENANT_ID).is(tenantAppUserTagMongodb.getTenantId())
								.and(TenantAppUserTagMongodb.FIELD.APP_ID).is(tenantAppUserTagMongodb.getAppId())
								.and(TenantAppUserTagMongodb.FIELD.TAG_ID).is(tenantAppUserTagMongodb.getTagId());
							Query updateQuery = Query.query(updateCriteria);

							Update update = new Update();
							update.set(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
							update.currentDate(TenantAppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);

							UpdateResult updateResult = mongoTemplate.updateFirst(updateQuery, update, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);
							TenantAppUserTagMongodb deletedTenantAppUserTagMongodb = mongoTemplate.findAndRemove(updateQuery, TenantAppUserTagMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TAG);

							if (deletedTenantAppUserTagMongodb != null) {
								mongoTemplate.insert(deletedTenantAppUserTagMongodb, MongodbConstants.DeletedCollection.TENANT_APP_USER_TAG);
								log.debug("用户标签删除成功: TenantId: {} AppId: {} TagId: {} ",
									tenantAppUserTagMongodb.getTenantId(),
									tenantAppUserTagMongodb.getAppId(),
									tenantAppUserTagMongodb.getTagId()
								);
							}
							return deletedTenantAppUserTagMongodb;
						} catch (Exception e) {
							log.info("删除用户标签失败", e);
							status.setRollbackOnly();
							return null;
						}
					});
				} catch (Exception e) {
					log.debug("用户标签删除失败: TenantId: {} AppId: {} TagId: {}  异常： {}",
						tenantAppUserTagMongodb.getTenantId(),
						tenantAppUserTagMongodb.getAppId(),
						tenantAppUserTagMongodb.getTagId(),
						e.getMessage()
					);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_user_tag_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_user_tag_by_deleted_tenant_app] message handler error", e);
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
