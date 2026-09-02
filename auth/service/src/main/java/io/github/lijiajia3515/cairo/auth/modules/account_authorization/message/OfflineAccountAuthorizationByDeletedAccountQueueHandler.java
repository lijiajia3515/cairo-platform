package io.github.lijiajia3515.cairo.auth.modules.account_authorization.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
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
 * 下线已删除的账号根据已删除的账号 处理器
 */

@Slf4j
@Component
public class OfflineAccountAuthorizationByDeletedAccountQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;
	private final AccountAuthorizationCommonService accountAuthorizationCommonService;


	public OfflineAccountAuthorizationByDeletedAccountQueueHandler(ObjectMapper objectMapper,
																   MongoTemplate mongoTemplate,
																   AccountAuthorizationCommonService accountAuthorizationCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.accountAuthorizationCommonService = accountAuthorizationCommonService;
	}

	@RabbitListener(
		queues = {"#{offlineAccountAuthorizationByDeletedAccountQueue.getName()}"}
	)
	public void offlineAccountAuthorizationByDeletedAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[offline_account_authorization_by_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[offline_account_authorization_by_deleted_account] ===> 已删除账号: AccountId: {} EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Query query = Query.query(
				Criteria
					.where(AccountAuthorizationMongodb.FIELD.ACCOUNT_ID).is(deletedAccountMessage.getAccountId())
					.and(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue())
			);
			List<AccountAuthorizationMongodb> list = mongoTemplate.find(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
			list.forEach(authorization -> {
				try {
					Query tokenQuery = Query.query(
						Criteria
							.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).in(authorization.getTokenId())
					);
					Update update = new Update();
					update.set(AccountAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
					update.currentDate(AccountAuthorizationMongodb.FIELD.LOGOUT_TIME);
					update.set(AccountAuthorizationMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
					mongoTemplate.updateFirst(tokenQuery, update, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
					accountAuthorizationCommonService.removeAuthorizationCache(authorization.getAccessToken().getTokenValue());
				} catch (Exception e) {
					log.warn("offlineAccountAuthorization accountId: {} accessToken: {} error", deletedAccountMessage.getAccountId(), authorization.getAccessToken().getTokenValue(), e);
				}
			});


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[offline_account_authorization_by_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[offline_account_authorization_by_deleted_account] message handler error", e);
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
