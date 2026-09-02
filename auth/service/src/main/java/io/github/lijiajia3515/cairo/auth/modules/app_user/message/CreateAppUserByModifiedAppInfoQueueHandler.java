package io.github.lijiajia3515.cairo.auth.modules.app_user.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.app.ModifiedAppInfoMessage;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 创建应用用户根据修改应用 队列处理器
 */
@Slf4j
@Component
public class CreateAppUserByModifiedAppInfoQueueHandler {

	private final AccountCommonService accountCommonService;
	private final AppUserCommonService userCommonService;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateAppUserByModifiedAppInfoQueueHandler(AccountCommonService accountCommonService, AppUserCommonService userCommonService,
														 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														 TransactionTemplate transactionTemplate, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool) {
		this.accountCommonService = accountCommonService;
		this.userCommonService = userCommonService;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	/**
	 * 业务队列
	 *
	 * @param headers headers
	 * @param payload payload
	 * @param message message
	 * @param channel channel
	 * @throws IOException 1
	 */
	@RabbitListener(
		queues = {"#{createAppUserByModifiedAppInfoQueue.getName()}"}
	)
	public void createAppUserByModifiedAppInfoQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			ModifiedAppInfoMessage modifiedAppInfoMessage = objectMapper.readValue(payload, ModifiedAppInfoMessage.class);
			log.debug("[create_app_user_by_modified__app_info] message handler start:AppId: {} RemoveAdminAccountIds: {} NewAdminAccountIds: {} EventTime: {}",
				modifiedAppInfoMessage.getAppId(),
				modifiedAppInfoMessage.getRemoveAdminAccountIds(),
				modifiedAppInfoMessage.getNewAdminAccountIds(),
				modifiedAppInfoMessage.getEventTime()
			);

			// 移除旧账号的应用用户管理员权限
			List<String> removeAdminAccountIds = modifiedAppInfoMessage.getRemoveAdminAccountIds();
			if (removeAdminAccountIds != null && !removeAdminAccountIds.isEmpty()) {
				Criteria criteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(modifiedAppInfoMessage.getAppId())
					.and(AppUserMongodb.FIELD.ACCOUNT_ID).in(removeAdminAccountIds);
				Query query = Query.query(criteria);
				Update update = Update.update(AppUserMongodb.FIELD.ADMIN, false);
				update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
				update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateMulti(query, update, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				log.info("remove admin app_user updateResult: {}", updateResult);
			}

			List<String> newAdminAccountIds = modifiedAppInfoMessage.getNewAdminAccountIds();
			if (newAdminAccountIds != null && !newAdminAccountIds.isEmpty()) {
				// 创建新管理员应用用户
				List<Account> newAdminAccountList = accountCommonService.getAccountListByAccountIds(newAdminAccountIds);
				newAdminAccountList.forEach(account -> {
					AppUserMongodb newUser = transactionTemplate.execute(transactionStatus -> {
						try {
							Criteria userCriteria = Criteria
								.where(AppUserMongodb.FIELD.APP_ID).is(modifiedAppInfoMessage.getAppId())
								.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
							Query userQuery = Query.query(userCriteria);
							boolean exists = mongoTemplate.exists(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
							if (!exists) {
								String newUserId = userCommonService.getNewAppUserId();
								AppUserMongodb user = AppUserMongodb.builder()
									.appId(modifiedAppInfoMessage.getAppId())
									.userId(newUserId)
									.nickname(account.getNickname())
									.roleIds(Collections.emptyList())
									.departmentIds(Collections.emptyList())
									.tagIds(Collections.emptyList())
									.joinTime(modifiedAppInfoMessage.getEventTime())
									.admin(true)
									.enabled(true)
									.accountId(account.getAccountId())
									.metadata(AppUserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
									.build();
								return mongoTemplate.insert(user, MongodbConstants.Collection.APP_USER);
							} else {
								Criteria criteria = Criteria
									.where(AppUserMongodb.FIELD.APP_ID).is(modifiedAppInfoMessage.getAppId())
									.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
								Query query = Query.query(criteria);
								Update update = Update.update(AppUserMongodb.FIELD.ADMIN, true);
								update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
								update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);
								UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
								log.info("add admin app_user updateResult: {}", updateResult);
								return null;
							}
						} catch (Exception e) {
							log.warn("[create_app_user_by_modified__app_info] handler error", e);
						}
						return null;
					});

					if (newUser != null) {
						try {
							// 发送创建应用用户消息
							rabbitTemplate.convertAndSend(
								cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
								cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER, modifiedAppInfoMessage.getAppId()),
								objectMapper.writeValueAsString(
									CreatedAppUserMessage.builder()
										.appId(modifiedAppInfoMessage.getAppId())
										.userId(newUser.getUserId())
										.nickname(newUser.getNickname())
										.admin(newUser.getAdmin())
										.accountId(newUser.getAccountId())
										.eventAppUserId(null)
										.eventTime(LocalDateTime.now())
										.build()
								),
								new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
							);
						} catch (JsonProcessingException e) {
							log.warn("e", e);
						}
					}
				});
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_app_user_by_modified__app_info] handler end:  AppId: {} RemoveAdminAccountIds: {} NewAdminAccountIds: {}  EventTime: {}",
				modifiedAppInfoMessage.getAppId(),
				modifiedAppInfoMessage.getRemoveAdminAccountIds(),
				modifiedAppInfoMessage.getNewAdminAccountIds(),
				modifiedAppInfoMessage.getEventTime()
			);
		} catch (Exception e) {
			log.info("[create_app_user_by_modified__app_info] handler error", e);
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
