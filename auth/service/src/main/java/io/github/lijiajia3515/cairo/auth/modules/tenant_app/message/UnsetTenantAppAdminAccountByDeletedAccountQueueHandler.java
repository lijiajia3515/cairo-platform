package io.github.lijiajia3515.cairo.auth.modules.tenant_app.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
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
 * 取消系统管理员（根据已删除的账号）处理器
 */

@Slf4j
@Component
public class UnsetTenantAppAdminAccountByDeletedAccountQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;



	public UnsetTenantAppAdminAccountByDeletedAccountQueueHandler(ObjectMapper objectMapper,
																  MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{unsetTenantAppAdminAccountByDeletedAccountQueue.getName()}"}
	)
	public void unsetTenantAppAdminAccountByDeletedAccountQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[unset_tenant_app_admin_account_by_deleted_account] message handler start");
			DeletedAccountMessage deletedAccountMessage = objectMapper.readValue(payload, DeletedAccountMessage.class);
			log.info("[unset_tenant_app_admin_account_by_deleted_account] ===> 已删除账号: AccountId: {} EventAccountId: {} EventTime: {} ",
				deletedAccountMessage.getAccountId(),
				deletedAccountMessage.getEventAccountId(),
				deletedAccountMessage.getEventTime()
			);

			Criteria criteria = new Criteria();
			Query query = Query.query(criteria);
			Update update = new Update();
			update.pull(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS, deletedAccountMessage.getAccountId());
			mongoTemplate.updateMulti(query, update, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[unset_tenant_app_admin_account_by_deleted_account] message handler end");
		} catch (RuntimeException e) {
			log.info("[unset_tenant_app_admin_account_by_deleted_account] message handler error", e);
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
