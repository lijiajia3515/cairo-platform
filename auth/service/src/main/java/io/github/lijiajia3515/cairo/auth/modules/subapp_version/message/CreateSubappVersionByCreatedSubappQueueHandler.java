package io.github.lijiajia3515.cairo.auth.modules.subapp_version.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.CreatedSubappMessage;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.Map;

/**
 * 创建默认版本根据已创建的子应用 队列处理器
 * <p>
 * 子应用创建完成即自动建 v1 默认版本（菜单/权限同步以版本为单位），
 * 保证新子应用无需手工建版本即可接入前端 subapp-version 上下文。
 */
@Slf4j
@Component
public class CreateSubappVersionByCreatedSubappQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public CreateSubappVersionByCreatedSubappQueueHandler(ObjectMapper objectMapper,
																@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																TransactionTemplate transactionTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
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
		queues = {"#{createSubappVersionByCreatedSubappQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedSubappMessage createdSubappMessage = objectMapper.readValue(payload, CreatedSubappMessage.class);
			log.debug("[create_subapp_version_by_created_subapp] message handler start: AppId: {} EndpointId: {} SubappId: {} EventCairoUserId: {} EventTime: {} ",
				createdSubappMessage.getAppId(),
				createdSubappMessage.getEndpointId(),
				createdSubappMessage.getSubappId(),
				createdSubappMessage.getEventCairoUserId(),
				createdSubappMessage.getEventTime()
			);

			// 默认版本已存在（重复投递）则跳过
			Criteria existsCriteria = Criteria
				.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(createdSubappMessage.getSubappId())
				.and(SubappVersionMongodb.FIELD.SUBAPP_VERSION).is("v1");
			boolean exists = mongoTemplate.exists(Query.query(existsCriteria), SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);
			if (exists) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[create_subapp_version_by_created_subapp] handler end: 默认版本已存在");
				return;
			}

			transactionTemplate.executeWithoutResult(status -> {
				try {
					SubappVersionMongodb newSubappVersionMongodb = SubappVersionMongodb.builder()
						.subappId(createdSubappMessage.getSubappId())
						.subappVersion("v1")
						.subappRemark("默认版本")
						.enabled(true)
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(createdSubappMessage.getEventCairoUserId())
							.updateUserId(createdSubappMessage.getEventCairoUserId())
							.build())
						.build();
					mongoTemplate.insert(newSubappVersionMongodb, MongodbConstants.Collection.SUBAPP_VERSION);
				} catch (Exception e) {
					status.setRollbackOnly();
					log.warn("[create_subapp_version_by_created_subapp] 创建默认版本失败", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_subapp_version_by_created_subapp] message handler end");
		} catch (JsonProcessingException e) {
			log.info("[create_subapp_version_by_created_subapp] message handler error", e);
			// 消息体异常无法恢复，直接丢弃
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
		} catch (RuntimeException e) {
			log.info("[create_subapp_version_by_created_subapp] message handler error", e);
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
