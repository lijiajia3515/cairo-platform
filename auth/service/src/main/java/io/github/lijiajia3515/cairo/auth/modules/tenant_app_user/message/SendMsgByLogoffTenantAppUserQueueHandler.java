package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 给用户发送注册成功消息 队列处理器
 */
@Slf4j
@Component
public class SendMsgByLogoffTenantAppUserQueueHandler {

	private final TenantCommonService tenantCommonService;
	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;
	private final MongoTemplate readMongoTemplate;

	public SendMsgByLogoffTenantAppUserQueueHandler(TenantCommonService tenantCommonService,
														SmsMsgClientApiService smsMsgClientApiService,
														ObjectMapper objectMapper,
														@Qualifier("readMongoTemplate")MongoTemplate readMongoTemplate) {
		this.tenantCommonService = tenantCommonService;
		this.smsMsgClientApiService = smsMsgClientApiService;
		this.objectMapper = objectMapper;
		this.readMongoTemplate = readMongoTemplate;
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
		queues = {"#{sendMsgByLogoffTenantAppUserQueue.getName()}"}
	)
	public void sendMsgByLogoffTenantAppUserQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffTenantAppUserMessage logoffTenantAppUserMessage = objectMapper.readValue(payload, LogoffTenantAppUserMessage.class);
			log.debug("[send_msg_by_logoff_tenant_app_user] message handler start: TenantId: {} AppId: {} AccountId: {} UserId: {}", logoffTenantAppUserMessage.getTenantId(), logoffTenantAppUserMessage.getAppId(), logoffTenantAppUserMessage.getAccountId(), logoffTenantAppUserMessage.getUserId());
			Tenant tenant = tenantCommonService.getBasicTenantMapByTenantIds(Collections.singleton(logoffTenantAppUserMessage.getTenantId())).get(logoffTenantAppUserMessage.getTenantId());

			if (logoffTenantAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_logoff_tenant_app_user] message handler end: TenantId: {} AppId: {} AccountId: {} UserId: {}", logoffTenantAppUserMessage.getTenantId(), logoffTenantAppUserMessage.getAppId(), logoffTenantAppUserMessage.getAccountId(), logoffTenantAppUserMessage.getUserId());
				return;
			}

			//账号查询
			Query accountQuery = Query.query(Criteria
				.where(AccountMongodb.FIELD.ACCOUNT_ID).is(tenant.getOwnerAccount()));
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

			String smsAccount = String.format("%s(%s)", Optional.ofNullable(account).map(AccountMongodb::getNickname).filter(x->!x.isBlank()).orElse("****"), logoffTenantAppUserMessage.getAccountId());
			String smsTenant = tenant.getTenantName();
			String smsUser = String.format("%s(%s)", Optional.of(logoffTenantAppUserMessage).map(LogoffTenantAppUserMessage::getNickname).filter(x->!x.isBlank()).orElse("****"), logoffTenantAppUserMessage.getUserId());

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(logoffTenantAppUserMessage.getAccountId())
				.appId(logoffTenantAppUserMessage.getAppId())
				.bizId(CairoAuthSmsConstants.LogoffTenantAppUser.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.LogoffTenantAppUser.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.LogoffTenantAppUser.PARAM_TENANT, smsTenant);
					put(CairoAuthSmsConstants.LogoffTenantAppUser.PARAM_USER, smsUser);
					put(CairoAuthSmsConstants.LogoffTenantAppUser.PARAM_DAY, "3天");
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_logoff_tenant_app_user] message handler end: TenantId: {} AppId: {} AccountId: {} UserId: {}", logoffTenantAppUserMessage.getTenantId(), logoffTenantAppUserMessage.getAppId(), logoffTenantAppUserMessage.getAccountId(), logoffTenantAppUserMessage.getUserId());
		} catch (Exception e) {
			log.info("[send_msg_by_logoff_tenant_app_user] handler error", e);
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
