package io.github.lijiajia3515.cairo.auth.modules.biz_log.tenant_app_biz_log.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.TenantAppBizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantAppMongodb;
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
public class StoreTenantAppBizLogQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public StoreTenantAppBizLogQueueHandler(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{storeTenantAppBizLogQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			TenantAppBizLog bizLog = objectMapper.readValue(payload, TenantAppBizLog.class);
			log.debug("[store_tenant_app_user_biz_log] message handler logId: {} start", bizLog.getLogId());
			BizLogTenantAppMongodb bizLogMongodb = BizLogTenantAppMongodb.builder()
				.logId(bizLog.getLogId())
				.tenantId(bizLog.getTenantId())
				.appId(bizLog.getAppId())
				.endpointId(bizLog.getEndpointId())
				.clientId(bizLog.getClientId())
				.userId(bizLog.getUserId())
				.tokenId(bizLog.getTokenId())
				.bizId(bizLog.getBizId())
				.scope(bizLog.getScope())
				.params(bizLog.getParams())
				.success(bizLog.isSuccess())
				.errorMessage(bizLog.getErrorMessage())
				.ip(bizLog.getIp())
				.startTime(bizLog.getStartTime())
				.endTime(bizLog.getEndTime())
				.mills(bizLog.getMills())
				.metadata(TenantAppUserMetadataMongodb.builder().build())
				.build();
			mongoTemplate.insert(bizLogMongodb, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[store_tenant_app_user_biz_log] message handler logId: {} end", bizLog.getLogId());
		} catch (Exception e) {
			log.info("[store_tenant_app_user_biz_log] error: ", e);
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
