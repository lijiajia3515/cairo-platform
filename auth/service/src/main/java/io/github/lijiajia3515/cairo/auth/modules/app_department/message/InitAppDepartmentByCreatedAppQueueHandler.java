package io.github.lijiajia3515.cairo.auth.modules.app_department.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.message.app.CreatedAppMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
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

import java.io.IOException;
import java.util.Map;


/**
 * 初始化应用部门 队列处理器
 */
@Slf4j
@Component
public class InitAppDepartmentByCreatedAppQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final ObjectMapper objectMapper;

	public InitAppDepartmentByCreatedAppQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													 ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 业务队列
	 *
	 * @param headers headers
	 * @param payload payload
	 * @param message message
	 * @param channel channel
	 * @throws java.io.IOException 1
	 */
	@RabbitListener(
		queues = {"#{initAppDepartmentByCreatedAppQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedAppMessage createdAppMessage = objectMapper.readValue(payload, CreatedAppMessage.class);
			log.debug("[init_app_department_by_created_app] message handler start:AppId: {} AdminAccountIds: {} ",
				createdAppMessage.getAppId(),
				createdAppMessage.getAdminAccountIds()
			);

			AppDepartmentMongodb rootAppDepartmentMongodb = AppDepartmentMongodb.builder()
				.appId(createdAppMessage.getAppId())
				.departmentId(CoreConstants.nextIdStr())
				.departmentName("组织结构")
				.parentId(null)
				.root(true)
				.remark(String.format("%s的根节点", createdAppMessage.getAppId()))
				.leftNo(1)
				.rightNo(2)
				.depth(0)
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(CairoSecurityContextHolder.getSubappUserId())
					.updateUserId(CairoSecurityContextHolder.getSubappUserId())
					.build())
				.build();
			Criteria criteria = Criteria.where(AppDepartmentMongodb.FIELD.APP_ID).is(createdAppMessage.getAppId())
				.and(AppDepartmentMongodb.FIELD.ROOT).is(true);
			Query query = Query.query(criteria);
			boolean exists = mongoTemplate.exists(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
			if (!exists) {
				mongoTemplate.insert(rootAppDepartmentMongodb, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("create app department root node: {} {} {}", rootAppDepartmentMongodb.getDepartmentId(), rootAppDepartmentMongodb.getDepartmentName(), rootAppDepartmentMongodb.getRemark());
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[init_app_department_by_created_app] handler end: {}", createdAppMessage.getAppId());
		} catch (Exception e) {
			log.info("[init_app_department_by_created_app] handler error", e);
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
