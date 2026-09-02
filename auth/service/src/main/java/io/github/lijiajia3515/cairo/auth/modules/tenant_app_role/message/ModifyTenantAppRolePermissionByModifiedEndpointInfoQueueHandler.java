package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
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
 * 更新角色权限根据已更新的终端 队列处理器
 */
@Slf4j
@Component
public class ModifyTenantAppRolePermissionByModifiedEndpointInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public ModifyTenantAppRolePermissionByModifiedEndpointInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{modifyTenantRolePermissionByModifiedEndpointInfoQueue.getName()}"}
	)
	public void modifyTenantRolePermissionByModifiedEndpointInfoQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_tenant_role_permission_by_modified_endpoint_info] message handler start");
			ModifiedEndpointInfoMessage modifiedEndpointInfoMessage = objectMapper.readValue(payload, ModifiedEndpointInfoMessage.class);
			log.info("[modify_tenant_role_permission_by_modified_endpoint_info] ===> 已修改的终端：Id: {} AppId: {} OldEndpointId: {} OldEndpointName: {} NewEndpointId: {} NewEndpointName: {} EventCairoUserId: {} EventTime: {} ",
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
				log.debug("[modify_tenant_role_permission_by_modified_endpoint_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(modifiedEndpointInfoMessage.getAppId())
				.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(modifiedEndpointInfoMessage.getOldEndpointId());
			Query query = Query.query(criteria);
			List<TenantAppRolePermissionMongodb> rolePermissionMongodbList = mongoTemplate.find(query, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);

			rolePermissionMongodbList.forEach(modifyRolePermissionMongodb -> {
				try {
					Criteria rolePermissionCriteria = Criteria
						.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(modifyRolePermissionMongodb.getTenantId())
						.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(modifyRolePermissionMongodb.getAppId())
						.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(modifyRolePermissionMongodb.getRoleId())
						.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(modifyRolePermissionMongodb.getEndpointId());

					Query rolePermissionQuery = Query.query(rolePermissionCriteria);
					Update rolePermissionUpdate = new Update();
					rolePermissionUpdate.set(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID, modifiedEndpointInfoMessage.getNewEndpointId());
					rolePermissionUpdate.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					rolePermissionUpdate.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

					UpdateResult rolePermissionUpdateResult = mongoTemplate.updateMulti(rolePermissionQuery, rolePermissionUpdate, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
					log.debug("rolePermissionUpdateResult : {}", rolePermissionUpdateResult);


					Criteria roleCriteria = Criteria.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(modifyRolePermissionMongodb.getTenantId())
						.and(TenantAppRoleMongodb.FIELD.APP_ID).is(modifyRolePermissionMongodb.getAppId())
						.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(modifyRolePermissionMongodb.getRoleId());

					Query roleQuery = Query.query(roleCriteria);
					Update roleUpdate = new Update();
					roleUpdate.set(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					roleUpdate.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult roleUpdateResult = mongoTemplate.updateMulti(roleQuery, roleUpdate, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
					log.debug("roleUpdateResult: {}", roleUpdateResult);
				} catch (Exception e) {
					log.debug("更新角色失败", e);
				}
			});


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_tenant_role_permission_by_modified_endpoint_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_tenant_role_permission_by_modified_endpoint_info] message handler error", e);
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
