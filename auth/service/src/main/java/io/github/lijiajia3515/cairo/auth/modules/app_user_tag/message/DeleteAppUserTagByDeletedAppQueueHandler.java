package io.github.lijiajia3515.cairo.auth.modules.app_user_tag.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserTagMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
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
import java.util.List;
import java.util.Map;


/**
 * 删除应用级用户标签根据已删除的应用 队列 处理器
 */
@Slf4j
@Component
public class DeleteAppUserTagByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;

	public DeleteAppUserTagByDeletedAppQueueHandler(ObjectMapper objectMapper,
                                                    MongoTemplate mongoTemplate,
                                                    TransactionTemplate transactionTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAppUserTagByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_user_tag_by_deleted_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_app_user_tag_by_deleted_app] ===> 已删除企业应用： AppId: {} EventAppUserId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventCairoUserId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppUserTagMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<AppUserTagMongodb> deletedAppUserTagMongodbList = mongoTemplate.find(query, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
			deletedAppUserTagMongodbList.forEach(userTagMongodb -> {
				try {
					AppUserTagMongodb deletedUserTag = transactionTemplate.execute(status -> {
						try {
							Criteria updateCriteria = Criteria
								.where(AppUserTagMongodb.FIELD.APP_ID).is(userTagMongodb.getAppId())
								.and(AppUserTagMongodb.FIELD.TAG_ID).is(userTagMongodb.getTagId());
							Query updateQuery = Query.query(updateCriteria);

							Update update = new Update();
							update.set(AppUserTagMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
							update.currentDate(AppUserTagMongodb.FIELD.METADATA.UPDATE_TIME);

							UpdateResult updateResult = mongoTemplate.updateFirst(updateQuery, update, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);
							AppUserTagMongodb deletedAppUserTagMongodb = mongoTemplate.findAndRemove(updateQuery, AppUserTagMongodb.class, MongodbConstants.Collection.APP_USER_TAG);

							if (deletedAppUserTagMongodb != null) {
								mongoTemplate.insert(deletedAppUserTagMongodb, MongodbConstants.DeletedCollection.APP_USER_TAG);
								log.debug("应用级用户标签删除成功: AppId: {} TagId: {} ",
									userTagMongodb.getAppId(),
									userTagMongodb.getTagId()
								);
							}
							return deletedAppUserTagMongodb;
						} catch (Exception e) {
							log.info("删除应用级用户标签失败", e);
							status.setRollbackOnly();
							return null;
						}
					});
				} catch (Exception e) {
					log.debug("应用级用户标签删除失败:AppId: {} TagId: {}  异常： {}",
						userTagMongodb.getAppId(),
						userTagMongodb.getTagId(),
						e.getMessage()
					);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_app_user_tag_by_deleted_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_user_tag_by_deleted_app] message handler error", e);
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
