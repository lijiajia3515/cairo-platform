package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffSuccessTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.message.SmsMsgResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sms.template.SendAccountSmsMsgArgs;
import io.github.lijiajia3515.cairo.auth.api.client.sms.message.SmsMsgClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 给用户发送注销成功消息 队列处理器
 */
@Slf4j
@Component
public class SendMsgByLogoffSuccessTenantAppUserQueueHandler {

	private final TenantCommonService tenantCommonService;
	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;

	public SendMsgByLogoffSuccessTenantAppUserQueueHandler(TenantCommonService tenantCommonService,
															   SmsMsgClientApiService smsMsgClientApiService,
															   ObjectMapper objectMapper) {
		this.tenantCommonService = tenantCommonService;
		this.smsMsgClientApiService = smsMsgClientApiService;
		this.objectMapper = objectMapper;
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
		queues = {"#{sendMsgByLogoffSuccessTenantAppUserQueue.getName()}"}
	)
	public void sendMsgByLogoffSuccessTenantAppUserQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			LogoffSuccessTenantAppUserMessage logoffSuccessTenantAppUserMessage = objectMapper.readValue(payload, LogoffSuccessTenantAppUserMessage.class);
			log.debug("[send_msg_by_logoff_success_tenant_app_user] message handler start: TenantId: {} AppId: {} UserId: {}", logoffSuccessTenantAppUserMessage.getTenantId(), logoffSuccessTenantAppUserMessage.getAppId(), logoffSuccessTenantAppUserMessage.getUserId());
			Tenant tenant = tenantCommonService.getBasicTenantMapByTenantIds(Collections.singleton(logoffSuccessTenantAppUserMessage.getTenantId())).get(logoffSuccessTenantAppUserMessage.getTenantId());


			if (logoffSuccessTenantAppUserMessage.getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_logoff_success_tenant_app_user] message handler end: TenantId: {} AppId: {} UserId: {}", logoffSuccessTenantAppUserMessage.getTenantId(), logoffSuccessTenantAppUserMessage.getAppId(), logoffSuccessTenantAppUserMessage.getUserId());
				return;
			}

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(logoffSuccessTenantAppUserMessage.getAccountId())
				.appId(logoffSuccessTenantAppUserMessage.getAppId())
				.bizId(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_ACCOUNT, logoffSuccessTenantAppUserMessage.getAccountId());
					put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_TENANT, tenant.getTenantName());
					put(CairoAuthSmsConstants.LogoffTenantAppUserSuccess.PARAM_USER, logoffSuccessTenantAppUserMessage.getUserId());
				}})
				.build()
			);

			log.debug("sendMsgResp: {}", smsMsgResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_logoff_success_tenant_app_user] message handler end: TenantId: {} AppId: {} UserId: {}", logoffSuccessTenantAppUserMessage.getTenantId(), logoffSuccessTenantAppUserMessage.getAppId(), logoffSuccessTenantAppUserMessage.getUserId());
		} catch (Exception e) {
			log.info("[send_msg_by_logoff_success_tenant_app_user] handler error", e);
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
