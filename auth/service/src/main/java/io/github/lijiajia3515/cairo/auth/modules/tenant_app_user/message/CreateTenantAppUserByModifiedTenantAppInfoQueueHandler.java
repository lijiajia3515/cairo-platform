package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.ModifiedTenantAppInfoMessage;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
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
 * 创建用户根据修改企业应用 队列处理器
 */
@Slf4j
@Component
public class CreateTenantAppUserByModifiedTenantAppInfoQueueHandler {

	private final AccountCommonService accountCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateTenantAppUserByModifiedTenantAppInfoQueueHandler(AccountCommonService accountCommonService, TenantAppUserCommonService tenantAppUserCommonService,
																  @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																  TransactionTemplate transactionTemplate, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool) {
		this.accountCommonService = accountCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
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
		queues = {"#{createTenantAppUserByModifiedTenantAppInfoQueue.getName()}"}
	)
	public void createTenantAppUserByModifiedTenantAppInfoQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			ModifiedTenantAppInfoMessage modifiedTenantAppInfoMessage = objectMapper.readValue(payload, ModifiedTenantAppInfoMessage.class);
			log.debug("[create_tenant_app_user_by_modified_tenant_app_info] message handler start: TenantId: {} AppId: {} RemoveAdminAccountIds: {} NewAdminAccountIds: {} EventAccountId: {} EventTime: {}",
				modifiedTenantAppInfoMessage.getTenantId(),
				modifiedTenantAppInfoMessage.getAppId(),
				modifiedTenantAppInfoMessage.getRemoveAdminAccountIds(),
				modifiedTenantAppInfoMessage.getNewAdminAccountIds(),
				modifiedTenantAppInfoMessage.getEventAccountId(),
				modifiedTenantAppInfoMessage.getEventTime()
			);

			// 移除旧账号的企业应用用户管理员权限
			List<String> removeAdminAccountIds = modifiedTenantAppInfoMessage.getRemoveAdminAccountIds();
			if (removeAdminAccountIds != null && !removeAdminAccountIds.isEmpty()) {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(modifiedTenantAppInfoMessage.getTenantId())
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(modifiedTenantAppInfoMessage.getAppId())
					.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).in(removeAdminAccountIds);
				Query query = Query.query(criteria);
				Update update = Update.update(TenantAppUserMongodb.FIELD.ADMIN, false);
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateMulti(query, update, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				log.info("remove admin user updateResult: {}", updateResult);
			}

			List<String> newAdminAccountIds = modifiedTenantAppInfoMessage.getNewAdminAccountIds();
			if (newAdminAccountIds != null && !newAdminAccountIds.isEmpty()) {
				// 创建新管理员用户
				List<Account> newAdminAccountList = accountCommonService.getAccountListByAccountIds(newAdminAccountIds);
				newAdminAccountList.forEach(account -> {
					TenantAppUserMongodb newUser = transactionTemplate.execute(transactionStatus -> {
						try {
							Criteria userCriteria = Criteria
								.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(modifiedTenantAppInfoMessage.getTenantId())
								.and(TenantAppUserMongodb.FIELD.APP_ID).is(modifiedTenantAppInfoMessage.getAppId())
								.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
							Query userQuery = Query.query(userCriteria);
							boolean exists = mongoTemplate.exists(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
							if (!exists) {
								String newUserId = tenantAppUserCommonService.getNewUserId();
								TenantAppUserMongodb user = TenantAppUserMongodb.builder()
									.tenantId(modifiedTenantAppInfoMessage.getTenantId())
									.appId(modifiedTenantAppInfoMessage.getAppId())
									.userId(newUserId)
									.nickname(account.getNickname())
									.roleIds(Collections.emptyList())
									.departmentIds(Collections.emptyList())
									.tagIds(Collections.emptyList())
									.admin(true)
									.enabled(true)
									.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
									.joinTime(modifiedTenantAppInfoMessage.getEventTime())
									.accountId(account.getAccountId())
									.metadata(TenantAppUserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
									.build();
								return mongoTemplate.insert(user, MongodbConstants.Collection.TENANT_APP_USER);
							} else {
								Criteria criteria = Criteria
									.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(modifiedTenantAppInfoMessage.getTenantId())
									.and(TenantAppUserMongodb.FIELD.APP_ID).is(modifiedTenantAppInfoMessage.getAppId())
									.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
								Query query = Query.query(criteria);
								Update update = Update.update(TenantAppUserMongodb.FIELD.ADMIN, true);
								update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
								update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);
								UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
								log.info("add admin user updateResult: {}", updateResult);
								return null;
							}
						} catch (Exception e) {
							log.warn("[create_tenant_app_user_by_modified_tenant_app_info] handler error", e);
						}
						return null;
					});

					if (newUser != null) {
						try {
							// 发送创建用户消息
							rabbitTemplate.convertAndSend(
								cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
								cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, modifiedTenantAppInfoMessage.getTenantId(), modifiedTenantAppInfoMessage.getAppId()),
								objectMapper.writeValueAsString(
									CreatedTenantAppUserMessage.builder()
										.tenantId(modifiedTenantAppInfoMessage.getTenantId())
										.appId(modifiedTenantAppInfoMessage.getAppId())
										.userId(newUser.getUserId())
										.nickname(newUser.getNickname())
										.admin(newUser.getAdmin())
										.accountId(newUser.getAccountId())
										.eventUserId(null)
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
			log.debug("[create_tenant_app_user_by_modified_tenant_app_info] handler end: TenantId: {} AppId: {} RemoveAdminAccountIds: {} NewAdminAccountIds: {} EventAccountId: {} EventTime: {}",
				modifiedTenantAppInfoMessage.getTenantId(),
				modifiedTenantAppInfoMessage.getAppId(),
				modifiedTenantAppInfoMessage.getRemoveAdminAccountIds(),
				modifiedTenantAppInfoMessage.getNewAdminAccountIds(),
				modifiedTenantAppInfoMessage.getEventAccountId(),
				modifiedTenantAppInfoMessage.getEventTime()
			);
		} catch (Exception e) {
			log.info("[create_tenant_app_user_by_modified_tenant_app_info] handler error", e);
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
