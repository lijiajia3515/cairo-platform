package io.github.lijiajia3515.cairo.auth.modules.app_department.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.app.DeletedAppMessage;
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
 * 删除应用应用部门根据已删除应用 队列 处理器
 */
@Slf4j
@Component
public class DeleteAppDepartmentByDeletedAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteAppDepartmentByDeletedAppQueueHandler(ObjectMapper objectMapper,
														  MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteAppDepartmentByDeletedAppQueue.getName()}"}
	)
	public void bizHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_app_department_by_deleted_tenant_app] message handler start");
			DeletedAppMessage deletedAppMessage = objectMapper.readValue(payload, DeletedAppMessage.class);
			log.info("[delete_app_department_by_deleted_tenant_app] ===> 已删除应用:  AppId: {} EventTime: {} ",
				deletedAppMessage.getAppId(),
				deletedAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(AppDepartmentMongodb.FIELD.APP_ID).is(deletedAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<AppDepartmentMongodb> deletedEndpointList = mongoTemplate.findAllAndRemove(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
			if (!deletedEndpointList.isEmpty()){
				mongoTemplate.insert(deletedEndpointList, MongodbConstants.DeletedCollection.APP_DEPARTMENT);
			}
			deletedEndpointList.forEach(departmentMongodb -> {
				log.debug("应用部门删除成功:  AppId: {} DepartmentId: {} DepartmentName: {}",
					departmentMongodb.getAppId(),
					departmentMongodb.getDepartmentId(),
					departmentMongodb.getDepartmentName()
				);
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_app_department_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_app_department_by_deleted_tenant_app] message handler error", e);
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
