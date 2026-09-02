package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.DeleteSysDictMessage;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 删除业务字典队列处理
 */
@Slf4j
@Component
public class DeleteBizDictByDeletedSysDictQueueHandler {

	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final PublicFileCommonService publicFileCommonService;

	public DeleteBizDictByDeletedSysDictQueueHandler(ObjectMapper objectMapper,
																	MongoTemplate mongoTemplate,
																	TransactionTemplate transactionTemplate,
																	PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.publicFileCommonService = publicFileCommonService;
	}


	@RabbitListener(
		queues = {"#{deleteBizDictByDeletedSysDictQueue.getName()}"}
	)
	public void deleteBizDictQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleteBizDictQueueHandle] message handler start");
			DeleteSysDictMessage deleteSysDictMessage = objectMapper.readValue(payload, DeleteSysDictMessage.class);
			log.info("[deleteBizDictQueueHandle] ===> 已删除系统字典：  AppId: {} dictId {}",
				deleteSysDictMessage.getAppId(),
				deleteSysDictMessage.getDictId()
			);
			Criteria bdCriteria = Criteria
				.where(BizDictMongodb.FIELD.APP_ID).is(deleteSysDictMessage.getAppId())
				.and(BizDictMongodb.FIELD.DICT_ID).is(deleteSysDictMessage.getDictId());

			Query bdQuery = Query.query(bdCriteria);
			Update update = Update.update(BizDictMongodb.FIELD.METADATA.UPDATE_USER_ID, deleteSysDictMessage.getEventCairoUserId());
			update.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);

			//字典项
			Criteria bdiCriteria = Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(deleteSysDictMessage.getAppId())
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(deleteSysDictMessage.getDictId());

			Query bdiQuery = Query.query(bdiCriteria);
			Update sdiUpdate = Update.update(BizDictItemMongodb.FIELD.METADATA.UPDATE_USER_ID, deleteSysDictMessage.getEventCairoUserId());
			update.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);

			transactionTemplate.executeWithoutResult(status -> {
				try {
					mongoTemplate.updateMulti(bdQuery, update, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
					List<BizDictMongodb> removeBizDict = mongoTemplate.findAllAndRemove(bdQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
					if (!removeBizDict.isEmpty()) {
						Collection<BizDictMongodb> removeBds = mongoTemplate.insert(removeBizDict, MongodbConstants.DeletedCollection.BIZ_DICT);
						removeBds.forEach(bd->{
							if (bd.getIcon()!=null&&!bd.getIcon().isBlank()) {
								publicFileCommonService.deleteFile(bd.getTenantId().concat("/").concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ICON), Collections.singletonList(bd.getIcon()));
							}
						});
					} else {
						throw new ConflictBusinessException("删除业务级字典失败，业务字典不存在");
					}
					mongoTemplate.updateMulti(bdiQuery, sdiUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					List<BizDictItemMongodb> deleteBizDictItemMongodbList = mongoTemplate.findAllAndRemove(bdiQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
					if (!deleteBizDictItemMongodbList.isEmpty()) {
						Collection<BizDictItemMongodb> removeBdis = mongoTemplate.insert(deleteBizDictItemMongodbList, MongodbConstants.DeletedCollection.BIZ_DICT_ITEM);
						removeBdis.forEach(bdi->{
							if (bdi.getIcon()!=null&&!bdi.getIcon().isBlank()) {
								publicFileCommonService.deleteFile(bdi.getTenantId().concat("/").concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON), Collections.singletonList(bdi.getIcon()));
							}
						});
					}

				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("deleteBizDict", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除业务级字典失败");
				}
			});
			// 消费成功！
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleteBizDictQueueHandle] message handler end");
		} catch (Exception e) {
			log.info("[deleteBizDictQueueHandle] error: ", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
		log.info("[deleteBizDictQueueHandle] end");
	}
}
