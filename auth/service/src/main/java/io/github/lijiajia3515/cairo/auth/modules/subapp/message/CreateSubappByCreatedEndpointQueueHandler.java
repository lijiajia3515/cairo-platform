package io.github.lijiajia3515.cairo.auth.modules.subapp.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.CreatedEndpointMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.CreatedSubappMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

/**
 * 创建默认子应用根据已创建的终端 队列处理器
 * <p>
 * 终端创建完成即自动挂一个默认子应用（subappId 复用终端ID，准入范围缺省开放），
 * 并发送创建子应用消息触发默认版本创建，保证新终端开箱可用。
 */
@Slf4j
@Component
public class CreateSubappByCreatedEndpointQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateSubappByCreatedEndpointQueueHandler(ObjectMapper objectMapper,
														@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														TransactionTemplate transactionTemplate,
														RabbitTemplate rabbitTemplate,
														CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
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
		queues = {"#{createSubappByCreatedEndpointQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedEndpointMessage createdEndpointMessage = objectMapper.readValue(payload, CreatedEndpointMessage.class);
			log.debug("[create_subapp_by_created_endpoint] message handler start: AppId: {} EndpointId: {} EventCairoUserId: {} EventTime: {} ",
				createdEndpointMessage.getAppId(),
				createdEndpointMessage.getEndpointId(),
				createdEndpointMessage.getEventCairoUserId(),
				createdEndpointMessage.getEventTime()
			);

			// 默认子应用已存在（重复投递）则跳过
			Criteria existsCriteria = Criteria
				.where(SubappMongodb.FIELD.APP_ID).is(createdEndpointMessage.getAppId())
				.and(SubappMongodb.FIELD.ENDPOINT_ID).is(createdEndpointMessage.getEndpointId())
				.and(SubappMongodb.FIELD.SUBAPP_ID).is(createdEndpointMessage.getEndpointId());
			boolean exists = mongoTemplate.exists(Query.query(existsCriteria), SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
			if (exists) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[create_subapp_by_created_endpoint] handler end: 默认子应用已存在");
				return;
			}

			SubappMongodb subappMongodb = transactionTemplate.execute(status -> {
				try {
					SubappMongodb newSubappMongodb = SubappMongodb.builder()
						.id(CoreConstants.SNOWFLAKE.nextIdStr())
						.appId(createdEndpointMessage.getAppId())
						.endpointId(createdEndpointMessage.getEndpointId())
						.subappId(createdEndpointMessage.getEndpointId())
						.subappName(createdEndpointMessage.getEndpointName())
						.subappIcon(Optional.ofNullable(createdEndpointMessage.getIcon()).orElse(""))
						// 默认子应用跟随终端准入范围（缺省开放）
						.scope(Optional.ofNullable(createdEndpointMessage.getScope()).orElse(AccessScope.PUBLIC.getScopeValue()))
						.enabled(true)
						.sort((int) LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8)))
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(createdEndpointMessage.getEventCairoUserId())
							.updateUserId(createdEndpointMessage.getEventCairoUserId())
							.build())
						.build();
					return mongoTemplate.insert(newSubappMongodb, MongodbConstants.Collection.SUBAPP);
				} catch (Exception e) {
					status.setRollbackOnly();
					log.warn("[create_subapp_by_created_endpoint] 创建默认子应用失败", e);
					return null;
				}
			});

			if (subappMongodb != null) {
				// 发送创建子应用消息（触发默认版本创建）
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_SUBAPP, subappMongodb.getAppId()),
					objectMapper.writeValueAsString(CreatedSubappMessage
						.builder()
						.id(subappMongodb.getId())
						.appId(subappMongodb.getAppId())
						.endpointId(subappMongodb.getEndpointId())
						.subappId(subappMongodb.getSubappId())
						.subappName(subappMongodb.getSubappName())
						.subappIcon(subappMongodb.getSubappIcon())
						.scope(subappMongodb.getScope())
						.enabled(subappMongodb.getEnabled())
						.eventCairoUserId(createdEndpointMessage.getEventCairoUserId())
						.eventTime(LocalDateTime.now())
						.build()
					),
					new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_subapp_by_created_endpoint] message handler end");
		} catch (JsonProcessingException e) {
			log.info("[create_subapp_by_created_endpoint] message handler error", e);
			// 消息体异常无法恢复，直接丢弃
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
		} catch (RuntimeException e) {
			log.info("[create_subapp_by_created_endpoint] message handler error", e);
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
