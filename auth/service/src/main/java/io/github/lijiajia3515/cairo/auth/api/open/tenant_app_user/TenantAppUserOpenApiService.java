package io.github.lijiajia3515.cairo.auth.api.open.tenant_app_user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant_app_user.LogoffTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant_app_user.RegisterTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.verify_code.VerifyCodeBusiness;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
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
import java.util.Collections;
import java.util.Optional;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.TENANT_APP_USER_LOGOFF_PENDING_TIME;


/**
 * [open/api] tenant app user service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserOpenApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final AccountCommonService accountCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final VerifyCodeService verifyCodeService;
	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;

	public TenantAppUserOpenApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									   TransactionTemplate transactionTemplate,
									   RabbitTemplate rabbitTemplate,
									   CairoRabbitmqTool cairoRabbitmqTool,
									   AccountCommonService accountCommonService,
									   TenantAppUserCommonService tenantAppUserCommonService,
									   VerifyCodeService verifyCodeService,
									   AuthProperties authProperties,
									   ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.accountCommonService = accountCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.verifyCodeService = verifyCodeService;
		this.authProperties = authProperties;
		this.objectMapper = objectMapper;
	}

	/**
	 * 注册用户
	 *
	 * @param args 参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user:register_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void registerTenantAppUser(@Validated RegisterTenantAppUserArgs args) {
		// 验证应用，企业状态
		checkStatus(args.getTenantId(), args.getAppId());

		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}


		// 验证身份
		VerifyCodeStat verifyCodeStat = verifyCodeService.verify(
			VerifyVerifyCodeArgs.builder()
				.bizCode(CairoAuthVerifyCodeConstants.AUTH)
				.target(args.getPhoneNumber())
				.maxFailCount(3)
				.verifyCode(args.getVerifyCode())
				.build()
		);

		if (!VerifyCodeStat.SUCCESS.equals(verifyCodeStat)) {
			throw new ConflictBusinessException("验证码错误", VerifyCodeBusiness.BAD);
		}

		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());
		AccountMongodb accountMongodb = mongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb != null) {
			log.debug("手机号已注册账号，继续创建用户：{} is {}", args.getPhoneNumber(), accountMongodb.getAccountId());
		} else {
			accountMongodb = transactionTemplate.execute(transactionStatus -> {
				try {
					String newAccountId = accountCommonService.getNewAccountId();
					AccountMongodb newAccountMongodb = AccountMongodb.builder()
						.accountId(newAccountId)
						.nickname(Optional.ofNullable(args.getNickname()).orElse(newAccountId))
						.avatarUrl(Optional.ofNullable(args.getAvatarUrl()).orElse(authProperties.getDefaultAvatarUrl()))
						.phoneNumber(args.getPhoneNumber())
						.enabled(true)
						.locked(false)
						.joinTime(LocalDateTime.now())
						.logoffStatus(AccountLogoffStatus.NO.getLogoffStatusValue())
						.metadata(AccountMetadataMongodb.builder()
							.createAccountId(newAccountId)
							.updateAccountId(newAccountId)
							.build()
						)
						.build();
					String encodePassword = accountCommonService.getPasswordEncoder().encode(args.getPassword());
					AccountPasswordMongodb accountPasswordMongodb = AccountPasswordMongodb.builder()
						.accountId(newAccountId)
						.type(PasswordType.PASSWORD.getType())
						.password(encodePassword)
						.metadata(AccountMetadataMongodb.builder()
							.createAccountId(newAccountId)
							.updateAccountId(newAccountId)
							.build()
						)
						.build();
					mongoTemplate.insert(newAccountMongodb, MongodbConstants.Collection.ACCOUNT);
					mongoTemplate.insert(accountPasswordMongodb, MongodbConstants.Collection.ACCOUNT_PASSWORD);
					return newAccountMongodb;
				} catch (Exception e) {
					transactionStatus.setRollbackOnly();
					log.info("CreateAccountFail", e);
					throw new ConflictBusinessException("创建账号失败");
				}
			});
			if (accountMongodb == null) {
				throw new ConflictBusinessException("创建账号失败");
			}
			// 发送创建账号消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
				objectMapper.writeValueAsString(
					CreatedAccountMessage.builder()
						.accountId(accountMongodb.getAccountId())
						.nickname(accountMongodb.getNickname())
						.phoneNumber(accountMongodb.getPhoneNumber())
						.username(accountMongodb.getUsername())
						.password(args.getPassword())
						.eventAccountId(accountMongodb.getAccountId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

		Criteria tenantAppAccountCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(args.getTenantId())
			.and(TenantAppMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(accountMongodb.getAccountId());
		Query tenantAppAccountQuery = Query.query(tenantAppAccountCriteria);
		boolean admin = readMongoTemplate.exists(tenantAppAccountQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

		AccountMongodb finalAccountMongodb = accountMongodb;
		TenantAppUserMongodb user = transactionTemplate.execute(transactionStatus -> {
			try {

				String userId = tenantAppUserCommonService.getNewUserId();

				TenantAppUserMongodb userMongodb = TenantAppUserMongodb.builder()
					.tenantId(args.getTenantId())
					.appId(args.getAppId())
					.userId(userId)
					.nickname(Optional.ofNullable(args.getNickname()).orElse(finalAccountMongodb.getNickname()))
					.phoneNumber(finalAccountMongodb.getPhoneNumber())
					.admin(admin)
					.roleIds(Collections.emptyList())
					.position(null)
					.mainDepartmentId(null)
					.departmentIds(Collections.emptyList())
					.tagIds(Collections.emptyList())
					.enabled(true)
					.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
					.joinTime(LocalDateTime.now())
					.accountId(finalAccountMongodb.getAccountId())
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(userId)
						.updateUserId(userId)
						.build())
					.build();

				return mongoTemplate.insert(userMongodb, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("CreateTenantAppUserFail", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注册企业应用级用户失败");
			}
		});

		if (user == null) {
			throw new ConflictBusinessException("注册用户失败");
		}

		// 发送创建用户消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, args.getTenantId(), args.getAppId()),
			objectMapper.writeValueAsString(
				CreatedTenantAppUserMessage.builder()
					.tenantId(args.getTenantId())
					.appId(args.getAppId())
					.userId(user.getUserId())
					.nickname(user.getNickname())
					.admin(user.getAdmin())
					.accountId(user.getAccountId())
					.eventUserId(user.getUserId())
					.eventTime(LocalDateTime.now())
					.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);

		log.debug("[tenant_app_user][registerTenantAppUser] result -> {} ", user);
	}

	/**
	 * 注册用户
	 *
	 * @param args 参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user:register_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void logoffTenantAppUser(@Validated LogoffTenantAppUserArgs args) {
		// 验证应用，企业状态
		checkStatus(args.getTenantId(), args.getAppId());

		// 验证身份
		VerifyCodeStat verifyCodeStat = verifyCodeService.verify(
			VerifyVerifyCodeArgs.builder()
				.bizCode(CairoAuthVerifyCodeConstants.AUTH)
				.target(args.getPhoneNumber())
				.maxFailCount(3)
				.verifyCode(args.getVerifyCode())
				.build()
		);

		if (!VerifyCodeStat.SUCCESS.equals(verifyCodeStat)) {
			throw new ConflictBusinessException("验证码错误", VerifyCodeBusiness.BAD);
		}

		TenantAppUserMongodb logoffUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());

				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.ENABLED, AccountMongodb.FIELD.LOCKED, AccountMongodb.FIELD.LOGOFF_STATUS);
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb != null && accountMongodb.getAccountId() != null) {
					Criteria userCriteria = Criteria
						.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(args.getTenantId())
						.and(TenantAppUserMongodb.FIELD.APP_ID).is(args.getAppId())
						.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId());

					Query userQuery = Query.query(userCriteria);
					TenantAppUserMongodb user = mongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
					if (user == null) {
						throw new ConflictBusinessException("无需注销（用户不存在）");
					}
					if (user.getAdmin() != null && user.getAdmin()) {
						throw new ConflictBusinessException("注销失败（系统管理员无法注销）");
					}

					if (user.getLogoffStatus() != null && !user.getLogoffStatus().equals(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())) {
						throw new ConflictBusinessException("注销失败（无需重复注销）");
					}
					Update userUpdate = new Update();
					userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.PENDING);
					userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plus(TENANT_APP_USER_LOGOFF_PENDING_TIME));
					userUpdate.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
					userUpdate.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

					FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

					return mongoTemplate.findAndModify(userQuery, userUpdate, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				}
				return null;
			} catch (Exception e) {
				log.debug("logoffTenantAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注销失败");
			}
		});

		log.debug("[tenant_app_user][logoffTenantAppUser] result -> {} ", logoffUserMongodb);
		if (logoffUserMongodb != null) {
			// 发送注销用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_TENANT_APP_USER, logoffUserMongodb.getTenantId(), logoffUserMongodb.getAppId()),
				objectMapper.writeValueAsString(
					LogoffTenantAppUserMessage.builder()
						.tenantId(logoffUserMongodb.getTenantId())
						.appId(logoffUserMongodb.getAppId())
						.userId(logoffUserMongodb.getUserId())
						.nickname(logoffUserMongodb.getNickname())
						.accountId(logoffUserMongodb.getAccountId())
						.eventUserId(logoffUserMongodb.getUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * 检查状态
	 *
	 * @param tenantId 企业ID
	 * @param appId    应用ID
	 */
	public void checkStatus(String tenantId, String appId) {

		// 验证应用状态
		Criteria appCriteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).is(appId)
			.and(AppMongodb.FIELD.ENABLED).is(true);
		Query appQuery = Query.query(appCriteria);
		if (!mongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP)) {
			throw new ConflictBusinessException("应用状态异常");
		}

		// 验证企业状态
		Criteria tenantCriteria = Criteria
			.where(TenantMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantMongodb.FIELD.ENABLED).is(true);
		Query tenantQuery = Query.query(tenantCriteria);
		if (!mongoTemplate.exists(tenantQuery, TenantMongodb.class, MongodbConstants.Collection.TENANT)) {
			throw new ConflictBusinessException("企业状态异常");
		}

		// 验证企业应用状态
		Criteria tenantAppCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppMongodb.FIELD.ENABLED).is(true);
		Query tenantAppQuery = Query.query(tenantAppCriteria);
		if (!mongoTemplate.exists(tenantAppQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP)) {
			throw new ConflictBusinessException("企业应用状态异常");
		}
	}
}
