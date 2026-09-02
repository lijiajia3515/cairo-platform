package io.github.lijiajia3515.cairo.auth.modules.biz_log.client_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.ClientBizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogClientMongodb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


@Slf4j
@Component
public class StoreClientBizLogQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public StoreClientBizLogQueueHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{storeClientBizLogQueue.getName()}"}
	)
	public void storeClientBizLogQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			ClientBizLog bizLog = objectMapper.readValue(payload, ClientBizLog.class);
			log.debug("[store_client_biz_log] message handler logId: {} start", bizLog.getLogId());
			// log.info("[store_client_biz_log]: {}", payload);
			BizLogClientMongodb bizLogMongodb = BizLogClientMongodb.builder()
				.logId(bizLog.getLogId())
				.appId(bizLog.getAppId())
				.clientId(bizLog.getClientId())
				.clientTokenId(bizLog.getClientTokenId())
				.bizId(bizLog.getBizId())
				.scope(bizLog.getScope())
				.params(bizLog.getParams())
				.success(bizLog.isSuccess())
				.errorMessage(bizLog.getErrorMessage())
				.ip(bizLog.getIp())
				.startTime(bizLog.getStartTime())
				.endTime(bizLog.getEndTime())
				.mills(bizLog.getMills())
				.metadata(ClientMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(bizLogMongodb, MongodbConstants.Collection.BIZ_LOG_CLIENT);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[store_client_biz_log] message handler logId: {} end", bizLog.getLogId());
		} catch (Exception e) {
			log.info("[store_client_biz_log] error: ", e);
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
