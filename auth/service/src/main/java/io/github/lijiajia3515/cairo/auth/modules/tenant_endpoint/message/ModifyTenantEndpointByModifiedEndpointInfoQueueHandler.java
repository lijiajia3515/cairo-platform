package io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
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
import java.util.List;
import java.util.Map;


/**
 * 更新应用角色权限根据已更新的终端 队列处理器
 */
@Slf4j
@Component
public class ModifyTenantEndpointByModifiedEndpointInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public ModifyTenantEndpointByModifiedEndpointInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{modifyTenantEndpointByModifiedEndpointInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_tenant_endpoint_by_modified_endpoint_info] message handler start");
			ModifiedEndpointInfoMessage modifiedEndpointInfoMessage = objectMapper.readValue(payload, ModifiedEndpointInfoMessage.class);
			log.info("[modify_tenant_endpoint_by_modified_endpoint_info] ===> 已修改的终端：Id: {} AppId: {} OldEndpointId: {} OldEndpointName: {} NewEndpointId: {} NewEndpointName: {} EventCairoUserId: {} EventTime: {} ",
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
				log.debug("未更新EndpointId, 无需更新Permission");
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[modify_tenant_endpoint_by_modified_endpoint_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(TenantEndpointMongodb.FIELD.APP_ID).is(modifiedEndpointInfoMessage.getAppId())
				.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(modifiedEndpointInfoMessage.getOldEndpointId());
			Query query = Query.query(criteria);
			List<TenantEndpointMongodb> tenantEndpoints = mongoTemplate.find(query, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);

			tenantEndpoints.forEach(modifyAppRolePermissionMongodb -> {
				try {
					Criteria rolePermissionCriteria = Criteria
						.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(modifyAppRolePermissionMongodb.getTenantId())
						.and(TenantEndpointMongodb.FIELD.APP_ID).is(modifyAppRolePermissionMongodb.getAppId())
						.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).is(modifyAppRolePermissionMongodb.getEndpointId());


					Query rolePermissionQuery = Query.query(rolePermissionCriteria);
					Update tenantEndpointUpdate = new Update();
					tenantEndpointUpdate.set(TenantEndpointMongodb.FIELD.ENDPOINT_ID, modifiedEndpointInfoMessage.getNewEndpointId());
					tenantEndpointUpdate.currentDate(TenantEndpointMongodb.FIELD.METADATA.UPDATE_TIME);

					UpdateResult tenantEndpointUpdateResult = mongoTemplate.updateMulti(rolePermissionQuery, tenantEndpointUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
					log.debug("tenantEndpointUpdateResult : {}", tenantEndpointUpdateResult);

				} catch (Exception e) {
					log.debug("更新企业终端失败", e);
				}
			});


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_tenant_endpoint_by_modified_endpoint_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_tenant_endpoint_by_modified_endpoint_info] message handler error", e);
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
