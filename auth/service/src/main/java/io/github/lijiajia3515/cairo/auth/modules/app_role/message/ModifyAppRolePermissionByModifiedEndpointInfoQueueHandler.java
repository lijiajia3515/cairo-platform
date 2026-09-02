package io.github.lijiajia3515.cairo.auth.modules.app_role.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
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
public class ModifyAppRolePermissionByModifiedEndpointInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public ModifyAppRolePermissionByModifiedEndpointInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{modifyAppRolePermissionByModifiedEndpointInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_app_role_permission_by_modified_endpoint_info] message handler start");
			ModifiedEndpointInfoMessage modifiedEndpointInfoMessage = objectMapper.readValue(payload, ModifiedEndpointInfoMessage.class);
			log.info("[modify_app_role_permission_by_modified_endpoint_info] ===> 已修改的终端：Id: {} AppId: {} OldEndpointId: {} OldEndpointName: {} NewEndpointId: {} NewEndpointName: {} EventCairoUserId: {} EventTime: {} ",
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
				log.debug("[modify_app_role_permission_by_modified_endpoint_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(AppRolePermissionMongodb.FIELD.APP_ID).is(modifiedEndpointInfoMessage.getAppId())
				.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(modifiedEndpointInfoMessage.getOldEndpointId());
			Query query = Query.query(criteria);
			List<AppRolePermissionMongodb> rolePermissionMongodbList = mongoTemplate.find(query, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);

			rolePermissionMongodbList.forEach(modifyAppRolePermissionMongodb -> {
				try {
					Criteria rolePermissionCriteria = Criteria
						.where(AppRolePermissionMongodb.FIELD.APP_ID).is(modifyAppRolePermissionMongodb.getAppId())
						.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(modifyAppRolePermissionMongodb.getRoleId())
						.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(modifyAppRolePermissionMongodb.getEndpointId());

					Query rolePermissionQuery = Query.query(rolePermissionCriteria);
					Update rolePermissionUpdate = new Update();
					rolePermissionUpdate.set(AppRolePermissionMongodb.FIELD.ENDPOINT_ID, modifiedEndpointInfoMessage.getNewEndpointId());
					rolePermissionUpdate.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					rolePermissionUpdate.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

					UpdateResult rolePermissionUpdateResult = mongoTemplate.updateMulti(rolePermissionQuery, rolePermissionUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
					log.debug("rolePermissionUpdateResult : {}", rolePermissionUpdateResult);


					Criteria roleCriteria = Criteria.where(AppRolePermissionMongodb.FIELD.APP_ID).is(modifyAppRolePermissionMongodb.getAppId())
						.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(modifyAppRolePermissionMongodb.getRoleId());

					Query roleQuery = Query.query(roleCriteria);
					Update roleUpdate = new Update();
					roleUpdate.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					roleUpdate.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult roleUpdateResult = mongoTemplate.updateMulti(roleQuery, roleUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
					log.debug("roleUpdateResult: {}", roleUpdateResult);
				} catch (Exception e) {
					log.debug("更新应用角色失败", e);
				}
			});


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_app_role_permission_by_modified_endpoint_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_app_role_permission_by_modified_endpoint_info] message handler error", e);
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
