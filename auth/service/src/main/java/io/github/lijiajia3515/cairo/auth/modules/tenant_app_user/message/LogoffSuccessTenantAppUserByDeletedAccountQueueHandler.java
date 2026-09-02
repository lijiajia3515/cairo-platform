package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendPhoneNumberSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 删除企业用户（根据已删除的企业应用）处理器
 */

@Slf4j
@Component
public class LogoffSuccessTenantAppUserByDeletedAccountQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TenantCommonService tenantCommonService;
	private final SmsMsgClientApiService smsMsgClientApiService;

	public LogoffSuccessTenantAppUserByDeletedAccountQueueHandler(ObjectMapper objectMapper,
																  MongoTemplate mongoTemplate,
																  TenantCommonService tenantCommonService,
																  SmsMsgClientApiService smsMsgClientApiService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.tenantCommonService = tenantCommonService;
		this.smsMsgClientApiService = smsMsgClientApiService;
	}

	@RabbitListener(
		queues = {"#{logoffSuccessTenantAppUserByDeletedAccountQueue.getName()}"}
	)
	public void logoffSuccessTenantAppUserByDeletedAccountQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[logoff_success_tenant_app_user_by_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[logoff_success_tenant_app_user_by_deleted_account] ===> 已删除账号: AccountId: {} EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Criteria criteria = Criteria.where(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(deletedAccountMessage.getAccountId());

			Query query = Query.query(criteria);
			List<TenantAppUserMongodb> deleteAccountUserList = mongoTemplate.find(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

			Set<String> tenantIds = deleteAccountUserList.stream().map(TenantAppUserMongodb::getTenantId).collect(Collectors.toSet());
			Map<String, Tenant> tenantMap = Optional.of(tenantIds)
				.filter(x -> !x.isEmpty())
				.map(tenantCommonService::getBasicTenantMapByTenantIds)
				.orElse(Collections.emptyMap());

			List<SendPhoneNumberSmsMsgArgs> smsMsgArgsList = new ArrayList<>();

			deleteAccountUserList.forEach(user -> {
				try {
					Query userQuery = Query.query(Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(user.getTenantId())
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(user.getAppId())
						.and(TenantAppUserMongodb.FIELD.USER_ID).is(user.getUserId())
					);

					Update userUpdate = Update.update(TenantAppUserMongodb.FIELD.ACCOUNT_ID, null);
					userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
					userUpdate.set(TenantAppUserMongodb.FIELD.ADMIN, false); // 注销完成取消管理员身份
					userUpdate.currentDate(TenantAppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME);
					userUpdate.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, user.getUserId());
					userUpdate.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);


					UpdateResult updateResult = mongoTemplate.updateFirst(userQuery, userUpdate, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
					log.debug("updateResult: {}", updateResult);

					if (deletedAccountMessage.getPhoneNumber() != null) {
						String smsAccount = String.format("%s(%s)", (deletedAccountMessage.getNickname() != null ? deletedAccountMessage.getNickname() : "****"), deletedAccountMessage.getAccountId());
						String smsTenant = Optional.ofNullable(tenantMap.get(user.getTenantId())).map(Tenant::getTenantName).orElse(user.getTenantId());
						String smsUser = String.format("%s(%s)", (user.getNickname() != null ? user.getNickname() : "****"), user.getUserId());
						smsMsgArgsList.add(
							SendPhoneNumberSmsMsgArgs.builder()
								.phoneNumber(deletedAccountMessage.getPhoneNumber())
								.appId(user.getAppId())
								.bizId(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.BIZ_ID)
								.args(new HashMap<>() {{
									put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_ACCOUNT, smsAccount);
									put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_TENANT, smsTenant);
									put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_USER, smsUser);
								}})
								.build()
						);
					}


				} catch (Exception e) {
					log.warn("注销企业用户: TenantId: {} AppId: {} UserId: {} Nickname: {}  AccountId: {} 异常：{}",
						user.getTenantId(),
						user.getAppId(),
						user.getUserId(),
						user.getNickname(),
						user.getAccountId(),
						e.getMessage());
				}
			});

			if (!smsMsgArgsList.isEmpty()) {
				List<SmsMsgResult> smsMsgResults = smsMsgClientApiService.sendBatchMessageByPhoneNumber(smsMsgArgsList);
				log.debug("smsMsgResp: {}", smsMsgResults);
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[logoff_success_tenant_app_user_by_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[logoff_success_tenant_app_user_by_deleted_account] message handler error", e);
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
