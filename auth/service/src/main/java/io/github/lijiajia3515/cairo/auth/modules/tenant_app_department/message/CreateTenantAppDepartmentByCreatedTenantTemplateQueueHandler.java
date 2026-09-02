package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 创建企业应用部门根据已创建企业应用模板 队列处理器
 */
@Slf4j
@Component
public class CreateTenantAppDepartmentByCreatedTenantTemplateQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;

	public CreateTenantAppDepartmentByCreatedTenantTemplateQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																		TransactionTemplate transactionTemplate,
																		ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
	}

	/**
	 * 业务队列
	 *
	 * @param headers headers
	 * @param payload payload
	 * @param message message
	 * @param channel channel
	 * @throws IOException 1
	 */
	@RabbitListener(
		queues = {"#{createTenantAppDepartmentByCreatedTenantTemplateQueue.getName()}"}
	)
	public void createTenantAppDepartmentByCreatedTenantTemplateQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.debug("[create_tenant_app_department_by_created_tenant_template] message handler start: TenantId: {} AppId: {} EndpointIds: {} AdminAccountIds: {} ",
				createdTenantAppMessage.getTenantId(),
				createdTenantAppMessage.getAppId(),
				createdTenantAppMessage.getEndpointIds(),
				createdTenantAppMessage.getAdminAccountIds()
			);
			if (createdTenantAppMessage.getAppId() == null || createdTenantAppMessage.getTenantId().isEmpty()) {
				return;
			}


			//查询企业部门模板状态
			Criteria appCriteria = Criteria.where(AppMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId());
			AppMongodb appMongodb = mongoTemplate.findOne(Query.query(appCriteria), AppMongodb.class, MongodbConstants.Collection.APP);
			if (appMongodb == null || appMongodb.getTenantAppDepartmentTemplateStatus() == null || !appMongodb.getTenantAppDepartmentTemplateStatus()) {
				return;
			}

			transactionTemplate.executeWithoutResult(transactionStatus -> {
				try {
					// 企业部门模板列表
					Criteria departmentCriteria = Criteria.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId());
					List<TenantAppDepartmentTemplateMongodb> departmentTemplateMongodbs = mongoTemplate.find(Query.query(departmentCriteria), TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
					//删除企业部门数据
					Criteria deleteDepartmentCriteria = Criteria.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(createdTenantAppMessage.getTenantId())
						.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId());
					List<TenantAppDepartmentMongodb> remove = mongoTemplate.findAllAndRemove(Query.query(deleteDepartmentCriteria), TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
					log.debug("[delete_tenant_app_department]{}", remove);

					Set<TenantAppDepartmentMongodb> insertTenantAppDepartmentMongodbs = departmentTemplateMongodbs.stream().map(tenantAppDepartmentTemplateMongodb -> TenantAppDepartmentMongodb.builder()
						.tenantId(createdTenantAppMessage.getTenantId())
						.appId(createdTenantAppMessage.getAppId())
						.parentId(tenantAppDepartmentTemplateMongodb.getParentId())
						.root(tenantAppDepartmentTemplateMongodb.isRoot())
						.departmentId(tenantAppDepartmentTemplateMongodb.getTenantAppDepartmentTemplateId())
						.departmentName(tenantAppDepartmentTemplateMongodb.getTenantAppDepartmentTemplateName())
						.remark(tenantAppDepartmentTemplateMongodb.getRemark())
						.leftNo(tenantAppDepartmentTemplateMongodb.getLeftNo())
						.rightNo(tenantAppDepartmentTemplateMongodb.getRightNo())
						.depth(tenantAppDepartmentTemplateMongodb.getDepth())
						.build()).collect(Collectors.toSet());
					mongoTemplate.insert(insertTenantAppDepartmentMongodbs, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				} catch (Exception e) {
					log.warn("[create_tenant_app_department_by_created_tenant_template] handler error", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_tenant_app_department_by_created_tenant_template] handler end: {}", createdTenantAppMessage.getTenantId());
		} catch (Exception e) {
			log.info("[create_tenant_app_department_by_created_tenant_template] handler error", e);
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
