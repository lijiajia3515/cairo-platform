package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.ModifiedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.ModifiedSubappInfoMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 更新企业子应用信息
 */
@Slf4j
@Component
public class ModifyTenantSubappByModifiedSubappInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public ModifyTenantSubappByModifiedSubappInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@RabbitListener(
		queues = {"#{modifyTenantSubappByModifiedSubappInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_tenant_subapp_by_modified_subapp_info] message handler start");
			ModifiedSubappInfoMessage modifiedSubappInfoMessage = objectMapper.readValue(payload, ModifiedSubappInfoMessage.class);
			log.info("[modify_tenant_subapp_by_modified_subapp_info] ===> 已修改的子应用： AppId: {} OldSubappId: {} OldSubappName: {} NewSubappId: {} NewSubappName: {} EventCairoUserId: {} EventTime: {} ",
				modifiedSubappInfoMessage.getAppId(),
				modifiedSubappInfoMessage.getOldSubappId(),
				modifiedSubappInfoMessage.getOldSubappName(),
				modifiedSubappInfoMessage.getNewSubappId(),
				modifiedSubappInfoMessage.getNewSubappName(),
				modifiedSubappInfoMessage.getEventCairoUserId(),
				modifiedSubappInfoMessage.getEventTime()
			);

			if (modifiedSubappInfoMessage.getOldSubappId().equals(modifiedSubappInfoMessage.getNewSubappId())) {
				log.debug("未更新SubappId, 无需更新子应用版本");
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[modify_tenant_subapp_by_modified_subapp_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(TenantSubappMongodb.FIELD.SUBAPP_ID).is(modifiedSubappInfoMessage.getOldSubappId());
			Query query = Query.query(criteria);
			List<TenantSubappMongodb> tenantSubappMongodbs = mongoTemplate.find(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
			Update update = new Update();
			update.set(TenantSubappMongodb.FIELD.SUBAPP_ID, modifiedSubappInfoMessage.getNewSubappId());
			// update.currentDate(SubappVersionMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateMulti(query, update, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
			log.debug("tenant_subapp updateResult: {}", updateResult);
			tenantSubappMongodbs.forEach(tenantSubapp -> {
				//发送企业子应用修改完成消息
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_TENANT_SUBAPP_INFO, tenantSubapp.getTenantId(), tenantSubapp.getAppId()),
						objectMapper.writeValueAsString(ModifiedTenantSubappMessage
							.builder()
							.tenantId(tenantSubapp.getTenantId())
							.appId(tenantSubapp.getAppId())
							.endpointId(tenantSubapp.getEndpointId())
							.subappId(tenantSubapp.getSubappId())
							.eventTime(LocalDateTime.now())
							.build()
						),
						new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("send create_tenant_subapp mq warn", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_tenant_subapp_by_modified_subapp_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_tenant_subapp_by_modified_subapp_info] message handler error", e);
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
