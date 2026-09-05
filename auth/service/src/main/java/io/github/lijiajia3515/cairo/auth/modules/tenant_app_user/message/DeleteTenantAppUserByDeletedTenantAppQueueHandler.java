package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.DeletedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
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
 * 删除用户（根据已删除的企业应用）处理器
 */
@Slf4j
@Component
public class DeleteTenantAppUserByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final RabbitTemplate rabbitTemplate;

	private final CairoRabbitmqTool cairoRabbitmqTool;

	public DeleteTenantAppUserByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
															 MongoTemplate mongoTemplate,
															 RabbitTemplate rabbitTemplate,
															 CairoRabbitmqTool cairoRabbitmqTool) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppUserByDeletedTenantAppQueue.getName()}"}
	)
	public void deleteTenantAppUserByDeletedTenantAppQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_app_user_by_deleted_tenant_app] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[delete_tenant_app_user_by_deleted_tenant_app] ===> 已删除企业应用: TenantId: {} AppId: {} EventAccountId: {} EventTime: {} ",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId(),
				deletedTenantAppMessage.getEventAccountId(),
				deletedTenantAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantAppUserMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<TenantAppUserMongodb> deleteUserList = mongoTemplate.find(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

			deleteUserList.forEach(deleteUser -> {
				try {
					Criteria deleteCriteria = Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(deleteUser.getTenantId())
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(deleteUser.getAppId())
						.and(TenantAppUserMongodb.FIELD.USER_ID).is(deleteUser.getUserId());

					Query deleteQuery = Query.query(deleteCriteria);
					TenantAppUserMongodb deletedUserMongodb = mongoTemplate.findAndRemove(deleteQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
					if (deletedUserMongodb != null) {
						mongoTemplate.insert(deletedUserMongodb, MongodbConstants.DeletedCollection.TENANT_APP_USER);
						log.debug("用户删除成功: TenantId: {} AppId: {} UserId: {} Nickname: {}",
							deletedUserMongodb.getTenantId(),
							deletedUserMongodb.getAppId(),
							deletedUserMongodb.getUserId(),
							deletedUserMongodb.getNickname()
						);

						DeletedTenantAppUserMessage deletedTenantAppUserMessage = DeletedTenantAppUserMessage.builder()
							.tenantId(deleteUser.getTenantId())
							.appId(deleteUser.getAppId())
							.userId(deleteUser.getUserId())
							.nickname(deleteUser.getNickname())
							.accountId(deleteUser.getAccountId())
							.eventTime(LocalDateTime.now())
							.build();

						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_APP_USER, deletedTenantAppUserMessage.getTenantId(), deletedTenantAppUserMessage.getAppId()),
							objectMapper.writeValueAsString(deletedTenantAppUserMessage),
							new CorrelationData(CoreConstants.nextIdStr())
						);

					}
				} catch (Exception e) {
					log.warn("企业应用级用户删除失败: TenantId: {} AppId: {} UserId: {} Nickname: {} 异常：{}",
						deleteUser.getTenantId(),
						deleteUser.getAppId(),
						deleteUser.getUserId(),
						deleteUser.getNickname(),
						e.getMessage());
				}
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_app_user_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_app_user_by_deleted_tenant_app] message handler error", e);
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
