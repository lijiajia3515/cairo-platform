package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplatePermissionMongodb;
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
 * 创建企业应用角色根据已创建企业应用模板 队列处理器
 */
@Slf4j
@Component
public class CreateTenantAppRoleByCreatedTenantTemplateQueueHandler {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;

	public CreateTenantAppRoleByCreatedTenantTemplateQueueHandler(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
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
		queues = {"#{createTenantAppRoleByCreatedTenantTemplateQueue.getName()}"}
	)
	public void createTenantAppRoleByCreatedTenantTemplateQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.debug("[create_tenant_app_role_by_created_tenant_template] message handler start: TenantId: {} AppId: {} EndpointIds: {} AdminAccountIds: {} ",
				createdTenantAppMessage.getTenantId(),
				createdTenantAppMessage.getAppId(),
				createdTenantAppMessage.getEndpointIds(),
				createdTenantAppMessage.getAdminAccountIds()
			);
			transactionTemplate.executeWithoutResult(transactionStatus -> {
				try {
					// 企业角色模板列表
					Criteria roleCriteria = Criteria.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
						.and(TenantAppRoleTemplateMongodb.FIELD.ENABLED).is(true);
					List<TenantAppRoleTemplateMongodb> tenantAppRoleTemplateMongodbs = mongoTemplate.find(Query.query(roleCriteria), TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE);
					Set<TenantAppRoleMongodb> insertTenantAppRoleMongodbs = tenantAppRoleTemplateMongodbs.stream().map(role -> TenantAppRoleMongodb.builder()
						.tenantId(createdTenantAppMessage.getTenantId())
						.appId(createdTenantAppMessage.getAppId())
						.roleId(role.getTenantAppRoleTemplateId())
						.roleName(role.getTenantAppRoleTemplateName())
						.enabled(true)
						.remark(role.getRemark())
						.build()).collect(Collectors.toSet());
					mongoTemplate.insert(insertTenantAppRoleMongodbs, MongodbConstants.Collection.TENANT_APP_ROLE);

					//权限
					Criteria permissionCriteria = Criteria.where(TenantAppRoleTemplatePermissionMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
						.and(TenantAppRoleTemplatePermissionMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).in(tenantAppRoleTemplateMongodbs.stream().map(TenantAppRoleTemplateMongodb::getTenantAppRoleTemplateId).collect(Collectors.toList()));
					List<TenantAppRoleTemplatePermissionMongodb> tenantAppRoleTemplatePermissionMongodbs = mongoTemplate.find(Query.query(permissionCriteria), TenantAppRoleTemplatePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE_PERMISSION);
					Set<TenantAppRolePermissionMongodb> insertTenantAppRolePermissionMongodbs = tenantAppRoleTemplatePermissionMongodbs.stream().map(permission -> TenantAppRolePermissionMongodb.builder()
						.tenantId(createdTenantAppMessage.getTenantId())
						.appId(createdTenantAppMessage.getAppId())
						.roleId(permission.getTenantAppRoleTemplateId())
						.endpointId(permission.getEndpointId())
						.subappId(permission.getSubappId())
						.subappVersion(permission.getSubappVersion())
						.permissionIds(permission.getPermissionIds())
						.build()).collect(Collectors.toSet());
					mongoTemplate.insert(insertTenantAppRolePermissionMongodbs, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				} catch (Exception e) {
					log.warn("[create_tenant_app_role_by_created_tenant_template] handler error", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_tenant_app_role_by_created_tenant_template] handler end: {}", createdTenantAppMessage.getTenantId());
		} catch (Exception e) {
			log.info("[create_tenant_app_role_by_created_tenant_template] handler error", e);
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
