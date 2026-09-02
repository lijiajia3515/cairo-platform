package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffSuccessAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * 下线会话根据根据已注销的项目 队列处理器
 */
@Slf4j
@Component
public class OfflineAppUserAuthorizationByLogoffSuccessAppUserQueueHandler {

	private final MongoTemplate mongoTemplate;
	private final AppUserAuthorizationCommonService appUserAuthorizationCommonService;
	private final ObjectMapper objectMapper;

	public OfflineAppUserAuthorizationByLogoffSuccessAppUserQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																				 AppUserAuthorizationCommonService appUserAuthorizationCommonService,
																				 ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.appUserAuthorizationCommonService = appUserAuthorizationCommonService;
		this.objectMapper = objectMapper;
	}

	/**
	 * @param headers
	 * @param payload
	 * @param message
	 * @param channel
	 * @throws java.io.IOException
	 */
	@RabbitListener(
		queues = {"#{offlineAppUserAuthorizationByLogoffSuccessAppUserQueue.getName()}"}
	)
	public void offlineAppUserAuthorizationByLogoffSuccessAppUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffSuccessAppUserMessage logoffSuccessAppUserMessage = objectMapper.readValue(payload, LogoffSuccessAppUserMessage.class);
			log.debug("[offline_app_user_authorization_by_logoff_success_app_user] message handler start:  AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
			if (logoffSuccessAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[offline_app_user_authorization_by_logoff_success_app_user] message handler end: AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
				return;
			}

			Query query = Query.query(
				Criteria
					.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(logoffSuccessAppUserMessage.getAppId())
					.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(logoffSuccessAppUserMessage.getUserId())
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
					appUserAuthorizationCommonService.removeAuthorizationCache(logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), authorization.getAccessToken().getTokenValue());
				} catch (Exception e) {
					log.warn("offlineAppUserAuthorization appId: {} userId: {} accessToken: {} error", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), authorization.getAccessToken().getTokenValue(), e);
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[offline_app_user_authorization_by_logoff_success_app_user] message handler end: AppId: {} UserId: {} AccountId: {}", logoffSuccessAppUserMessage.getAppId(), logoffSuccessAppUserMessage.getUserId(), logoffSuccessAppUserMessage.getAccountId());
		} catch (Exception e) {
			log.info("[offline_app_user_authorization_by_logoff_success_app_user] handler error", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误，重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
	}
}
