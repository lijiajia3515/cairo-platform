package io.github.lijiajia3515.cairo.auth.modules.app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserLogoffStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * 注销应用用户（根据已删除的账号）处理器
 */

@Slf4j
@Component
public class LogoffSuccessAppUserByDeletedAccountQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final SmsMsgClientApiService smsMsgClientApiService;

	public LogoffSuccessAppUserByDeletedAccountQueueHandler(ObjectMapper objectMapper,
															MongoTemplate mongoTemplate,
															SmsMsgClientApiService smsMsgClientApiService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.smsMsgClientApiService = smsMsgClientApiService;
	}

	@RabbitListener(
		queues = {"#{logoffSuccessAppUserByDeletedAccountQueue.getName()}"}
	)
	public void logoffSuccessAppUserByDeletedAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[logoff_success_app_user_by_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[logoff_success_app_user_by_deleted_account] ===> 已删除账号: AccountId: {} EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppUserMongodb.FIELD.ACCOUNT_ID).is(deletedAccountMessage.getAccountId());

			Query query = Query.query(criteria);
			List<AppUserMongodb> deleteAccountAppUserList = mongoTemplate.find(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

			List<SendPhoneNumberSmsMsgArgs> smsMsgArgsList = new ArrayList<>();

			deleteAccountAppUserList.forEach(appUser -> {
				try {
					Query userQuery = Query.query(Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(appUser.getAppId())
						.and(AppUserMongodb.FIELD.USER_ID).is(appUser.getUserId())
					);

					Update userUpdate = Update.update(AppUserMongodb.FIELD.ACCOUNT_ID, null);
					userUpdate.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
					userUpdate.set(AppUserMongodb.FIELD.ADMIN, false); // 注销完成，取消管理员身份
					userUpdate.currentDate(AppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME);
					userUpdate.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, appUser.getUserId());
					userUpdate.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

					UpdateResult updateResult = mongoTemplate.updateFirst(userQuery, userUpdate, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
					log.debug("updateResult: {}", updateResult);

					String smsAccount = String.format("%s(%s)",Optional.ofNullable(deletedAccountMessage.getNickname()).filter(x->!x.isBlank()).orElse("****"), appUser.getAccountId());
					String smsUser = String.format("%s(%s)", Optional.ofNullable(appUser.getNickname()).orElse("****"), appUser.getUserId());

					if (deletedAccountMessage.getPhoneNumber() != null) {
						smsMsgArgsList.add(
							SendPhoneNumberSmsMsgArgs.builder()
								.phoneNumber(deletedAccountMessage.getPhoneNumber())
								.appId(appUser.getAppId())
								.bizId(CairoAuthSmsConstants.LogoffAppUserSuccess.BIZ_ID)
								.args(new HashMap<>() {{
									put(CairoAuthSmsConstants.LogoffAppUserSuccess.PARAM_ACCOUNT, smsAccount);
									put(CairoAuthSmsConstants.LogoffAppUserSuccess.PARAM_USER, smsUser);
								}})
								.build()
						);
					}

				} catch (Exception e) {
					log.warn("注销应用用户:AppId: {} UserId: {} Nickname: {}  AccountId: {} 异常：{}",
						appUser.getAppId(),
						appUser.getUserId(),
						appUser.getNickname(),
						appUser.getAccountId(),
						e.getMessage());
				}
			});

			if (!smsMsgArgsList.isEmpty()) {
				List<SmsMsgResult> smsMsgResults = smsMsgClientApiService.sendBatchMessageByPhoneNumber(smsMsgArgsList);
				log.debug("smsMsgResp: {}", smsMsgResults);
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[logoff_success_app_user_by_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[logoff_success_app_user_by_deleted_account] message handler error", e);
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
