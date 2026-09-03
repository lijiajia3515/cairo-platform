package io.github.lijiajia3515.cairo.auth.api.open.account;


import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.GetLoginAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.LogoffAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.OpenAccount;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.RegisterAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ResetPasswordPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountEmailArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountEmailResp;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountPhoneNumberResp;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountUsernameArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.account.ValidAccountUsernameResp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountBusiness;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.LogoffAccountMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.verify_code.VerifyCodeBusiness;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import groovy.lang.Tuple;
import groovy.lang.Tuple2;
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

/**
 * [open/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountOpenApiService {
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AccountCommonService accountCommonService;
	private final VerifyCodeService verifyCodeService;
	private final ObjectMapper objectMapper;
	private final AuthProperties authProperties;

	public AccountOpenApiService(
		RabbitTemplate rabbitTemplate,
		CairoRabbitmqTool cairoRabbitmqTool,
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
		AccountCommonService accountCommonService,
		VerifyCodeService verifyCodeService,
		TransactionTemplate transactionTemplate,
		ObjectMapper objectMapper, AuthProperties authProperties) {
		this.accountCommonService = accountCommonService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.verifyCodeService = verifyCodeService;
		this.objectMapper = objectMapper;
		this.authProperties = authProperties;
	}

	/**
	 * 验证账号用户名
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:valid_account_username",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	// 基于请求IP进行过滤
	// @Lock4j(name = "unlogoff_account", keys = {"#args.accountId"})
	public ValidAccountUsernameResp validAccountUsername(@Validated ValidAccountUsernameArgs args) {
		// 匹配格式
		boolean formatIllegal = !AccountCommonService.validUsername(args.getUsername());
		boolean exists = false;

		// 格式正确数据库查重
		if (!formatIllegal) {
			Criteria criteria = Criteria.where(AccountMongodb.FIELD.USERNAME).is(args.getUsername());
			Query query = Query.query(criteria);
			exists = readMongoTemplate.exists(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		}


		// 是否可用
		boolean success = !formatIllegal && !exists;

		return ValidAccountUsernameResp.builder().success(success).formatIllegal(formatIllegal).exists(exists).build();
	}

	/**
	 * 验证账号手机号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:valid_account_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	// 基于请求IP进行过滤
	// @Lock4j(name = "unlogoff_account", keys = {"#args.accountId"})
	public ValidAccountPhoneNumberResp validAccountPhoneNumber(@Validated ValidAccountPhoneNumberArgs args) {
		// 匹配格式
		boolean formatIllegal = !AccountCommonService.validPhoneNumber(args.getPhoneNumber());
		boolean exists = false;

		// 格式正确数据库查重
		if (!formatIllegal) {
			Criteria criteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());
			Query query = Query.query(criteria);
			exists = readMongoTemplate.exists(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		}

		// 是否可用
		boolean success = !formatIllegal && !exists;

		return ValidAccountPhoneNumberResp.builder().success(success).formatIllegal(formatIllegal).exists(exists).build();
	}

	/**
	 * 验证账号邮箱
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:valid_account_email",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	// 基于请求IP进行过滤
	// @Lock4j(name = "unlogoff_account", keys = {"#args.accountId"})
	public ValidAccountEmailResp validAccountEmail(@Validated ValidAccountEmailArgs args) {
		Criteria criteria = Criteria.where(AccountMongodb.FIELD.EMAIL).is(args.getEmail());
		Query query = Query.query(criteria);
		boolean exists = readMongoTemplate.exists(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (exists) {
			return ValidAccountEmailResp.builder().success(false).exists(true).build();
		} else {
			return ValidAccountEmailResp.builder().success(true).exists(false).build();
		}
	}

	/**
	 * 普通注册
	 *
	 * @param args 注册参数
	 */
	@NewSpan
	@BizLog(
		bizId = "account:register_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	// 基于请求IP进行过滤
	// @Lock4j(name = "unlogoff_account", keys = {"#args.accountId"})
	public void registerAccount(@Validated RegisterAccountArgs args) {
		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		// 验证真实性
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

		// 验证重复
		if (existPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException(String.format("手机号已存在: %s", args.getPhoneNumber()), AccountBusiness.PHONE_NUMBER_EXISTS);
		}

		String newAccountId = accountCommonService.getNewAccountId();
		Tuple2<AccountMongodb, String> tuple = transactionTemplate.execute(status -> {
			try {
				AccountMongodb accountMongodb = AccountMongodb.builder()
					.accountId(newAccountId)
					.nickname(args.getNickname())
					.avatarUrl(authProperties.getDefaultAvatarUrl())
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
				mongoTemplate.insert(accountMongodb, MongodbConstants.Collection.ACCOUNT);
				String rawPassword;
				String encodePassword;

				// 设置密码
				if (args.getPassword() != null) {
					rawPassword = args.getPassword();
					encodePassword = accountCommonService.getPasswordEncoder().encode(rawPassword);
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
					mongoTemplate.insert(accountPasswordMongodb, MongodbConstants.Collection.ACCOUNT_PASSWORD);
					return Tuple.tuple(accountMongodb, rawPassword);
				}

				return Tuple.tuple(accountMongodb, null);
			} catch (Exception e) {
				log.debug("registerAccount", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注册账号失败");
			}
		});

		if (tuple == null) {
			throw new ConflictBusinessException("注册账号失败");
		}

		AccountMongodb md = tuple.getV1();
		String rawPassword = tuple.getV2();

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
			objectMapper.writeValueAsString(
				CreatedAccountMessage.builder()
					.accountId(md.getAccountId())
					.nickname(md.getNickname())
					.phoneNumber(md.getPhoneNumber())
					.username(md.getUsername())
					.password(rawPassword)
					.eventAccountId(md.getAccountId())
					.eventTime(LocalDateTime.now())
					.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 注销账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:logoff_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	// @Lock4j(name = "open_logoff_account", keys = {"#args.phoneNumber"})
	public void logoffAccount(@Validated LogoffAccountArgs args) {
		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		// 验证手机号真实性
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

		AccountMongodb logoffAccountMongodb = transactionTemplate.execute(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria
					.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber())
				);

				Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.PENDING.getLogoffStatusValue());
				accountUpdate.set(AccountMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plus(CairoAuthConstants.ACCOUNT_LOGOFF_PENDING_TIME));
				accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
				accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				AccountMongodb modifiedAccountMongodb = mongoTemplate.findAndModify(accountQuery, accountUpdate, options, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (modifiedAccountMongodb == null) {
					throw new ConflictBusinessException("注销账号失败，账号不存在");
				}
				return modifiedAccountMongodb;
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffAccount", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注销账号失败");
			}
		});

		if (logoffAccountMongodb != null) {
			// 发送账号删除消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.LOGOFF_ACCOUNT),
				objectMapper.writeValueAsString(LogoffAccountMessage.builder()
					.accountId(logoffAccountMongodb.getAccountId())
					.nickname(logoffAccountMongodb.getNickname())
					.avatarUrl(logoffAccountMongodb.getAvatarUrl())
					.phoneNumber(logoffAccountMongodb.getPhoneNumber())
					.email(logoffAccountMongodb.getEmail())
					.username(logoffAccountMongodb.getUsername())
					.joinTime(logoffAccountMongodb.getJoinTime())
					.eventAccountId(CairoSecurityContextHolder.getAccountId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * 根据手机号重置密码
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:reset_account_password_by_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@Lock4j(name = "reset_account_password_by_phone_number", keys = {"#args.phoneNumber"})
	public void resetAccountPasswordByPhoneNumber(@Validated ResetPasswordPhoneNumberArgs args) {
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		VerifyCodeStat verifyCodeStat = verifyCodeService.verify(VerifyVerifyCodeArgs.builder()
			.bizCode(CairoAuthVerifyCodeConstants.AUTH)
			.target(args.getPhoneNumber())
			.maxFailCount(3)
			.verifyCode(args.getVerifyCode())
			.build());
		if (VerifyCodeStat.FAILED.equals(verifyCodeStat)) {
			throw new ConflictBusinessException("验证码错误", VerifyCodeBusiness.BAD);
		}

		if (VerifyCodeStat.EXPIRED.equals(verifyCodeStat)) {
			throw new ConflictBusinessException("验证码失效", VerifyCodeBusiness.EXPIRED);
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());
				Query accountQuery = Query.query(accountCriteria).limit(1);
				accountQuery.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.LOCKED, AccountMongodb.FIELD.LOCKED_TIME);

				AccountMongodb account = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (account == null) {
					throw new ConflictBusinessException("账号不存在", AccountBusiness.NOT_FOUND);
				}
				if (account.isLocked()) {
					Criteria unlockAccountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId());
					Query unlockAccountQuery = Query.query(unlockAccountCriteria);
					Update unlockAccountUpdate = Update.update(AccountMongodb.FIELD.LOCKED, false);
					unlockAccountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, account.getAccountId());
					unlockAccountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

					UpdateResult updateResult = mongoTemplate.updateFirst(unlockAccountQuery, unlockAccountUpdate, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
					log.debug("account unlock: {}", updateResult);
				}
				String encodePassword = accountCommonService.getPasswordEncoder().encode(args.getPassword());
				Criteria accountPasswordCriteria = Criteria
					.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(account.getAccountId())
					.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());

				Update accountPasswordUpdate = Update.update(AccountPasswordMongodb.FIELD.PASSWORD, encodePassword);
				accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, account.getAccountId());
				accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.update(AccountPasswordMongodb.class)
					.inCollection(MongodbConstants.Collection.ACCOUNT_PASSWORD)
					.matching(accountPasswordCriteria)
					.apply(accountPasswordUpdate)
					.upsert();

				if (updateResult.getModifiedCount() < 1L) {
					throw new ConflictBusinessException("重置账号密码失败");
				}
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("resetPasswordByPhoneNumber", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("重置账号密码失败");
			}
		});

	}

	/**
	 * 注销账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:logoff_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public OpenAccount getLoginAccount(@Validated GetLoginAccountArgs args) {
		Criteria criteria = new Criteria();
		if ("PhoneNumber".equals(args.getType())) {
			if (args.getPhoneNumber() == null || !AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
				throw new ConflictBusinessException("手机号格式错误");
			}
			criteria.and(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber());
		} else if ("Username".equals(args.getType())) {
			if (args.getUsername() == null || !AccountCommonService.validUsername(args.getUsername())) {
				throw new ConflictBusinessException("用户名格式错误");
			}
			criteria.and(AccountMongodb.FIELD.USERNAME).is(args.getUsername());
		} else if ("Email".equals(args.getType())) {
			if (args.getEmail() == null) {
				throw new ConflictBusinessException("邮箱格式错误");
			}
			criteria.and(AccountMongodb.FIELD.EMAIL).is(args.getEmail());
		} else {
			throw new ConflictBusinessException("找回类型错误");
		}
		Query query = Query.query(criteria);
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb == null) {
			return null;
		}
		return OpenAccount.builder()
			.accountId(accountMongodb.getAccountId())
			.nickname(accountMongodb.getNickname())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.email(accountMongodb.getEmail())
			.username(accountMongodb.getUsername())
			.joinTime(accountMongodb.getJoinTime())
			.build();
	}

	/**
	 * 检测手机号是否存在
	 *
	 * @param phoneNumber 手机号
	 * @return 是否存在
	 */
	@NewSpan
	public boolean existPhoneNumber(String phoneNumber) {
		final Criteria criteria = Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(phoneNumber);
		return readMongoTemplate.exists(Query.query(criteria), MongodbConstants.Collection.ACCOUNT);
	}
}
