package io.github.lijiajia3515.cairo.auth.modules.app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
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
import java.util.stream.Collectors;


/**
 * 删除应用角色根据已删除应用 队列 处理器
 */
@Slf4j
@Component
public class DeleteAppRoleByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteAppRoleByDeletedAppQueueHandler(ObjectMapper objectMapper,
														  MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAppRoleByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_role_by_deleted_tenant_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_app_role_by_deleted_tenant_app] ===> 已删除应用:  AppId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppRoleMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<AppRoleMongodb> deletedEndpointList = mongoTemplate.findAllAndRemove(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
			if (!deletedEndpointList.isEmpty()){
				mongoTemplate.insert(deletedEndpointList, MongodbConstants.DeletedCollection.APP_ROLE);
			}

			//删除权限
			Criteria appRolePermissionCriteria = Criteria
				.where(AppRolePermissionMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId())
				.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(deletedEndpointList.stream().map(AppRoleMongodb::getRoleId).collect(Collectors.toSet()));
			Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
			Update appRolePermissionUpdate = new Update();
			appRolePermissionUpdate.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
			appRolePermissionUpdate.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

			UpdateResult appRolePermissionUpdateResult = mongoTemplate.updateMulti(appRolePermissionQuery, appRolePermissionUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
			List<AppRolePermissionMongodb> deleteAppRolePermissionMongodbList = mongoTemplate.findAllAndRemove(appRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
			if (!deleteAppRolePermissionMongodbList.isEmpty()) {
				mongoTemplate.insert(deleteAppRolePermissionMongodbList, MongodbConstants.DeletedCollection.APP_ROLE_PERMISSION);
			}

			deletedEndpointList.forEach(departmentMongodb -> {
				log.debug("角色删除成功:  AppId: {} RoleId: {} RoleName: {}",
					departmentMongodb.getAppId(),
					departmentMongodb.getRoleId(),
					departmentMongodb.getRoleName()
				);
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_app_role_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_role_by_deleted_tenant_app] message handler error", e);
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
