package io.github.lijiajia3515.cairo.auth.modules.login_log.app_user_login_log.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
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
 * 删除应用级用户登录日志根据已删除的企业终端队列处理器
 */
@Slf4j
@Component
public class DeleteAppUserLoginLogByDeletedEndpointQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public DeleteAppUserLoginLogByDeletedEndpointQueueHandler(ObjectMapper objectMapper, MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAppUserLoginLogByDeletedEndpointQueue.getName()}"}
	)
	public void deleteAppUserLoginLogByDeletedEndpointQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_user_login_log_by_deleted_endpoint] message handler start");
			DeletedEndpointMessage deletedEndpointMessage = objectMapper.readValue(payload, DeletedEndpointMessage.class);
			log.info("[delete_app_user_login_log_by_deleted_endpoint] ===> 已删除应用: AppId: {} EventCairoUserId: {} EventTime: {} ",
				deletedEndpointMessage.getAppId(),
				deletedEndpointMessage.getEventCairoUserId(),
				deletedEndpointMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppUserLoginLogMongodb.FIELD.APP_ID).is(deletedEndpointMessage.getAppId())
				.and(AppUserLoginLogMongodb.FIELD.ENDPOINT_ID).is(deletedEndpointMessage.getEndpointId());

			Query query = Query.query(criteria);
			long count = mongoTemplate.count(query, AppUserLoginLogMongodb.class, MongodbConstants.Collection.APP_USER_LOGIN_LOG);
			long each = count % 1000 == 0 ? count / 1000 : (count / 1000) + 1;
			query.limit(1000);
			for (long i = 0; i < each; i++) {
				try {
					List<AppUserLoginLogMongodb> deletedAppUserLoginLogList = mongoTemplate.findAllAndRemove(query, AppUserLoginLogMongodb.class, MongodbConstants.Collection.APP_USER_LOGIN_LOG);
					if (!deletedAppUserLoginLogList.isEmpty()) {
						mongoTemplate.insert(deletedAppUserLoginLogList, MongodbConstants.DeletedCollection.APP_USER_LOGIN_LOG);
					}
					log.debug("终端登录数据数据删除成功:  AppId: {} EndpointId: {} DeletedCount: {}",
						deletedEndpointMessage.getAppId(),
						deletedEndpointMessage.getEndpointId(),
						deletedAppUserLoginLogList.size()
					);
				} catch (Exception e) {
					log.warn("delete app_user_login_log: {}", e.getMessage());
				}
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[delete_app_user_login_log_by_deleted_endpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_user_login_log_by_deleted_endpoint] message handler error", e);
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
