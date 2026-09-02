package io.github.lijiajia3515.cairo.auth.modules.tenant.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthSmsConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant.CreatedTenantMessage;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * 给企业拥有着发送注册企业消息通知 队列处理器
 */
@Slf4j
@Component
public class SendMsgByCreatedTenantQueueHandler {

	private final CairoSecurityProperties cairoSecurityProperties;
	private final SmsMsgClientApiService smsMsgClientApiService;
	private final ObjectMapper objectMapper;
	private final MongoTemplate readMongoTemplate;

	public SendMsgByCreatedTenantQueueHandler(CairoSecurityProperties cairoSecurityProperties, SmsMsgClientApiService smsMsgClientApiService,
												  ObjectMapper objectMapper,@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.cairoSecurityProperties = cairoSecurityProperties;
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
		queues = {"#{sendMsgByCreatedTenantQueue.getName()}"}
	)
	public void createPortalUserQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantMessage createdTenantMessage = objectMapper.readValue(payload, CreatedTenantMessage.class);
			log.debug("[send_msg_by_created_tenant] message handler start: {}", createdTenantMessage.getTenantId());

			if (createdTenantMessage.getOwnerAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[send_msg_by_created_tenant] handler end: {}", createdTenantMessage.getTenantId());
				return;
			}
			//账号查询
			Query accountQuery = Query.query(Criteria
				.where(AccountMongodb.FIELD.ACCOUNT_ID).is(createdTenantMessage.getOwnerAccountId()));
			AccountMongodb account = readMongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			String smsAccount = String.format("%s(%s)", Optional.ofNullable(account).map(AccountMongodb::getNickname).filter(x->!x.isBlank()).orElse("****"), Optional.of(createdTenantMessage).map(CreatedTenantMessage::getOwnerAccountId).filter(x -> !x.isBlank()).orElse("****"));
			String smsTenant = String.format("%s(%s)", createdTenantMessage.getTenantName(),createdTenantMessage.getTenantId());

			SmsMsgResult smsMsgResult = smsMsgClientApiService.sendMsgByAccount(SendAccountSmsMsgArgs.builder()
				.accountId(createdTenantMessage.getOwnerAccountId())
				.appId(cairoSecurityProperties.getCairoAppId())
				.bizId(CairoAuthSmsConstants.RegisterTenantSuccess.BIZ_ID)
				.args(new HashMap<>() {{
					put(CairoAuthSmsConstants.RegisterTenantSuccess.PARAM_ACCOUNT, smsAccount);
					put(CairoAuthSmsConstants.RegisterTenantSuccess.PARAM_TENANT, smsTenant);
				}})
				.build()
			);
			log.debug("sendMsgResp: {}", smsMsgResult);


			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[send_msg_by_created_tenant] handler end: {}", createdTenantMessage.getTenantId());
		} catch (Exception e) {
			log.info("[send_msg_by_created_tenant] handler error", e);
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
