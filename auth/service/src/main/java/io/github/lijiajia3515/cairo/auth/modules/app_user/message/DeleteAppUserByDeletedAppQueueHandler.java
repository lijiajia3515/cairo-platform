package io.github.lijiajia3515.cairo.auth.modules.app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.DeletedAppUserMessage;
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
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 删除应用用户（根据已删除的应用）处理器
 */
@Slf4j
@Component
public class DeleteAppUserByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteAppUserByDeletedAppQueueHandler(ObjectMapper objectMapper,
													MongoTemplate mongoTemplate,
													RabbitTemplate rabbitTemplate,
													CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@RabbitListener(
		queues = {"#{deleteAppUserByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_user_by_deleted_tenant_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_app_user_by_deleted_tenant_app] ===> 已删除应用:  AppId: {}  EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppUserMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<AppUserMongodb> deleteAppUserList = mongoTemplate.find(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

			deleteAppUserList.forEach(deleteAppUser -> {
				try {
					Criteria deleteCriteria = Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(deleteAppUser.getAppId())
						.and(AppUserMongodb.FIELD.USER_ID).is(deleteAppUser.getUserId());

					Query deleteQuery = Query.query(deleteCriteria);
					AppUserMongodb deletedAppUserMongodb = mongoTemplate.findAndRemove(deleteQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
					if (deletedAppUserMongodb != null) {
						mongoTemplate.insert(deletedAppUserMongodb, MongodbConstants.DeletedCollection.APP_USER);
						log.debug("应用用户删除成功: AppId: {} AppUserId: {} Nickname: {}",
							deletedAppUserMongodb.getAppId(),
							deletedAppUserMongodb.getUserId(),
							deletedAppUserMongodb.getNickname()
						);

						DeletedAppUserMessage deletedAppUserMessage = DeletedAppUserMessage.builder()
							.appId(deleteAppUser.getAppId())
							.userId(deleteAppUser.getUserId())
							.nickname(deleteAppUser.getNickname())
							.accountId(deleteAppUser.getAccountId())
							.eventTime(LocalDateTime.now())
							.build();

						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_APP_USER,  deletedAppUserMessage.getAppId()),
							objectMapper.writeValueAsString(deletedAppUserMessage),
							new CorrelationData(CoreConstants.nextIdStr())
						);

					}
				} catch (Exception e) {
					log.warn("应用用户删除失败: AppId: {} AppUserId: {} Nickname: {} 异常：{}",
						deleteAppUser.getAppId(),
						deleteAppUser.getUserId(),
						deleteAppUser.getNickname(),
						e.getMessage());
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_app_user_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_user_by_deleted_tenant_app] message handler error", e);
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
