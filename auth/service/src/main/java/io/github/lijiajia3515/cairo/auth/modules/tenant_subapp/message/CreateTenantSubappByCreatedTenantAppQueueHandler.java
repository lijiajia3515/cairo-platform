package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.scope.AccessScope;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_subapp.CreatedTenantSubappMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 创建企业子应用根据创建企业应用 队列处理器
 */
@Slf4j
@Component
public class CreateTenantSubappByCreatedTenantAppQueueHandler {
	private final SubappCommonService subappCommonService;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateTenantSubappByCreatedTenantAppQueueHandler(SubappCommonService subappCommonService,
																@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																TransactionTemplate transactionTemplate,
																ObjectMapper objectMapper,
																RabbitTemplate rabbitTemplate,
																CairoRabbitmqTool cairoRabbitmqTool) {
		this.subappCommonService = subappCommonService;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
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
		queues = {"#{createTenantSubappByCreatedTenantAppQueue.getName()}"}
	)
	public void createUserByCreatedTenantAppQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.debug("[create_tenant_subapp_by_created_tenant_app] message handler start: TenantId: {} AppId: {} EndpointIds: {} SubappIds: {} AdminAccountIds: {} ",
				createdTenantAppMessage.getTenantId(),
				createdTenantAppMessage.getAppId(),
				createdTenantAppMessage.getEndpointIds(),
				createdTenantAppMessage.getSubappIds(),
				createdTenantAppMessage.getAdminAccountIds()
			);

			if (createdTenantAppMessage.getSubappIds() == null || createdTenantAppMessage.getSubappIds().isEmpty()) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[create_tenant_subapp_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
				return;
			}
			// 子应用列表（只镜像开放子应用；企业准入的模块需企业按模块开通，不自动生成）
			List<Subapp> subappIds = subappCommonService.getSubappListBySubappIds(createdTenantAppMessage.getSubappIds())
				.stream()
				.filter(subapp -> subapp.getScope() == null || AccessScope.PUBLIC.getScopeValue().equals(subapp.getScope()))
				.collect(Collectors.toList());

			subappIds.forEach(subapp -> {
				transactionTemplate.executeWithoutResult(transactionStatus -> {
					try {
						Criteria tenantSubappCriteria = Criteria
							.where(TenantSubappMongodb.FIELD.TENANT_ID).is(createdTenantAppMessage.getTenantId())
							.and(TenantSubappMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
							.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(subapp.getEndpointId())
							.and(TenantSubappMongodb.FIELD.SUBAPP_ID).is(subapp.getSubappId());
						Query userQuery = Query.query(tenantSubappCriteria);
						boolean exists = mongoTemplate.exists(userQuery, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
						if (!exists) {
							TenantSubappMongodb insertTenantEndpointMongodb = TenantSubappMongodb.builder()
								.tenantId(createdTenantAppMessage.getTenantId())
								.appId(createdTenantAppMessage.getAppId())
								.endpointId(subapp.getEndpointId())
								.subappId(subapp.getSubappId())
								.enabled(true)
								.metadata(AccountMetadataMongodb.builder()
									.createAccountId(createdTenantAppMessage.getEventAccountId())
									.updateAccountId(createdTenantAppMessage.getEventAccountId())
									.build())
								.build();
							mongoTemplate.insert(insertTenantEndpointMongodb, MongodbConstants.Collection.TENANT_SUBAPP);
						}
					} catch (Exception e) {
						log.warn("[create_tenant_subapp_by_created_tenant_app] handler error", e);
					}
				});
				//发送企业子应用创建完成消息
				try {
					rabbitTemplate.convertAndSend(
						cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
						cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_SUBAPP, createdTenantAppMessage.getTenantId(), createdTenantAppMessage.getAppId()),
						objectMapper.writeValueAsString(CreatedTenantSubappMessage
							.builder()
							.tenantId(createdTenantAppMessage.getTenantId())
							.appId(createdTenantAppMessage.getAppId())
							.endpointId(subapp.getEndpointId())
							.subappId(subapp.getSubappId())
							.eventAccountId(createdTenantAppMessage.getEventAccountId())
							.eventTime(LocalDateTime.now())
							.build()
						),
						new CorrelationData(CoreConstants.nextIdStr())
					);
				} catch (JsonProcessingException e) {
					log.warn("send create_tenant_subapp mq warn", e);
				}
			});
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_tenant_subapp_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
		} catch (Exception e) {
			log.info("[create_tenant_subapp_by_created_tenant_app] handler error", e);
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
