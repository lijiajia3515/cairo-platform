package io.github.lijiajia3515.cairo.auth.modules.menu.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
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
 * 删除菜单根据已删除的子应用版本 队列处理器
 */
@Slf4j
@Component
public class DeleteMenuByDeletedSubappVersionQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final FileCommonService fileCommonService;
	private final TransactionTemplate transactionTemplate;

	public DeleteMenuByDeletedSubappVersionQueueHandler(ObjectMapper objectMapper,
												 MongoTemplate mongoTemplate,
												 TransactionTemplate transactionTemplate,
												 FileCommonService fileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.fileCommonService = fileCommonService;
		this.transactionTemplate = transactionTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteMenuByDeletedSubappVersionQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_menu_by_deleted_subapp_version] message handler start");
			DeletedSubappVersionMessage deletedSubappVersionMessage = objectMapper.readValue(payload, DeletedSubappVersionMessage.class);
			log.info("[delete_menu_by_deleted_subapp_version] ===> 已删除子应用版本： AppId: {} EndpointId: {} SubappId: {} SubappVersionId: {} EventCairoUserId: {} EventTime: {} ",
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
						.where(MenuMongodb.FIELD.APP_ID).is(deletedSubappVersionMessage.getAppId())
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(deletedSubappVersionMessage.getEndpointId())
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(deletedSubappVersionMessage.getSubappId())
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(deletedSubappVersionMessage.getSubappVersion());
					Query query = Query.query(criteria);
					List<MenuMongodb> deletedMenuList = mongoTemplate.findAllAndRemove(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
					if (!deletedMenuList.isEmpty()) {
						Collection<MenuMongodb> insert = mongoTemplate.insert(deletedMenuList, MongodbConstants.DeletedCollection.MENU);
						List<String> icons = insert.stream().map(MenuMongodb::getIcon).toList();
						iconList.addAll(icons);
					}
					deletedMenuList.forEach(deletedPermissionMongodb -> {
						log.debug("菜单删除成功: AppId: {} EndpointId: {} SubappId: {} SubappVersion: {}  MenuId： {} MenuName: {}",
							deletedPermissionMongodb.getAppId(),
							deletedPermissionMongodb.getEndpointId(),
							deletedPermissionMongodb.getSubappId(),
							deletedPermissionMongodb.getSubappVersion(),
							deletedPermissionMongodb.getMenuId(),
							deletedPermissionMongodb.getMenuName()
						);
					});
				} catch (BusinessException e) {
					status.setRollbackOnly();
				} catch (Exception e) {
					log.debug("deleteMenu", e);
					status.setRollbackOnly();
				}

			});
			//删除图标
			fileCommonService.deletePublicFile(deletedSubappVersionMessage.getAppId().concat(FileKeyPrefixConstants.MENU_ICON_PREFIX), iconList);
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_menu_by_deleted_subapp_version] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_menu_by_deleted_subapp_version] message handler error", e);
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
