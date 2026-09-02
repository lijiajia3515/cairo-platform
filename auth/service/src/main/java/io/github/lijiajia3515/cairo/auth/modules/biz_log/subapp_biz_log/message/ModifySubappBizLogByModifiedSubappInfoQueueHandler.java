package io.github.lijiajia3515.cairo.auth.modules.biz_log.subapp_biz_log.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.ModifiedSubappInfoMessage;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogSubappMongodb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.Map;


/**
 * 更新子应用业务日志根据已修改子应用信息
 */
@Slf4j
@Component
public class ModifySubappBizLogByModifiedSubappInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;


	public ModifySubappBizLogByModifiedSubappInfoQueueHandler(ObjectMapper objectMapper,
																   MongoTemplate mongoTemplate,
																   TransactionTemplate transactionTemplate,
																   MongoTemplate readMongoTemplate,
																   RabbitTemplate rabbitTemplate,
																   CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@RabbitListener(
		queues = {"#{modifySubappBizLogByModifiedSubappInfoQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_subapp_biz_log_by_modified_subapp_info] message handler start");
			ModifiedSubappInfoMessage modifiedSubappInfoMessage = objectMapper.readValue(payload, ModifiedSubappInfoMessage.class);
			log.info("[modify_subapp_biz_log_by_modified_subapp_info] ===> 已修改的子应用：Id: {} AppId: {}  EndpointId: {} OldSubappId: {} OldSubappName: {} NewSubappId: {} NewSubappName: {} EventCairoUserId: {} EventTime: {} ",
				modifiedSubappInfoMessage.getAppId(),
				modifiedSubappInfoMessage.getEndpointId(),
				modifiedSubappInfoMessage.getOldSubappId(),
				modifiedSubappInfoMessage.getOldSubappName(),
				modifiedSubappInfoMessage.getNewSubappId(),
				modifiedSubappInfoMessage.getNewSubappName(),
				modifiedSubappInfoMessage.getEventCairoUserId(),
				modifiedSubappInfoMessage.getEventCairoUserId(),
				modifiedSubappInfoMessage.getEventTime()
			);

			if (modifiedSubappInfoMessage.getOldSubappId().equals(modifiedSubappInfoMessage.getNewSubappId())) {
				log.debug("未更新SubappId, 无需更新业务日志");
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[modify_subapp_biz_log_by_modified_subapp_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(BizLogSubappMongodb.FIELD.APP_ID).is(modifiedSubappInfoMessage.getAppId())
				.and(BizLogSubappMongodb.FIELD.ENDPOINT_ID).is(modifiedSubappInfoMessage.getEndpointId())
				.and(BizLogSubappMongodb.FIELD.SUBAPP_ID).is(modifiedSubappInfoMessage.getOldSubappId());

			Query query = Query.query(criteria);

			Update update = new Update();
			update.set(BizLogSubappMongodb.FIELD.SUBAPP_ID, modifiedSubappInfoMessage.getNewSubappId());
			update.currentDate(BizLogSubappMongodb.FIELD.METADATA.UPDATE_TIME);

			UpdateResult updateResult = mongoTemplate.updateMulti(query, update, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
			log.debug("updateResult: {}", updateResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_subapp_biz_log_by_modified_subapp_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_subapp_biz_log_by_modified_subapp_info] message handler error", e);
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
