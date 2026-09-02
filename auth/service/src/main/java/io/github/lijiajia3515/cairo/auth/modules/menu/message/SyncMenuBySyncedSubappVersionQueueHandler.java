package io.github.lijiajia3515.cairo.auth.modules.menu.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.SyncedSubappVersionMessage;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 同步菜单 队列 处理器
 */
@Slf4j
@Component
public class SyncMenuBySyncedSubappVersionQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final TransactionTemplate transactionTemplate;

	private final PublicFileClientApiService publicFileClientApiService;


	public SyncMenuBySyncedSubappVersionQueueHandler(ObjectMapper objectMapper,
											  MongoTemplate mongoTemplate,
											  RabbitTemplate rabbitTemplate,
											  CairoRabbitmqTool cairoRabbitmqTool,
											  TransactionTemplate transactionTemplate,
											  PublicFileClientApiService publicFileClientApiService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.transactionTemplate = transactionTemplate;
		this.publicFileClientApiService = publicFileClientApiService;
	}

	@RabbitListener(
		queues = {"#{syncMenuBySyncedSubappVersionQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[sync_menu_by_synced_subapp_version] message handler start");
			SyncedSubappVersionMessage syncedSubappVersionMessage = objectMapper.readValue(payload, SyncedSubappVersionMessage.class);
			log.info("[sync_menu_by_synced_subapp_version] ===> 数据来源：SubappId: {} SubappVersion: {} Time: {} ",
				syncedSubappVersionMessage.getSourceSubappId(),
				syncedSubappVersionMessage.getSourceSubappVersion(),
				syncedSubappVersionMessage.getEventTime()
			);
			Criteria criteria = Criteria
				.where(MenuMongodb.FIELD.SUBAPP_ID).is(syncedSubappVersionMessage.getSourceSubappId())
				.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(syncedSubappVersionMessage.getSourceSubappVersion());
			Query query = Query.query(criteria);
			List<MenuMongodb> menuList = mongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);


			log.info("[sync_menu_by_synced_subapp_version] ===> 需要变更： AppId: {} EndpointId: {} SubappId: {} SubappVersion: {} Time: {} ",
				syncedSubappVersionMessage.getChangeAppId(),
				syncedSubappVersionMessage.getChangeEndpointId(),
				syncedSubappVersionMessage.getChangeSubappId(),
				syncedSubappVersionMessage.getChangeSubappVersion(),
				syncedSubappVersionMessage.getEventTime()
			);
			transactionTemplate.executeWithoutResult(transactionStatus -> {
				try {
					Criteria deleteCriteria = Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(syncedSubappVersionMessage.getChangeAppId())
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(syncedSubappVersionMessage.getChangeEndpointId())
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(syncedSubappVersionMessage.getChangeSubappId())
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(syncedSubappVersionMessage.getChangeSubappVersion());
					Query deleteQuery = Query.query(deleteCriteria);
					List<MenuMongodb> deleteMenuLists = mongoTemplate.findAllAndRemove(deleteQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
					if (!deleteMenuLists.isEmpty()) {
						Collection<MenuMongodb> deleteMenus = mongoTemplate.insert(deleteMenuLists, MongodbConstants.DeletedCollection.MENU);
						log.info("[sync_menu_by_synced_subapp_version] [delete]===> :{}", deleteMenus);
					}
					List<MenuMongodb> insert = menuList.stream().map(menu -> {
						String icon = "";
						try {
							if (menu.getIcon() != null && !menu.getIcon().isBlank()) {
								String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
								MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(menu.getIcon(),
									fileName.concat(FilesUtil.getType(menu.getIcon())));
								List<String> urls = Optional.ofNullable(publicFileClientApiService.uploadFile(
									syncedSubappVersionMessage.getChangeAppId().concat(FileKeyPrefixConstants.MENU_ICON_PREFIX).concat(fileName).concat(FilesUtil.getType((FilesUtil.getType(menu.getIcon())))),
										multipartFile)).orElse(Collections.emptyList());
								if (urls.size() > 2) icon = urls.get(2);
							}
						} catch (Exception e) {
							log.info("[uploadPublicFile] error", e);
						}
						return MenuMongodb.builder()
							.appId(syncedSubappVersionMessage.getChangeAppId())
							.endpointId(syncedSubappVersionMessage.getChangeEndpointId())
							.subappId(syncedSubappVersionMessage.getChangeSubappId())
							.subappVersion(syncedSubappVersionMessage.getChangeSubappVersion())
							.menuId(menu.getMenuId())
							.parentId(menu.getParentId())
							.menuName(menu.getMenuName())
							.path(menu.getPath())
							.component(menu.getComponent())
							.icon(icon)
							.tags(menu.getTags())
							.hiddenMenu(menu.isHiddenMenu())
							.leftNo(menu.getLeftNo())
							.rightNo(menu.getRightNo())
							.depth(menu.getDepth())
							.metadata(AppUserMetadataMongodb.builder().createUserId(syncedSubappVersionMessage.getEventCairoUserId()).updateUserId(syncedSubappVersionMessage.getEventCairoUserId()).build())
							.build();
					}).collect(Collectors.toList());
					if (!insert.isEmpty()) {
						Collection<MenuMongodb> insertMenus = mongoTemplate.insert(insert, MongodbConstants.Collection.MENU);
						log.info("[sync_menu_by_synced_subapp_version] [insert]===> :{}", insertMenus);
					}
				} catch (Exception e) {
					log.warn("[create_user_by_created_tenant_app] handler error", e);
				}
			});
				//发送功能权限同步消息
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.SYNCED_MENU, syncedSubappVersionMessage.getChangeAppId()),
						objectMapper.writeValueAsString(syncedSubappVersionMessage),
						new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("e", e);
				}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[sync_menu_by_synced_subapp_version] message handler end");
		} catch (RuntimeException e) {
			log.info("[sync_menu_by_synced_subapp_version] message handler error", e);
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
