package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.tenant_app_user;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user.TenantAppUserLogoffStatusInfo;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.UnlogoffTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user.ModifyMyTenantAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag.TenantAppUserTagCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.TENANT_APP_USER_LOGOFF_PENDING_TIME;


/**
 * [tenant_endpoint/api] tenant app user service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTenantAppUserApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final TenantAppDepartmentCommonService tenantAppDepartmentCommonService;
	private final TenantAppRoleCommonService tenantAppRoleCommonService;
	private final AccountCommonService accountCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final TenantAppUserTagCommonService tenantAppUserTagCommonService;
	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;

	public TenantAppUserTenantAppUserApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													TransactionTemplate transactionTemplate, RabbitTemplate rabbitTemplate,
													CairoRabbitmqTool cairoRabbitmqTool,
													TenantAppDepartmentCommonService tenantAppDepartmentCommonService,
													TenantAppRoleCommonService tenantAppRoleCommonService,
													AccountCommonService accountCommonService,
													TenantAppUserCommonService tenantAppUserCommonService,
													TenantAppUserTagCommonService tenantAppUserTagCommonService,
													AuthProperties authProperties,
													ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.tenantAppDepartmentCommonService = tenantAppDepartmentCommonService;
		this.tenantAppRoleCommonService = tenantAppRoleCommonService;
		this.tenantAppUserTagCommonService = tenantAppUserTagCommonService;
		this.accountCommonService = accountCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.authProperties = authProperties;
		this.objectMapper = objectMapper;
	}

	@NewSpan
	@Lock4j(name = "modify_my_tenant_app_user_info", keys = {"#tenantId", "#appId", "#userId"})
	@BizLog(
		bizId = "tenant_app_user:modify_my_tenant_app_user_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyMyTenantAppUserInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated ModifyMyTenantAppUserInfoArgs args) {
		TenantAppUserMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId));

				Update update = Update.update(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				Optional.ofNullable(args.getNickname()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.NICKNAME, x));
				Optional.ofNullable(args.getPhoneNumber()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.PHONE_NUMBER, x));
				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (Exception e) {
				log.debug("modifyUserInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改用户信息失败");
			}
		});
		if (userMongodb == null) {
			throw new ConflictBusinessException("修改用户信息失败");
		}
	}

	/**
	 * 获取我的注销状态
	 *
	 * @param appId  appId
	 * @param userId userId
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:get_my_tenant_app_user_logoff_status",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	public TenantAppUserLogoffStatusInfo getMyTenantAppUserLogoffStatus(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query query = Query.query(criteria);
		TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		TenantAppUserLogoffStatusInfo.TenantAppUserLogoffStatusInfoBuilder<?, ?> builder = TenantAppUserLogoffStatusInfo.builder()
			.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue());
		if (userMongodb != null) {
			builder
				.logoffStatus(Optional.ofNullable(userMongodb.getLogoffStatus()).orElse(TenantAppUserLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(userMongodb.getLogoffPendingTime());
		}
		return builder.build();
	}

	/**
	 * 获取预注销信息
	 *
	 * @param appId  appId
	 * @param userId userId
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:get_my_tenant_app_user_pre_logoff_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	public PreLogoffInfo getMyTenantAppUserPreLogoffInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query query = Query.query(criteria);
		TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		PreLogoffInfo.PreLogoffInfoBuilder<?, ?> builder = PreLogoffInfo.builder()
			.nickname(userId)
			.day((int) CairoAuthConstants.TENANT_APP_USER_LOGOFF_PENDING_TIME.toDays())
			.logoffPendingTime(LocalDateTime.now().plus(CairoAuthConstants.TENANT_APP_USER_LOGOFF_PENDING_TIME));
		if (userMongodb != null) {
			builder.nickname(userMongodb.getNickname());
		}
		return builder.build();
	}

	/**
	 * 注销当前用户
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param userId   userId
	 */
	@NewSpan
	@Lock4j(name = "logoff_my_tenant_app_user", keys = {"#tenantId", "#appId", "#userId"})
	@BizLog(
		bizId = "tenant_app_user:logoff_my_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	@SneakyThrows
	public void logoffMyTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		TenantAppUserMongodb loggedOffUser = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

				Query query = Query.query(criteria);
				TenantAppUserMongodb user = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

				if (user == null) {
					throw new ConflictBusinessException("注销失败（用户不存在）");
				}
				if (user.getAdmin() != null && user.getAdmin()) {
					throw new ConflictBusinessException("注销失败（系统管理员无法注销）");
				}
				if (user.getLogoffStatus() != null && !user.getLogoffStatus().equals(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())) {
					throw new ConflictBusinessException("注销失败（无需重复注销）");
				}

				Update update = new Update();
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.PENDING.getLogoffStatusValue());
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plus(TENANT_APP_USER_LOGOFF_PENDING_TIME));
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				return mongoTemplate.findAndModify(query, update, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffMyTenantAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注销用户失败");
			}
		});
		if (loggedOffUser != null) {
			// 发送注销用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_TENANT_APP_USER, tenantId, appId),
				objectMapper.writeValueAsString(
					LogoffTenantAppUserMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.userId(loggedOffUser.getUserId())
						.nickname(loggedOffUser.getNickname())
						.accountId(loggedOffUser.getAccountId())
						.eventUserId(loggedOffUser.getUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * 取消注销当前用户
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param userId   userId
	 */
	@NewSpan
	@Lock4j(name = "unlogoff_my_tenant_app_user", keys = {"#tenantId", "#appId", "#userId"})
	@BizLog(
		bizId = "tenant_app_user:unlogoff_my_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	@SneakyThrows
	public void unlogoffMyTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		TenantAppUserMongodb modified = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

				Query query = Query.query(criteria);
				TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("取消注销失败（用户不存在）");
				}

				if (userMongodb.getLogoffStatus() != null && userMongodb.getLogoffStatus().equals(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())) {
					throw new ConflictBusinessException("取消注销失败（未申请注销流程）");
				}

				Update update = new Update();
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.NO.getLogoffStatusValue());
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_PENDING_TIME, null);
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				return mongoTemplate.findAndModify(query, update, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("unlogoffMyAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("取消注销失败");
			}
		});
		if (modified != null) {
			// 发送取消注销用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.UNLOGOFF_TENANT_APP_USER, tenantId, appId),
				objectMapper.writeValueAsString(
					UnlogoffTenantAppUserMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.userId(modified.getUserId())
						.nickname(modified.getNickname())
						.accountId(modified.getAccountId())
						.eventUserId(modified.getUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

}
