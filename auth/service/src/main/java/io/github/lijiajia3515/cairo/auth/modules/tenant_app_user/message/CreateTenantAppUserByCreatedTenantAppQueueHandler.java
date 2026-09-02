package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app.CreatedTenantAppMessage;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 创建用户根据创建企业应用 队列处理器
 */
@Slf4j
@Component
public class CreateTenantAppUserByCreatedTenantAppQueueHandler {
	private final AccountCommonService accountCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	public CreateTenantAppUserByCreatedTenantAppQueueHandler(AccountCommonService accountCommonService, TenantAppUserCommonService tenantAppUserCommonService,
															 @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
															 TransactionTemplate transactionTemplate, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool) {
		this.accountCommonService = accountCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
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
		queues = {"#{createTenantAppUserByCreatedTenantAppQueue.getName()}"}
	)
	public void createTenantAppUserByCreatedTenantAppQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			CreatedTenantAppMessage createdTenantAppMessage = objectMapper.readValue(payload, CreatedTenantAppMessage.class);
			log.debug("[create_tenant_app_user_by_created_tenant_app] message handler start: TenantId: {} AppId: {} EndpointIds: {} AdminAccountIds: {} ",
				createdTenantAppMessage.getTenantId(),
				createdTenantAppMessage.getAppId(),
				createdTenantAppMessage.getEndpointIds(),
				createdTenantAppMessage.getAdminAccountIds()
			);

			if (createdTenantAppMessage.getAdminAccountIds() == null || createdTenantAppMessage.getAdminAccountIds().isEmpty()) {
				// 消费成功
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
				log.debug("[create_tenant_app_user_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
				return;
			}
			// 账号列表
			List<Account> newAdminAccountList = accountCommonService.getAccountListByAccountIds(createdTenantAppMessage.getAdminAccountIds());

			// 企业用户模板列表
			Criteria userTemplateCriteria = Criteria.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
				.and(TenantAppUserTemplateMongodb.FIELD.ENABLED).is(true);
			List<TenantAppUserTemplateMongodb> tenantAppUserTemplateMongodbs = mongoTemplate.find(Query.query(userTemplateCriteria), TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);

			// 企业角色模板列表
			Criteria roleTemplateCriteria = Criteria.where(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
				.and(TenantAppRoleTemplateMongodb.FIELD.ENABLED).is(true);
			Set<String> roleIds = mongoTemplate.find(Query.query(roleTemplateCriteria), TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE)
				.stream().map(TenantAppRoleTemplateMongodb::getTenantAppRoleTemplateId).collect(Collectors.toSet());


			// 企业部门模板状态
			Criteria appCriteria = Criteria.where(AppMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId());
			Boolean departmentStatus = Optional.ofNullable(mongoTemplate.findOne(Query.query(appCriteria), AppMongodb.class, MongodbConstants.Collection.APP)).map(AppMongodb::getTenantAppDepartmentTemplateStatus).orElse(false);

			List<TenantAppUserMongodb> insertUsers = new ArrayList<>();

			newAdminAccountList.forEach(account -> {
				transactionTemplate.executeWithoutResult(transactionStatus -> {
					try {
						Criteria userCriteria = Criteria
							.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(createdTenantAppMessage.getTenantId())
							.and(TenantAppUserMongodb.FIELD.APP_ID).is(createdTenantAppMessage.getAppId())
							.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
						Query userQuery = Query.query(userCriteria);
						boolean exists = mongoTemplate.exists(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
						if (!exists) {
							String newUserId = tenantAppUserCommonService.getNewUserId();
							TenantAppUserMongodb user = TenantAppUserMongodb.builder()
								.tenantId(createdTenantAppMessage.getTenantId())
								.appId(createdTenantAppMessage.getAppId())
								.userId(newUserId)
								.nickname(account.getNickname())
								.admin(true)
								.roleIds(Collections.emptyList())
								.departmentIds(Collections.emptyList())
								.tagIds(Collections.emptyList())
								.enabled(true)
								.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
								.joinTime(createdTenantAppMessage.getEventTime())
								.accountId(account.getAccountId())
								.metadata(TenantAppUserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
								.build();
							insertUsers.add(user);
						}
					} catch (Exception e) {
						log.warn("[create_tenant_app_user_by_created_tenant_app] handler error", e);
					}
				});
			});
			try {
				Set<TenantAppUserMongodb> tenantAppUserMongodbs = tenantAppUserTemplateMongodbs.stream()
					.filter(template -> template.getEnabled() && !insertUsers.stream().map(TenantAppUserMongodb::getAccountId).collect(Collectors.toSet()).contains(template.getAccountId()))
					.map(user -> TenantAppUserMongodb.builder()
						.tenantId(createdTenantAppMessage.getTenantId())
						.appId(createdTenantAppMessage.getAppId())
						.userId(user.getTenantAppUserTemplateId())
						.nickname(user.getNickname())
						.phoneNumber(user.getPhoneNumber())
						.admin(user.getAdmin())
						.roleIds(getRoleTemplateIds(roleIds,user.getTenantAppRoleTemplateIds()))
						.position(user.getPosition())
						.mainDepartmentId(departmentStatus ? user.getTenantMainDepartmentTemplateId() : null)
						.departmentIds(departmentStatus ? user.getTenantAppDepartmentTemplateIds() : Collections.emptyList())
						.enabled(true)
						.joinTime(LocalDateTime.now())
						.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
						.accountId(user.getAccountId())
						.build()).collect(Collectors.toSet());
				insertUsers.addAll(tenantAppUserMongodbs);
			} catch (Exception e) {
				log.warn("e", e);
			}
			if (!insertUsers.isEmpty()) {
				mongoTemplate.insert(insertUsers, MongodbConstants.Collection.TENANT_APP_USER);
				insertUsers.forEach(newUser -> {
					try {
						// 发送创建用户消息
						rabbitTemplate.convertAndSend(
							cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
							cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, createdTenantAppMessage.getTenantId(), createdTenantAppMessage.getAppId()),
							objectMapper.writeValueAsString(
								CreatedTenantAppUserMessage.builder()
									.tenantId(createdTenantAppMessage.getTenantId())
									.appId(createdTenantAppMessage.getAppId())
									.userId(newUser.getUserId())
									.nickname(newUser.getNickname())
									.admin(newUser.getAdmin())
									.accountId(newUser.getAccountId())
									.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
									.eventTime(LocalDateTime.now())
									.build()
							),
							new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
						);
					} catch (JsonProcessingException e) {
						log.warn("e", e);
					}
				});
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_tenant_app_user_by_created_tenant_app] handler end: {}", createdTenantAppMessage.getTenantId());
		} catch (Exception e) {
			log.info("[create_tenant_app_user_by_created_tenant_app] handler error", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误，重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
	}

	public List<String> getRoleTemplateIds(Set<String> roleTemplateIds, List<String> roleIds2) {
		//删除
		return roleIds2.stream()
			.filter(roleTemplateIds::contains)
			.collect(Collectors.toList());
	}
}
