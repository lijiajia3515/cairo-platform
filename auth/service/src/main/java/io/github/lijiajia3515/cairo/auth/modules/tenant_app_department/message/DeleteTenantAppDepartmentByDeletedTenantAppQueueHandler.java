package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.DeletedTenantAppMessage;
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
import java.util.List;
import java.util.Map;


/**
 * 删除部门队列处理器
 */
@Slf4j
@Component
public class DeleteTenantAppDepartmentByDeletedTenantAppQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;


	public DeleteTenantAppDepartmentByDeletedTenantAppQueueHandler(ObjectMapper objectMapper,
																   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
	}

	@RabbitListener(
		queues = {"#{deleteTenantAppDepartmentByDeletedTenantAppQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[delete_tenant_app_department_by_deleted_tenant_app] message handler start");
			DeletedTenantAppMessage deletedTenantAppMessage = objectMapper.readValue(payload, DeletedTenantAppMessage.class);
			log.info("[delete_tenant_app_department_by_deleted_tenant_app] ===> 已删除企业应用: TenantId: {} AppId: {} EventAccountId: {} EventTime: {} ",
				deletedTenantAppMessage.getTenantId(),
				deletedTenantAppMessage.getAppId(),
				deletedTenantAppMessage.getEventAccountId(),
				deletedTenantAppMessage.getEventTime()
			);

			Criteria criteria = Criteria
				.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(deletedTenantAppMessage.getTenantId())
				.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(deletedTenantAppMessage.getAppId());

			Query query = Query.query(criteria);
			List<TenantAppDepartmentMongodb> deletedEndpointList = mongoTemplate.findAllAndRemove(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
			if (!deletedEndpointList.isEmpty()){
				mongoTemplate.insert(deletedEndpointList, MongodbConstants.DeletedCollection.TENANT_APP_DEPARTMENT);
			}
			deletedEndpointList.forEach(departmentMongodb -> {
				log.debug("部门删除成功: TenantId: {} AppId: {} DepartmentId: {} DepartmentName: {}",
					departmentMongodb.getTenantId(),
					departmentMongodb.getAppId(),
					departmentMongodb.getDepartmentId(),
					departmentMongodb.getDepartmentName()
				);
			});

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

			log.debug("[delete_tenant_app_department_by_deleted_tenant_app] message handler end");
		} catch (RuntimeException e) {
			log.info("[delete_tenant_app_department_by_deleted_tenant_app] message handler error", e);
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
