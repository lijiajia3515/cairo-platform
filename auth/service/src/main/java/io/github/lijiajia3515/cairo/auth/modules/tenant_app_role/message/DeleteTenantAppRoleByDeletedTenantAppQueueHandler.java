package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.stream.Collectors;


/**
 * 删除角色队列处理器
 */
@Slf4j
@Component
public class DeleteTenantAppRoleByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteTenantAppRoleByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
																   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppRoleByDeletedTenantAppQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_app_department_by_deleted_tenant_app] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[delete_tenant_app_department_by_deleted_tenant_app] ===> 已删除企业应用: TenantId: {} AppId: {} EventAccountId: {} EventTime: {} ",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId(),
				deletedTenantAppMessage.getEventAccountId(),
				deletedTenantAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantAppRoleMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<TenantAppRoleMongodb> deletedEndpointList = mongoTemplate.findAllAndRemove(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
			if (!deletedEndpointList.isEmpty()){
				mongoTemplate.insert(deletedEndpointList, MongodbConstants.DeletedCollection.TENANT_APP_DEPARTMENT);
			}
			//删除权限
			Criteria rolePermissionCriteria = Criteria
				.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId())
				.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(deletedEndpointList.stream().map(TenantAppRoleMongodb::getRoleId).collect(Collectors.toSet()));
			Query rolePermissionQuery = Query.query(rolePermissionCriteria);
			Update rolePermissionUpdate = new Update();
			rolePermissionUpdate.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
			rolePermissionUpdate.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

			UpdateResult rolePermissionUpdateResult = mongoTemplate.updateMulti(rolePermissionQuery, rolePermissionUpdate, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
			List<TenantAppRolePermissionMongodb> deleteRolePermissionMongodbList = mongoTemplate.findAllAndRemove(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
			if (!deleteRolePermissionMongodbList.isEmpty()) {
				mongoTemplate.insert(deleteRolePermissionMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_PERMISSION);
			}



			deletedEndpointList.forEach(departmentMongodb -> {
				log.debug("角色删除成功: TenantId: {} AppId: {} RoleId: {} RoleName: {}",
					departmentMongodb.getTenantId(),
					departmentMongodb.getAppId(),
					departmentMongodb.getRoleId(),
					departmentMongodb.getRoleName()
				);
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_app_department_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_app_department_by_deleted_tenant_app] message handler error", e);
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
