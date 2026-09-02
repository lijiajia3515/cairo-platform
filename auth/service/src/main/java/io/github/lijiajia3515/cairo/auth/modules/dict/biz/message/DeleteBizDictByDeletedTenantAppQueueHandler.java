package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 删除业务级字典根据删除企业应用
 */
@Slf4j
@Component
public class DeleteBizDictByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final PublicFileCommonService publicFileCommonService;

	public DeleteBizDictByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
															 MongoTemplate mongoTemplate,
															 TransactionTemplate transactionTemplate,
															 PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.publicFileCommonService = publicFileCommonService;
	}

	@RabbitListener(
		queues = {"#{deleteBizDictByDeletedTenantAppQueue.getName()}"}
	)
	public void deleteBizDictByDeletedTenantAppQueueHandler(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleteBizDictByDeletedTenantAppQueue] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[deleteBizDictByDeletedTenantAppQueue] ===> 已删除企业应用： TenantId: {} AppId: {}",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId()
			);
			Criteria bdCriteria = Criteria
				.where(BizDictMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(BizDictMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query bdQuery = Query.query(bdCriteria);
			Update update = new Update();
			update.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);

			//字典项
			Criteria bdiCriteria = Criteria
				.where(BizDictItemMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(BizDictItemMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query bdiQuery = Query.query(bdiCriteria);
			Update sdiUpdate = new Update();
			update.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
			List<String> deleteBdIcons = new ArrayList<>();
			List<String> deleteBdiIcons = new ArrayList<>();
			transactionTemplate.execute(status -> {
				try {
					mongoTemplate.updateFirst(bdQuery, update, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
					BizDictMongodb removeBizDict = mongoTemplate.findAndRemove(bdQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
					if (removeBizDict != null) {
						mongoTemplate.insert(removeBizDict, MongodbConstants.DeletedCollection.BIZ_DICT);
						if (removeBizDict.getIcon()!=null&&!removeBizDict.getIsSyncIcon()){
							deleteBdIcons.add(removeBizDict.getIcon());
						}
					} else {
						throw new ConflictBusinessException("删除业务级字典失败，业务字典不存在");
					}
					mongoTemplate.updateMulti(bdiQuery, sdiUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					List<BizDictItemMongodb> deleteBizDictItemMongodbList = mongoTemplate.findAllAndRemove(bdiQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					if (!deleteBizDictItemMongodbList.isEmpty()) {
						mongoTemplate.insert(deleteBizDictItemMongodbList, MongodbConstants.DeletedCollection.BIZ_DICT_ITEM);
						deleteBdiIcons.addAll(deleteBizDictItemMongodbList.stream().filter(x->!x.getIsSyncIcon()).map(BizDictItemMongodb::getIcon).collect(Collectors.toList()));
					}
					return removeBizDict;
				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("deleteBizDict", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除业务级字典失败");
				}
			});
			//删除业务级字典图标
			if (!deleteBdIcons.isEmpty()) {
				publicFileCommonService.deleteFile(deletedTenantAppMessage.getTenantId().concat("/").concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ICON), deleteBdIcons);
			}
			//删除业务级字典项图标
			if (!deleteBdiIcons.isEmpty()) {
				publicFileCommonService.deleteFile(deletedTenantAppMessage.getTenantId().concat("/").concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON),deleteBdiIcons);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleteBizDictByDeletedTenantAppQueue] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleteBizDictByDeletedTenantAppQueue] message handler error", e);
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
