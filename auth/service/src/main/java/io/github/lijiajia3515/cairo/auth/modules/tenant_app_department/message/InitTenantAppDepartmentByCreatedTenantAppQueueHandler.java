package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;


/**
 * 初始化企业应用部门 队列处理器
 */
@Slf4j
@Component
public class InitTenantAppDepartmentByCreatedTenantAppQueueHandler {

	private final TenantCommonService tenantCommonService;
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;

	public InitTenantAppDepartmentByCreatedTenantAppQueueHandler(TenantCommonService tenantCommonService,
																 ObjectMapper objectMapper,
																 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate) {
		this.tenantCommonService = tenantCommonService;
		this.mongoTemplate = mongoTemplate;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{initTenantAppDepartmentByCreatedTenantAppQueue.getName()}"}
	)
	public void queueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.debug("[init_tenant_app_department_by_created_tenant_app] message handler start: {}", createdTenantAppMessage.getTenantId());
			Tenant tenant = tenantCommonService.getTenantMapByTenantIds(Collections.singleton(createdTenantAppMessage.getTenantId())).get(createdTenantAppMessage.getTenantId());

			if (tenant == null || tenant.getOwnerAccount() == null || tenant.getOwnerAccount().getAccountId() == null) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[init_tenant_app_department_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
				return;
			}

			TenantAppDepartmentMongodb rootDepartmentMongodb = TenantAppDepartmentMongodb.builder()
				.tenantId(createdTenantAppMessage.getTenantId())
				.appId(createdTenantAppMessage.getAppId())
				.parentId(null)
				.root(true)
				.departmentId(CoreConstants.nextIdStr())
				.departmentName(tenant.getTenantName())
				.remark(String.format("%s的%s的组织结构", tenant.getTenantName(), createdTenantAppMessage.getAppId()))
				.leftNo(1)
				.rightNo(2)
				.depth(0)
				.metadata(TenantAppUserMetadataMongodb.builder()
					.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.build())
				.build();
			mongoTemplate.insert(rootDepartmentMongodb, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[init_tenant_app_department_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
		} catch (Exception e) {
			log.info("[init_tenant_app_department_by_created_tenant_app] handler error", e);
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
