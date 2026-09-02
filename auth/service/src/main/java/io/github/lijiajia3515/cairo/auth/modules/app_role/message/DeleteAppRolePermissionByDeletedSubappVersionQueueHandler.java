package io.github.lijiajia3515.cairo.auth.modules.app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.DeletedSubappVersionMessage;
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
 * 删除应用角色权限根据已删除子应用版本 队列 处理器
 */
@Slf4j
@Component
public class DeleteAppRolePermissionByDeletedSubappVersionQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public DeleteAppRolePermissionByDeletedSubappVersionQueueHandler(ObjectMapper objectMapper,
														   MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAppRolePermissionByDeletedSubappVersionQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_role_permission_by_deleted_subapp_version] message handler start");
			DeletedSubappVersionMessage deletedSubappVersionMessage = objectMapper.readValue(payload, DeletedSubappVersionMessage.class);
			log.info("[delete_app_role_permission_by_deleted_subapp_version] ===> 已删除子应用版本： AppId: {} EndpointId: {} SubappId: {},SubappVersion: {} EventCairoUserId: {} EventTime: {} ",
				deletedSubappVersionMessage.getAppId(),
				deletedSubappVersionMessage.getEndpointId(),
				deletedSubappVersionMessage.getSubappId(),
				deletedSubappVersionMessage.getSubappVersion(),
				deletedSubappVersionMessage.getEventCairoUserId(),
				deletedSubappVersionMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppRolePermissionMongodb.FIELD.APP_ID).is(deletedSubappVersionMessage.getAppId())
				.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(deletedSubappVersionMessage.getEndpointId())
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(deletedSubappVersionMessage.getSubappId())
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(deletedSubappVersionMessage.getSubappVersion());
			Query query = Query.query(criteria);

			List<AppRolePermissionMongodb> rolePermissionMongodbList = mongoTemplate.find(query, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);

			rolePermissionMongodbList.forEach(rolePermissionMongodb -> {
				try {
					Criteria rolePermissionCriteria = Criteria
						.where(AppRolePermissionMongodb.FIELD.APP_ID).is(rolePermissionMongodb.getAppId())
						.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(rolePermissionMongodb.getEndpointId());
					Query rolePermissionQuery = Query.query(rolePermissionCriteria);
					Update update = new Update();
					update.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					update.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(rolePermissionQuery, update, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
					AppRolePermissionMongodb deletedRolePermission = mongoTemplate.findAndRemove(rolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
					if (deletedRolePermission != null) {
						mongoTemplate.insert(deletedRolePermission, MongodbConstants.DeletedCollection.APP_ROLE_PERMISSION);
					}
					log.debug("应用角色权限删除成功:  AppId: {} RoleId: {} EndpointId: {} 状态： {}",
						rolePermissionMongodb.getAppId(),
						rolePermissionMongodb.getRoleId(),
						rolePermissionMongodb.getEndpointId(),
						deletedRolePermission
					);
				} catch (Exception e) {
					log.debug("应用角色权限删除失败:AppId: {} RoleId: {} EndpointId: {} 错误： {}",
						rolePermissionMongodb.getAppId(),
						rolePermissionMongodb.getRoleId(),
						rolePermissionMongodb.getEndpointId(),
						e.getMessage());
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_app_role_permission_by_deleted_subapp_version] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_role_permission_by_deleted_subapp_version] message handler error", e);
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
