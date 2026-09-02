package io.github.lijiajia3515.cairo.auth.modules.sms.message.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SmsMsgMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.sms.template.DeleteSmsTemplateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * 删除消息根据已删除的短信模板 队列处理
 */
@Slf4j
@Component
public class DeleteSmsMsgByDeletedSmsTemplateQueueHandler {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final ObjectMapper objectMapper;

	public DeleteSmsMsgByDeletedSmsTemplateQueueHandler(MongoTemplate mongoTemplate, MongoTemplate readMongoTemplate, ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{deleteSmsMsgByDeletedSmsTemplateQueue.getName()}"}
	)
	public void deleteSmsMsgByDeletedSmsTemplateQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			DeleteSmsTemplateMessage deleteSmsTemplateMessage = objectMapper.readValue(payload, DeleteSmsTemplateMessage.class);
			log.debug("[delete_sms_msg_deleted_sms_template] message handler start");
			Criteria criteria = Criteria
				.where(SmsMsgMongodb.FIELD.APP_ID).is(deleteSmsTemplateMessage.getAppId())
				.and(SmsMsgMongodb.FIELD.BIZ_ID).is(deleteSmsTemplateMessage.getBizId());
			Query query = Query.query(criteria);
			long count = readMongoTemplate.count(query, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);

			// 批量删除 每次1000
			int each = (int) ((count / 1000) + (count % 1000 > 0 ? 1 : 0));
			for (int i = 0; i < each; i++) {
				Criteria deletedCriteria = Criteria
					.where(SmsMsgMongodb.FIELD.APP_ID).is(deleteSmsTemplateMessage.getAppId())
					.and(SmsMsgMongodb.FIELD.BIZ_ID).is(deleteSmsTemplateMessage.getBizId());
				Query deleteQuery = Query.query(deletedCriteria).limit(1000);
				List<SmsMsgMongodb> deletedSmsMsgList = mongoTemplate.findAllAndRemove(deleteQuery, SmsMsgMongodb.class, MongodbConstants.Collection.SMS_MSG);
				if (!deletedSmsMsgList.isEmpty()) {
					mongoTemplate.insert(deletedSmsMsgList, MongodbConstants.DeletedCollection.SMS_MSG);
				}
			}

			// 消费成功！
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_sms_msg_deleted_sms_template] message handler end");
		} catch (Exception e) {
			log.info("[delete_sms_msg_deleted_sms_template] error: ", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
		log.info("[delete_sms_msg_deleted_sms_template] end");
	}
}
