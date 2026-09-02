package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 同步业务级字典根据创建企业应用
 */
@Slf4j
@Component
public class SyncBizDictByCreatedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final PublicFileCommonService publicFileCommonService;

	public SyncBizDictByCreatedTenantAppQueueHandler(ObjectMapper objectMapper,
														   MongoTemplate mongoTemplate,
														   TransactionTemplate transactionTemplate,
														   PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.publicFileCommonService = publicFileCommonService;
	}

	@RabbitListener(
		queues = {"#{syncBizDictByCreatedTenantAppQueue.getName()}"}
	)
	public void syncBizDictByCreatedTenantAppQueueHandler(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[syncBizDictByCreatedTenantAppQueue] message handler start");
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.info("[syncBizDictByCreatedTenantAppQueue] ===> 已创建企业应用： TenantId: {} AppId: {}",
				createdTenantAppMessage.getTenantId(),
				createdTenantAppMessage.getAppId()
			);
			//系统级字典业务模版
			Criteria sdCriteria = Criteria
				.where(SysDictMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
				.and(SysDictMongodb.FIELD.DICT_TYPE).is("biz_template");
			Query sdQuery = Query.query(sdCriteria);
			List<SysDictMongodb> sysDictMongodbs = mongoTemplate.find(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
			List<BizDictMongodb> bdms = sysDictMongodbs.stream().map(sdMongodb -> {
				String icon = null;
				try {
					if (sdMongodb.getIcon() != null && !sdMongodb.getIcon().isBlank()) {
						String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
						MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdMongodb.getIcon(),
							fileName.concat(FilesUtil.getType(sdMongodb.getIcon())));
						if (multipartFile != null) {
							List<String> urls = publicFileCommonService.uploadFile(createdTenantAppMessage.getTenantId()
									.concat("/")
									.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ICON)
									.concat("/")
									.concat(fileName)
									.concat(FilesUtil.getType((FilesUtil.getType(sdMongodb.getIcon())))),
								multipartFile);
							if (urls.size() > 2) icon = urls.get(2);
						}
					}
				} catch (Exception e) {
					log.info("[syncBizDictByCreatedTenantApp bd uploadPublicFile] error", e);
				}
				return BizDictMongodb.builder()
					.tenantId(createdTenantAppMessage.getTenantId())
					.dictId(sdMongodb.getDictId())
					.dictName(sdMongodb.getDictName())
					.appId(createdTenantAppMessage.getAppId())
					.icon(icon)
					.isCreateItem(sdMongodb.getIsCreateItem())
					.enabled(sdMongodb.getEnabled())
//					.syncVersion(sdMongodb.getVersion())
//					.reductionVersion(sdMongodb.getVersion())
					.reductionDictName(sdMongodb.getDictName())
					.reductionIcon(sdMongodb.getIcon())
					.leftNo(sdMongodb.getLeftNo())
					.rightNo(sdMongodb.getRightNo())
					.isSyncIcon(true)
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(null)
						.updateUserId(null)
						.build())
					.build();
			}).collect(Collectors.toList());
			transactionTemplate.executeWithoutResult(status -> mongoTemplate.insert(bdms, MongodbConstants.Collection.BIZ_DICT));
			//系统级字典项
			Criteria sdiCriteria = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
				.and(SysDictItemMongodb.FIELD.DICT_ID).in(sysDictMongodbs.stream().map(SysDictMongodb::getDictId).collect(Collectors.toList()));
			Query sdiQuery = Query.query(sdiCriteria);
			List<SysDictItemMongodb> sdiMongodbs = mongoTemplate.find(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);

			//新增业务级字典项
			List<BizDictItemMongodb> bdims = sdiMongodbs.stream().map(sdi -> {
				String icon = null;
				try {
					if (sdi.getIcon() != null && !sdi.getIcon().isBlank()) {
						String fileName = CoreConstants.SNOWFLAKE.nextIdStr();
						MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdi.getIcon(),
							fileName.concat(FilesUtil.getType(sdi.getIcon())));
						if (multipartFile != null) {
							List<String> urls = publicFileCommonService.uploadFile(createdTenantAppMessage.getTenantId()
									.concat("/")
									.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON)
									.concat("/")
									.concat(fileName)
									.concat(FilesUtil.getType((FilesUtil.getType(sdi.getIcon())))),
								multipartFile);
							if (urls.size() > 2) icon = urls.get(2);
						}
					}
				} catch (Exception e) {
					log.info("[syncBizDictByCreatedTenantApp bdi uploadPublicFile] error", e);
				}
				return BizDictItemMongodb.builder()
					.tenantId(createdTenantAppMessage.getTenantId())
					.appId(createdTenantAppMessage.getAppId())
					.dictId(sdi.getDictId())
					.parentItemId(sdi.getParentItemId())
					.itemId(sdi.getItemId())
					.itemName(sdi.getItemName())
					.editable(sdi.getEditable())
					.depth(sdi.getDepth())
					.leftNo(sdi.getLeftNo())
					.rightNo(sdi.getRightNo())
					.remark(sdi.getRemark())
					.icon(icon)
					.enabled(sdi.getEnabled())
					.isSync(true)
					.isSyncIcon(true)
//					.syncVersion(sdi.getVersion())
//					.reductionVersion(sdi.getVersion())
					.reductionRemark(sdi.getRemark())
					.reductionIcon(sdi.getIcon())
					.reductionItemName(sdi.getItemName())
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(null)
						.updateUserId(null)
						.build())
					.build();
				}).collect(Collectors.toList());
			transactionTemplate.executeWithoutResult(status -> mongoTemplate.insert(bdims, MongodbConstants.Collection.BIZ_DICT_ITEM));
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[syncBizDictByCreatedTenantAppQueue] message handler end");
		} catch (RuntimeException e) {
			log.info("[syncBizDictByCreatedTenantAppQueue] message handler error", e);
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
