package io.github.lijiajia3515.cairo.auth.modules.dict.sys.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.CopySysDictMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.DeleteSysDictMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 复制系统级字典
 */
@Slf4j
@Component
public class CopySysDictByDictIdQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final PublicFileCommonService publicFileCommonService;

	public CopySysDictByDictIdQueueHandler(ObjectMapper objectMapper,
														  MongoTemplate mongoTemplate,
														  TransactionTemplate transactionTemplate,
														  RabbitTemplate rabbitTemplate,
														  CairoRabbitmqTool cairoRabbitmqTool,
														  PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.publicFileCommonService = publicFileCommonService;
	}

	@RabbitListener(
		queues = {"#{copySysDictByDictIdQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[copySysDictByDictId] message handler start");
			CopySysDictMessage copySysDictMessage = objectMapper.readValue(payload, CopySysDictMessage.class);
			log.info("[copySysDictByDictId] ===> 复制系统级字典：CurrentAppId:{} CopyAppId: {} DictId: {} EventTime: {} ",
				copySysDictMessage.getCurrentAppId(),
				copySysDictMessage.getCopyAppId(),
				copySysDictMessage.getDictId(),
				copySysDictMessage.getEventTime()
			);

			//查询要复制字典,字典项
			Criteria sdCopyCriteria = Criteria
				.where(SysDictMongodb.FIELD.APP_ID).is(copySysDictMessage.getCopyAppId())
				.and(SysDictMongodb.FIELD.DICT_ID).is(copySysDictMessage.getDictId());
			Query sdQuery = Query.query(sdCopyCriteria);
			SysDictMongodb sdCopyMongodb = mongoTemplate.findOne(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

			if (sdCopyMongodb == null) {
				throw new ConflictBusinessException("字典不存在");
			}

			Criteria sdiCopyCriteria = Criteria.where(SysDictItemMongodb.FIELD.APP_ID).is(copySysDictMessage.getCopyAppId())
				.and(SysDictItemMongodb.FIELD.DICT_ID).is(copySysDictMessage.getDictId());
			Query sdiCopyQuery = Query.query(sdiCopyCriteria);
			List<SysDictItemMongodb> sdiCopyMongodbs = mongoTemplate.find(sdiCopyQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);


			//删除当前应用字典,字典项,业务级字典字典项
			Criteria sdDeleteCriteria = Criteria
				.where(SysDictMongodb.FIELD.APP_ID).is(copySysDictMessage.getCurrentAppId())
				.and(SysDictMongodb.FIELD.DICT_ID).is(copySysDictMessage.getDictId());
			Query sdDeleteQuery = Query.query(sdDeleteCriteria);
			Update update = Update.update(SysDictMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, copySysDictMessage.getEventAccountId());
			update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);

			// 字典项
			Criteria sdiDeleteCriteria = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(copySysDictMessage.getCurrentAppId())
				.and(SysDictItemMongodb.FIELD.DICT_ID).is(copySysDictMessage.getDictId());
			Query sdiDeleteQuery = Query.query(sdiDeleteCriteria);
			Update sdiUpdate = Update.update(SysDictItemMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, copySysDictMessage.getEventAccountId());
			update.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);


			transactionTemplate.executeWithoutResult(status -> {
				try {
					//删除字典
					mongoTemplate.updateMulti(sdDeleteQuery, update, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
					SysDictMongodb deleteSdMongodb = mongoTemplate.findAndRemove(sdDeleteQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
					if (deleteSdMongodb != null) {
						mongoTemplate.insert(deleteSdMongodb, MongodbConstants.DeletedCollection.SYS_DICT);

					}
					//删除字典项
					mongoTemplate.updateMulti(sdiDeleteQuery, sdiUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					List<SysDictItemMongodb> deleteSdiMongodbList = mongoTemplate.findAllAndRemove(sdiDeleteQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					if (!deleteSdiMongodbList.isEmpty()) {
						mongoTemplate.insert(deleteSdiMongodbList, MongodbConstants.DeletedCollection.SYS_DICT_ITEM);
					}
					//添加新字典
					String icon = null;
					try {
						if (sdCopyMongodb.getIcon() != null && !sdCopyMongodb.getIcon().isBlank()) {
							String fileName = CoreConstants.nextIdStr();
							MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdCopyMongodb.getIcon(),
								fileName.concat(FilesUtil.getType(sdCopyMongodb.getIcon())));
							if (multipartFile != null) {
								List<String> urls = publicFileCommonService.uploadFile(copySysDictMessage.getCurrentAppId()
										.concat("/")
										.concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON)
										.concat("/")
										.concat(fileName)
										.concat(FilesUtil.getType((FilesUtil.getType(sdCopyMongodb.getIcon())))),
									multipartFile);
								if (urls.size() > 2) icon = urls.get(2);
							}
						}
					} catch (Exception e) {
						log.info("[sd uploadPublicFile] error", e);
					}
					SysDictMongodb sd = SysDictMongodb.builder()
						.dictId(sdCopyMongodb.getDictId())
						.appId(copySysDictMessage.getCurrentAppId())
						.dictType(sdCopyMongodb.getDictType())
						.dictName(sdCopyMongodb.getDictName())
						.icon(icon)
						.isCreateItem(sdCopyMongodb.getIsCreateItem())
						.enabled(sdCopyMongodb.getEnabled())
						.leftNo(sdCopyMongodb.getLeftNo())
						.rightNo(sdCopyMongodb.getRightNo())
						.metadata(AccountMetadataMongodb.builder()
							.createAccountId(copySysDictMessage.getEventAccountId())
							.updateAccountId(copySysDictMessage.getEventAccountId())
							.build())
						.build();

					//添加新字典项
					List<SysDictItemMongodb> sdiList = sdiCopyMongodbs.stream().map(sdi -> {
						String sdiIcon = null;
						try {
							if (sdi.getIcon() != null && !sdi.getIcon().isBlank()) {
								String fileName = CoreConstants.nextIdStr();
								MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdi.getIcon(),
									fileName.concat(FilesUtil.getType(sdi.getIcon())));
								if (multipartFile != null) {
									List<String> urls = publicFileCommonService.uploadFile(
										copySysDictMessage.getCurrentAppId().concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON).concat("/").concat(fileName).concat(FilesUtil.getType((FilesUtil.getType(sdi.getIcon())))),
										multipartFile);
									if (urls.size() > 2) sdiIcon = urls.get(2);
								}
							}
						} catch (Exception e) {
							log.info("[sdi uploadPublicFile] error", e);
						}
						return SysDictItemMongodb.builder()
							.dictId(sdi.getDictId())
							.appId(copySysDictMessage.getCurrentAppId())
							.itemId(sdi.getItemId())
							.itemName(sdi.getItemName())
							.icon(sdiIcon)
							.depth(sdi.getDepth())
							.parentItemId(sdi.getParentItemId())
							.leftNo(sdi.getLeftNo())
							.rightNo(sdi.getRightNo())
							.editable(sdi.getEditable())
							.enabled(sdi.getEnabled())
							.metadata(AccountMetadataMongodb.builder()
								.createAccountId(copySysDictMessage.getEventAccountId())
								.updateAccountId(copySysDictMessage.getEventAccountId())
								.build())
							.build();
					}).collect(Collectors.toList());
					mongoTemplate.insert(sd, MongodbConstants.Collection.SYS_DICT);
					mongoTemplate.insert(sdiList, MongodbConstants.Collection.SYS_DICT_ITEM);

					//删除字典图标
					if (deleteSdMongodb != null && deleteSdMongodb.getIcon() != null) {
						publicFileCommonService.deleteFile(copySysDictMessage.getCurrentAppId().concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON), Collections.singletonList(deleteSdMongodb.getIcon()));
					}

					//删除字典项图标
					List<String> sdiIconUrls = deleteSdiMongodbList.stream().map(SysDictItemMongodb::getIcon).collect(Collectors.toList());
					publicFileCommonService.deleteFile(copySysDictMessage.getCurrentAppId().concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON), sdiIconUrls);

					//发送删除系统级字典完成消息
					try {
						if (deleteSdMongodb != null) {
							rabbitTemplate.convertAndSend(
								cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
								cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SYS_DICT, copySysDictMessage.getCurrentAppId()),
								objectMapper.writeValueAsString(DeleteSysDictMessage.builder()
									.eventCairoUserId(copySysDictMessage.getEventAccountId())
									.eventTime(LocalDateTime.now())
									.appId(copySysDictMessage.getCurrentAppId())
									.dictId(deleteSdMongodb.getDictId())
									.build()
								),
								new CorrelationData(CoreConstants.nextIdStr())
							);
						}
					} catch (JsonProcessingException e) {
						log.error("e", e);
					}


				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.error("copySysDictByAppId", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("复制字典失败");
				}
			});


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[copySysDictByDictId] message handler end");
		} catch (RuntimeException e) {
			log.info("[copySysDictByDictId] message handler error", e);
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
