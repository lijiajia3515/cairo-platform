package io.github.lijiajia3515.cairo.auth.modules.permission.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp_version.SyncedSubappVersionMessage;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * 同步功能权限 队列 处理器
 */
@Slf4j
@Component
public class SyncPermissionBySyncedMenuQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final PublicFileClientApiService publicFileClientApiService;
	private final FileCommonService fileCommonService;
	private final TransactionTemplate transactionTemplate;

	public SyncPermissionBySyncedMenuQueueHandler(ObjectMapper objectMapper,
														MongoTemplate mongoTemplate,
														PublicFileClientApiService publicFileClientApiService,
														FileCommonService fileCommonService,
														TransactionTemplate transactionTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.publicFileClientApiService = publicFileClientApiService;
		this.fileCommonService = fileCommonService;
		this.transactionTemplate = transactionTemplate;
	}

	@RabbitListener(
		queues = {"#{syncPermissionBySyncedMenuQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[syncPermissionQueue] message handler start");
			SyncedSubappVersionMessage syncedSubappVersionMessage = objectMapper.readValue(payload, SyncedSubappVersionMessage.class);
			log.info("[syncPermissionQueue] ===> 数据来源： SubappId: {} SubappVersion: {} Time: {} ",
				syncedSubappVersionMessage.getSourceSubappId(),
				syncedSubappVersionMessage.getSourceSubappVersion(),
				syncedSubappVersionMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(PermissionMongodb.FIELD.SUBAPP_ID).is(syncedSubappVersionMessage.getSourceSubappId())
				.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(syncedSubappVersionMessage.getSourceSubappVersion());
			Query query = Query.query(criteria);
			List<PermissionMongodb> permissionList = mongoTemplate.find(query, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);

			log.info("[syncPermissionQueue] ===> 改变数据： AppId: {} EndpointId: {} SubappId: {} SubappVersion: {} Time: {} ",
				syncedSubappVersionMessage.getChangeAppId(),
				syncedSubappVersionMessage.getChangeEndpointId(),
				syncedSubappVersionMessage.getChangeSubappId(),
				syncedSubappVersionMessage.getChangeSubappVersion(),
				syncedSubappVersionMessage.getEventTime()
			);
			List<String> deleteIcons = new ArrayList<>();
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria deleteCriteria = Criteria
						.where(PermissionMongodb.FIELD.APP_ID).is(syncedSubappVersionMessage.getChangeAppId())
						.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(syncedSubappVersionMessage.getChangeEndpointId())
						.and(PermissionMongodb.FIELD.SUBAPP_ID).is(syncedSubappVersionMessage.getChangeSubappId())
						.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(syncedSubappVersionMessage.getChangeSubappVersion());
					Query deleteQuery = Query.query(deleteCriteria);
					List<PermissionMongodb> deletePermissionList = mongoTemplate.findAllAndRemove(deleteQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
					log.info("[syncPermissionQueue] [delete]===> :{}", deletePermissionList);
					if (!deletePermissionList.isEmpty()) {
						mongoTemplate.insert(deletePermissionList, MongodbConstants.DeletedCollection.PERMISSION);
						deleteIcons.addAll(deletePermissionList.stream().map(PermissionMongodb::getIcon).collect(Collectors.toList()));
					}
					//新增
					List<PermissionMongodb> insert = permissionList.stream().map(permission -> {

						String icon = null;
						try {
							if (permission.getIcon() != null && !permission.getIcon().isBlank()) {
								String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
								MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(permission.getIcon(),
									fileName.concat(FilesUtil.getType(permission.getIcon())));
								List<String> urls = Optional.ofNullable(publicFileClientApiService.uploadFile(
										syncedSubappVersionMessage.getChangeAppId().concat(FileKeyPrefixConstants.PERMISSION_ICON_PREFIX).concat(fileName).concat(FilesUtil.getType((FilesUtil.getType(permission.getIcon())))),
										multipartFile)).orElse(Collections.emptyList());
								if (urls.size() > 2) icon = urls.get(2);
							}
						} catch (Exception e) {
							log.error("uploadPublicFile: {}", e.getMessage());
						}
						return PermissionMongodb.builder()
							.appId(syncedSubappVersionMessage.getChangeAppId())
							.endpointId(syncedSubappVersionMessage.getChangeEndpointId())
							.subappId(syncedSubappVersionMessage.getChangeSubappId())
							.subappVersion(syncedSubappVersionMessage.getChangeSubappVersion())
							.menuId(permission.getMenuId())
							.permissionId(permission.getPermissionId())
							.permissionName(permission.getPermissionName())
							.authorities(permission.getAuthorities())
							.defaultPermission(Optional.ofNullable(permission.getDefaultPermission()).orElse(false))
							.hiddenPermission(Optional.ofNullable(permission.getHiddenPermission()).orElse(false))
							.icon(icon)
							.sort(permission.getSort())
							.type(permission.getType())
							.metadata(AppUserMetadataMongodb.builder().createUserId(syncedSubappVersionMessage.getEventCairoUserId()).updateUserId(syncedSubappVersionMessage.getEventCairoUserId()).build())
							.build();
					}).collect(Collectors.toList());
					if (!insert.isEmpty()) {
						Collection<PermissionMongodb> insertPermissions = mongoTemplate.insert(insert, MongodbConstants.Collection.PERMISSION);
						log.info("[syncPermissionQueue] [insert]===> :{}", insertPermissions);
					}
				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.error("syncPermissionQueue", e);
					status.setRollbackOnly();
				}
			});
			//删除图标
			if (!deleteIcons.isEmpty()) {
				fileCommonService.deletePublicFile(syncedSubappVersionMessage.getChangeAppId().concat(FileKeyPrefixConstants.PERMISSION_ICON_PREFIX), deleteIcons);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[syncPermissionQueue] message handler end");
		} catch (RuntimeException e) {
			log.info("[syncPermissionQueue] message handler error", e);
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
