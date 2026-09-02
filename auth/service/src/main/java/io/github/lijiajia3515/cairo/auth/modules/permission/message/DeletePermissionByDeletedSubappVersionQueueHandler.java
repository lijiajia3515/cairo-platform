package io.github.lijiajia3515.cairo.auth.modules.permission.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.DeletedSubappVersionMessage;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * 删除功能权限根据已删除的子应用版本 队列 处理器
 */
@Slf4j
@Component
public class DeletePermissionByDeletedSubappVersionQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final FileCommonService fileCommonService;
	private final TransactionTemplate transactionTemplate;

	public DeletePermissionByDeletedSubappVersionQueueHandler(ObjectMapper objectMapper,
															 MongoTemplate mongoTemplate,
															 FileCommonService fileCommonService,
															 TransactionTemplate transactionTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.fileCommonService = fileCommonService;
		this.transactionTemplate = transactionTemplate;
	}

	@RabbitListener(
		queues = {"#{deletePermissionByDeletedSubappVersionQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_role_permission_by_deleted_subapp_version] message handler start");
			DeletedSubappVersionMessage deletedSubappVersionMessage = objectMapper.readValue(payload, DeletedSubappVersionMessage.class);
			log.info("[delete_role_permission_by_deleted_subapp_version] ===> 已删除子应用版本： AppId: {} EndpointId: {} SubappId: {} SubappVersion: {} EventCairoUserId: {} EventTime: {} ",
				deletedSubappVersionMessage.getAppId(),
				deletedSubappVersionMessage.getEndpointId(),
				deletedSubappVersionMessage.getSubappId(),
				deletedSubappVersionMessage.getSubappVersion(),
				deletedSubappVersionMessage.getEventCairoUserId(),
				deletedSubappVersionMessage.getEventTime()
			);
			List<String> iconList = new ArrayList<>();
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria criteria = Criteria
						.where(PermissionMongodb.FIELD.APP_ID).is(deletedSubappVersionMessage.getAppId())
						.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(deletedSubappVersionMessage.getEndpointId())
						.and(PermissionMongodb.FIELD.SUBAPP_ID).is(deletedSubappVersionMessage.getSubappId())
						.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(deletedSubappVersionMessage.getSubappVersion());
					Query query = Query.query(criteria);
					List<PermissionMongodb> deletedPermissionList = mongoTemplate.findAllAndRemove(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
					if (!deletedPermissionList.isEmpty()) {
						Collection<PermissionMongodb> insert = mongoTemplate.insert(deletedPermissionList, MongodbConstants.DeletedCollection.PERMISSION);
						List<String> icons = insert.stream().map(PermissionMongodb::getIcon).toList();
						iconList.addAll(icons);
					}
					deletedPermissionList.forEach(deletedPermissionMongodb -> {
						log.debug("功能权限删除成功: AppId: {} EndpointId: {} SubappId: {} SubappVersion: {}  PermissionId： {} PermissionName: {}",
							deletedPermissionMongodb.getAppId(),
							deletedPermissionMongodb.getEndpointId(),
							deletedSubappVersionMessage.getSubappId(),
							deletedSubappVersionMessage.getSubappVersion(),
							deletedPermissionMongodb.getPermissionId(),
							deletedPermissionMongodb.getPermissionName()
						);
					});
				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("deleteRolePermission", e);
					status.setRollbackOnly();
				}

			});
			//删除图标
			fileCommonService.deletePublicFile(deletedSubappVersionMessage.getAppId().concat("/").concat(FileKeyPrefixConstants.PERMISSION_ICON_PREFIX), iconList);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_role_permission_by_deleted_subapp_version] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_role_permission_by_deleted_subapp_version] message handler error", e);
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
