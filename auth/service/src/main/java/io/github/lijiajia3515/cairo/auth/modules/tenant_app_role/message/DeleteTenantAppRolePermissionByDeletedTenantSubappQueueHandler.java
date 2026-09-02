package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.DeletedTenantSubappMessage;
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
 * 删除角色权限 队列 处理器
 */
@Slf4j
@Component
public class DeleteTenantAppRolePermissionByDeletedTenantSubappQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteTenantAppRolePermissionByDeletedTenantSubappQueueHandler(ObjectMapper objectMapper,
																	MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteTenantRolePermissionByDeletedTenantSubappQueue.getName()}"}
	)
	public void deleteTenantRolePermissionByDeletedTenantAppQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_role_permission_by_deleted_tenant_subapp] message handler start");
			DeletedTenantSubappMessage deletedTenantSubappMessage = objectMapper.readValue(payload, DeletedTenantSubappMessage.class);
			log.info("[delete_tenant_role_permission_by_deleted_tenant_subapp] ===> 已删除企业应用： AppId: {} TenantId: {} subappId: {} EndpointId{},EventTime: {} ",
				deletedTenantSubappMessage.getAppId(),
				deletedTenantSubappMessage.getTenantId(),
				deletedTenantSubappMessage.getSubappId(),
				deletedTenantSubappMessage.getEndpointId(),
				deletedTenantSubappMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppRoleMongodb.FIELD.APP_ID).is(deletedTenantSubappMessage.getAppId())
				.and(TenantAppRoleMongodb.FIELD.TENANT_ID).is(deletedTenantSubappMessage.getTenantId())
				.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(deletedTenantSubappMessage.getEndpointId())
				.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(deletedTenantSubappMessage.getSubappId());
			Query query = Query.query(criteria);

			List<TenantAppRolePermissionMongodb> rolePermissionMongodbList = mongoTemplate.find(query, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);

			rolePermissionMongodbList.forEach(rolePermissionMongodb -> {
				try {
					Criteria rolePermissionCriteria = Criteria
						.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(rolePermissionMongodb.getTenantId())
						.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(rolePermissionMongodb.getAppId())
						.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(deletedTenantSubappMessage.getEndpointId())
						.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(deletedTenantSubappMessage.getSubappId())
						.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(rolePermissionMongodb.getRoleId());
					Query rolePermissionQuery = Query.query(rolePermissionCriteria);
					Update update = new Update();
					update.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					update.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(rolePermissionQuery, update, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
					TenantAppRolePermissionMongodb deletedRolePermission = mongoTemplate.findAndRemove(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
					if (deletedRolePermission != null) {
						mongoTemplate.insert(deletedRolePermission, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_PERMISSION);
					}
					log.debug("角色权限删除成功: TenantId: {} AppId: {} RoleId: {} EndpointId: {} 状态： {}",
						rolePermissionMongodb.getTenantId(),
						rolePermissionMongodb.getAppId(),
						rolePermissionMongodb.getRoleId(),
						rolePermissionMongodb.getEndpointId(),
						deletedRolePermission
					);
				} catch (Exception e) {
					log.debug("角色权限删除失败: TenantId: {} AppId: {} RoleId: {} EndpointId: {} 错误： {}",
						rolePermissionMongodb.getTenantId(),
						rolePermissionMongodb.getAppId(),
						rolePermissionMongodb.getRoleId(),
						rolePermissionMongodb.getEndpointId(),
						e.getMessage());
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_role_permission_by_deleted_tenant_subapp] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_role_permission_by_deleted_tenant_subapp] message handler error", e);
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
