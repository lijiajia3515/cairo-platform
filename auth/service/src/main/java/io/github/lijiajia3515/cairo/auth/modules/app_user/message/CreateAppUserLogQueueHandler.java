package io.github.lijiajia3515.cairo.auth.modules.app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * 创建应用用户日志 队列 处理器
 */
@Slf4j
@Component
public class CreateAppUserLogQueueHandler {
	private final ObjectMapper objectMapper;

	public CreateAppUserLogQueueHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{createAppUserLogQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[create_app_user_log] message handler start");
			CreatedAppUserMessage createdAppUserMessage = objectMapper.readValue(payload, CreatedAppUserMessage.class);
			log.info("[create_app_user_log] ===> 创建应用用户：AppId: {} AppUserId: {} Nickname: {} AccountId: {}",
				createdAppUserMessage.getAppId(),
				createdAppUserMessage.getUserId(),
				createdAppUserMessage.getNickname(),
				createdAppUserMessage.getAccountId()
			);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[create_app_user_log] message handler end");
		} catch (RuntimeException e) {
			log.info("[create_app_user_log] message handler error", e);
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
