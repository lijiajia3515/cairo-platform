package io.github.lijiajia3515.cairo.auth.api.open.app_user;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_user.LogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_user.RegisterAppUserArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.verify_code.VerifyCodeBusiness;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
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


/**
 * [open/api] app_app_user service
 */
@Slf4j
@Validated
@Component
public class AppUserOpenApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final AccountCommonService accountCommonService;
	private final AppCommonService appCommonService;
	private final AppUserCommonService userCommonService;
	private final VerifyCodeService verifyCodeService;
	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;

	public AppUserOpenApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
								 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								 TransactionTemplate transactionTemplate, RabbitTemplate rabbitTemplate,
								 CairoRabbitmqTool cairoRabbitmqTool,
								 AccountCommonService accountCommonService,
								 AppCommonService appCommonService,
								 AppUserCommonService userCommonService,
								 VerifyCodeService verifyCodeService,
								 AuthProperties authProperties,
								 ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.accountCommonService = accountCommonService;
		this.appCommonService = appCommonService;
		this.userCommonService = userCommonService;
		this.verifyCodeService = verifyCodeService;
		this.authProperties = authProperties;
		this.objectMapper = objectMapper;
	}

	/**
	 * 注册应用用户
	 *
	 * @param args 参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "app_user:register_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void registerAppUser(@Validated RegisterAppUserArgs args) {
		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}
		// 验证应用状态
		appCommonService.checkAppId(readMongoTemplate, args.getAppId());
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
			log.debug("手机号已注册账号，继续创建应用用户：{} is {}", args.getPhoneNumber(), accountMongodb.getAccountId());
		} else {
			accountMongodb = transactionTemplate.execute(transactionStatus -> {
				try {
					String newAccountId = accountCommonService.getNewAccountId();
					AccountMongodb newAccountMongodb = AccountMongodb.builder()
						.accountId(newAccountId)
						.nickname(Optional.ofNullable(args.getNickname()).orElse(newAccountId))
						.avatarUrl(Optional.ofNullable(args.getAvatarUrl()).orElse(authProperties.getDefaultAvatarUrl()))
						.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).filter(x -> !x.trim().isEmpty()).orElse(null))
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
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}

		Criteria appCriteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(accountMongodb.getAccountId());
		Query appQuery = Query.query(appCriteria);
		boolean admin = readMongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

		AccountMongodb finalAccountMongodb = accountMongodb;
		AppUserMongodb user = transactionTemplate.execute(transactionStatus -> {
			try {
				appCommonService.checkAppId(mongoTemplate, args.getAppId());

				String userId = userCommonService.getNewAppUserId();

				AppUserMongodb app_userMongodb = AppUserMongodb.builder()
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
					.joinTime(LocalDateTime.now())
					.accountId(finalAccountMongodb.getAccountId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(userId)
						.updateUserId(userId)
						.build())
					.build();

				return mongoTemplate.insert(app_userMongodb, MongodbConstants.Collection.APP_USER);
			} catch (BusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("CreateAppUserFail", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注册应用用户失败");
			}
		});

		if (user != null) {
			// 发送创建应用用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER, args.getAppId()),
				objectMapper.writeValueAsString(
					CreatedAppUserMessage.builder()
						.appId(args.getAppId())
						.userId(user.getUserId())
						.nickname(user.getNickname())
						.admin(user.getAdmin())
						.accountId(user.getAccountId())
						.eventAppUserId(user.getUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
		log.debug("[app_user][registerAppUser] result -> {} ", user);
	}

	/**
	 * 注册应用用户
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "logoff_app_user", keys = {"#args.appId","#args.phoneNumber"})
	@SneakyThrows
	@BizLog(
		bizId = "app_user:register_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void logoffAppUser(@Validated LogoffAppUserArgs args) {
		appCommonService.checkAppId(readMongoTemplate, args.getAppId());
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

		AppUserMongodb logoffAppUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());

				Query accountQuery = Query.query(accountCriteria);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.ENABLED, AccountMongodb.FIELD.LOCKED, AccountMongodb.FIELD.LOGOFF_STATUS);
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb != null && accountMongodb.getAccountId() != null) {
					Criteria userCriteria = Criteria
						.where(AppUserMongodb.FIELD.APP_ID).is(args.getAppId())
						.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId());

					Query userQuery = Query.query(userCriteria);
					AppUserMongodb user = mongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
					if (user == null) {
						throw new ConflictBusinessException("无需注销（用户不存在）");
					}
					if (user.getAdmin() != null && user.getAdmin()) {
						throw new ConflictBusinessException("注销失败（系统管理员无法注销）");
					}

					if (user.getLogoffStatus() != null && !user.getLogoffStatus().equals(AppUserLogoffStatus.NO.getLogoffStatusValue())) {
						throw new ConflictBusinessException("注销失败（无需重复注销）");
					}
					Update userUpdate = new Update();
					userUpdate.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.PENDING.getLogoffStatusValue());
					userUpdate.set(AppUserMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plus(CairoAuthConstants.APP_USER_LOGOFF_PENDING_TIME));
					userUpdate.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
					userUpdate.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

					FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

					return mongoTemplate.findAndModify(userQuery, userUpdate, options, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				}
				return null;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注销失败");
			}
		});

		if (logoffAppUserMongodb == null) {
			// 发送注销应用用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_APP_USER, logoffAppUserMongodb.getAppId()),
				objectMapper.writeValueAsString(
					LogoffAppUserMessage.builder()
						.appId(logoffAppUserMongodb.getAppId())
						.userId(logoffAppUserMongodb.getUserId())
						.nickname(logoffAppUserMongodb.getNickname())
						.accountId(logoffAppUserMongodb.getAccountId())
						.eventAppUserId(logoffAppUserMongodb.getUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}

		log.debug("[app_user][logoffAppUser] result -> {} ", logoffAppUserMongodb);
	}
}
