package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.DeletedAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 下线会话根据已删除的应用用户 队列处理器
 */
@Slf4j
@Component
public class OfflineAppUserAuthorizationByDeletedAppUserQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final AppUserAuthorizationCommonService appUserAuthorizationCommonService;


	public OfflineAppUserAuthorizationByDeletedAppUserQueueHandler(ObjectMapper objectMapper,
																		   MongoTemplate mongoTemplate,
																		   AppUserAuthorizationCommonService appUserAuthorizationCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.appUserAuthorizationCommonService = appUserAuthorizationCommonService;
	}

	@RabbitListener(
		queues = {"#{offlineAppUserAuthorizationByDeletedAppUserQueue.getName()}"}
	)
	public void offlineAppUserAuthorizationByDeletedAppUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[offline_app_user_authorization_by_deleted_app_user] message handler start");
			DeletedAppUserMessage deletedAppUserMessage = objectMapper.readValue(payload, DeletedAppUserMessage.class);
			log.info("[offline_app_user_authorization_by_deleted_app_user] ===> 已删除的用户： AppId: {} UserId: {} Nickname: {}, EventAppUserId: {} EventTime: {} ",
				deletedAppUserMessage.getAppId(),
				deletedAppUserMessage.getUserId(),
				deletedAppUserMessage.getNickname(),
				deletedAppUserMessage.getEventAppUserId(),
				deletedAppUserMessage.getEventTime()
			);

			Query query = Query.query(
				Criteria
					.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(deletedAppUserMessage.getAppId())
					.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(deletedAppUserMessage.getUserId())
					.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue())
			);
			List<AppUserAuthorizationMongodb> list = mongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
			list.forEach(authorization -> {
				try {
					Query tokenQuery = Query.query(
						Criteria
							.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).in(authorization.getTokenId())
					);
					Update update = new Update();
					update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AppUserAuthorizationStatus.BLACKLIST.getStatusValue());
					update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
					update.set(AppUserAuthorizationMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
					mongoTemplate.updateFirst(tokenQuery, update, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
					appUserAuthorizationCommonService.removeAuthorizationCache(deletedAppUserMessage.getAppId(), deletedAppUserMessage.getUserId(), authorization.getAccessToken().getTokenValue());
				} catch (Exception e) {
					log.warn("offline_app_user_authorization_by_deleted_app_user appId: {} userId: {} accessToken: {} error", deletedAppUserMessage.getAppId(), deletedAppUserMessage.getUserId(), authorization.getAccessToken().getTokenValue(), e);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[offline_app_user_authorization_by_deleted_app_user] message handler end");
		} catch (RuntimeException e) {
			log.info("[offline_app_user_authorization_by_deleted_app_user] message handler error", e);
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
