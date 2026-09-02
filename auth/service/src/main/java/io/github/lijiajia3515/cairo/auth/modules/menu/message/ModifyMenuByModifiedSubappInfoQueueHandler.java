package io.github.lijiajia3515.cairo.auth.modules.menu.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.subapp.ModifiedSubappInfoMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * 更新菜单子应用信息
 */
@Slf4j
@Component
public class ModifyMenuByModifiedSubappInfoQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public ModifyMenuByModifiedSubappInfoQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{modifyMenuByModifiedSubappInfoQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[modify_menu_by_modified_subapp_info] message handler start");
			ModifiedSubappInfoMessage modifiedSubappInfoMessage = objectMapper.readValue(payload, ModifiedSubappInfoMessage.class);
			log.info("[modify_menu_by_modified_subapp_info] ===> 已修改的子应用： AppId: {} OldSubappId: {} OldSubappName: {} NewSubappId: {} NewSubappName: {} EventCairoUserId: {} EventTime: {} ",
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
				log.debug("[modify_menu_by_modified_subapp_info] message handler end");
				return;
			}

			Criteria criteria = Criteria
				.where(MenuMongodb.FIELD.SUBAPP_ID).is(modifiedSubappInfoMessage.getOldSubappId());
			Query query = Query.query(criteria);

			Update update = new Update();
			update.set(MenuMongodb.FIELD.SUBAPP_ID, modifiedSubappInfoMessage.getNewSubappId());
			update.set(MenuMongodb.FIELD.METADATA.UPDATE_USER_ID, modifiedSubappInfoMessage.getEventCairoUserId());
			update.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateMulti(query, update, MenuMongodb.class, MongodbConstants.Collection.MENU);
			log.debug("menu updateResult: {}", updateResult);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[modify_menu_by_modified_subapp_info] message handler end");
		} catch (RuntimeException e) {
			log.info("[modify_menu_by_modified_subapp_info] message handler error", e);
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
