package io.github.lijiajia3515.cairo.auth.modules.dict.sys.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.DeleteSysDictMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 删除系统级字典
 */
@Slf4j
@Component
public class DeleteSysDictByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final PublicFileCommonService publicFileCommonService;

	public DeleteSysDictByDeletedAppQueueHandler(ObjectMapper objectMapper,
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
		queues = {"#{deleteSysDictByDeleteAppQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleteSysDictByDeleteApp] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[deleteSysDictByDeleteApp] ===> 已删除应用： AppId: {} EventCairoUserId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventCairoUserId(),
				deletedAppMessage.getEventTime()
			);
			Criteria sdCriteria = Criteria
				.where(SysDictMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());
			Query sdQuery = Query.query(sdCriteria);
			Update update = new Update();
			update.currentDate(SysDictMongodb.FIELD.METADATA.UPDATE_TIME);;

			// 字典项
			Criteria sdiCriteria = Criteria
				.where(SysDictItemMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());

			Query sdiQuery = Query.query(sdiCriteria);
			Update sdiUpdate = new Update();
			sdiUpdate.currentDate(SysDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
			List<SysDictMongodb> sdms = new ArrayList<>();
			List<String> deleteSdIcons = new ArrayList<>();
			List<String> deleteSdiIcons = new ArrayList<>();
			transactionTemplate.execute(status -> {
				try {
					mongoTemplate.updateMulti(sdQuery, update, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
					List<SysDictMongodb> sysDictMongodbs = mongoTemplate.findAllAndRemove(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);

					mongoTemplate.updateMulti(sdiQuery, sdiUpdate, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					List<SysDictItemMongodb> deleteSysDictItemMongodbList = mongoTemplate.findAllAndRemove(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
					if (!sysDictMongodbs.isEmpty()) {
						mongoTemplate.insert(sysDictMongodbs, MongodbConstants.DeletedCollection.SYS_DICT);
						sdms.addAll(sysDictMongodbs);
						deleteSdIcons.addAll(sysDictMongodbs.stream().map(SysDictMongodb::getIcon).collect(Collectors.toList()));
					}
					if (!deleteSysDictItemMongodbList.isEmpty()) {
						mongoTemplate.insert(deleteSysDictItemMongodbList, MongodbConstants.DeletedCollection.SYS_DICT_ITEM);
						deleteSdiIcons.addAll(deleteSysDictItemMongodbList.stream().map(SysDictItemMongodb::getIcon).collect(Collectors.toList()));
					}
					return deleteSysDictItemMongodbList;
				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("deleteSysDict", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除系统级字典失败");
				}
			});
			if (!sdms.isEmpty()) {
				// 发送删除系统级字典完成消息
				sdms.forEach(sd->{
					try {
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_SYS_DICT, sd.getAppId()),
							objectMapper.writeValueAsString(DeleteSysDictMessage.builder()
								.eventCairoUserId(deletedAppMessage.getEventCairoUserId())
								.eventTime(LocalDateTime.now())
								.appId(sd.getAppId())
								.dictId(sd.getDictId())
								.build()
							),
							new CorrelationData(CoreConstants.nextIdStr())
						);
					} catch (JsonProcessingException e) {
						log.error("e",e);
					}
				});
			}
			if (!deleteSdIcons.isEmpty()){
				//删除字典图标
				publicFileCommonService.deleteFile(deletedAppMessage.getAppId().concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ICON), deleteSdIcons);
			}
			if (!deleteSdiIcons.isEmpty()){
				//删除字典项图标
				publicFileCommonService.deleteFile(deletedAppMessage.getAppId().concat("/").concat(FileKeyPrefixConstants.Collection.SYS_DICT_ITEM_ICON), deleteSdiIcons);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleteSysDictByDeleteApp] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleteSysDictByDeleteApp] message handler error", e);
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
