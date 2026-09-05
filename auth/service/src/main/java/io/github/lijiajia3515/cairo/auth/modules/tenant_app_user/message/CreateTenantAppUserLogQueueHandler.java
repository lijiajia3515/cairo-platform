package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * 创建用户日志 队列 处理器
 */
@Slf4j
@Component
public class CreateTenantAppUserLogQueueHandler {
	private final ObjectMapper objectMapper;

	public CreateTenantAppUserLogQueueHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{createTenantAppUserLogQueue.getName()}"}
	)
	public void createTenantAppUserLogQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[create_tenant_app_user_log] message handler start");
			CreatedTenantAppUserMessage createdTenantAppUserMessage = objectMapper.readValue(payload, CreatedTenantAppUserMessage.class);
			log.info("[create_tenant_app_user_log] ===> 创建企业应用级用户： TenantId: {} AppId: {} UserId: {} Nickname: {} AccountId: {}",
				createdTenantAppUserMessage.getTenantId(),
				createdTenantAppUserMessage.getAppId(),
				createdTenantAppUserMessage.getUserId(),
				createdTenantAppUserMessage.getNickname(),
				createdTenantAppUserMessage.getAccountId()
			);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[create_tenant_app_user_log] message handler end");
		} catch (RuntimeException e) {
			log.info("[create_tenant_app_user_log] message handler error", e);
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
