package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.account;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.CreateAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.DeleteAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.LogoffAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountLockArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ModifyAccountStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.ResetAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account.UnlogoffAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.MetadataAccount;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.DeletedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.LogoffAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.ModifiedAccountPasswordMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.UnlogoffAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountConverter;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [endpoint_user/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountCairoWebManageApiService {

	/**
	 * 默认密码
	 */
	public static final String PASSWORD_DEFAULT = "123456";

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	private final AccountCommonService accountCommonService;
	private final CairoAuthAccountService cairoAuthAccountService;
	private final AccountAuthorizationCommonService accountAuthorizationCommonService;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	private final AuthProperties authProperties;
	private final FileCommonService fileCommonService;

	public AccountCairoWebManageApiService(
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		TransactionTemplate transactionTemplate,
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
		RedisTemplate<String, Object> redisTemplate,
		AccountCommonService accountCommonService, CairoAuthAccountService cairoAuthAccountService,
		AccountAuthorizationCommonService accountAuthorizationCommonService,
		RabbitTemplate rabbitTemplate,
		CairoRabbitmqTool cairoRabbitmqTool,
		ObjectMapper objectMapper,
		AuthProperties authProperties,
		FileCommonService fileCommonService) {
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.accountCommonService = accountCommonService;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.accountAuthorizationCommonService = accountAuthorizationCommonService;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.authProperties = authProperties;
		this.fileCommonService = fileCommonService;
	}

	/**
	 * 查询账号列表
	 *
	 * @param args 参数
	 * @return 账号列表
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataAccount> getAccountList(@Validated GetAccountPageListArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.orOperator(
			Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).regex(x),
			Criteria.where(AccountMongodb.FIELD.NICKNAME).regex(x),
			Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).regex(x),
			Criteria.where(AccountMongodb.FIELD.EMAIL).regex(x),
			Criteria.where(AccountMongodb.FIELD.USERNAME).regex(x)
		));
		if (args.getEnabled() != null) {
			criteria.and(AccountMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		if (args.getLocked() != null) {
			criteria.and(AccountMongodb.FIELD.LOCKED).is(args.getLocked());
		}
		if (args.getLogoffStatuses() != null && !args.getLogoffStatuses().isEmpty()) {
			criteria.and(AccountMongodb.FIELD.LOGOFF_STATUS).in(args.getLogoffStatuses());
		}
		Query query = Query.query(criteria);

		List<AccountMongodb> records = readMongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		List<AccountMetadataMongodb> metadataMongodbList = records.stream().map(AccountMongodb::getMetadata).collect(Collectors.toList());
		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(metadataMongodbList);
		Map<String, Account> metadataUserMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);
		return records.stream()
			.map(x -> AccountConverter.convertMetadataAccount(x, metadataUserMap))
			.collect(Collectors.toList());
	}

	/**
	 * 获取账号 分页模式
	 *
	 * @param args 分页参数
	 * @return 账号信息 分页模型
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataAccount> getAccountPageList(@Validated GetAccountPageListArgs args) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(args.getKeyword()).ifPresent(x -> criteria.orOperator(
			Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).regex(x),
			Criteria.where(AccountMongodb.FIELD.NICKNAME).regex(x),
			Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).regex(x),
			Criteria.where(AccountMongodb.FIELD.EMAIL).regex(x),
			Criteria.where(AccountMongodb.FIELD.USERNAME).regex(x)
		));

		if (args.getEnabled() != null) {
			criteria.and(AccountMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		if (args.getLocked() != null) {
			criteria.and(AccountMongodb.FIELD.LOCKED).is(args.getLocked());
		}
		if (args.getLogoffStatuses() != null && !args.getLogoffStatuses().isEmpty()) {
			criteria.and(AccountMongodb.FIELD.LOGOFF_STATUS).in(args.getLogoffStatuses());
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.ACCOUNT);

		query.with(args.pageable()).with(Sort.by(Sort.Order.desc(AccountMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<AccountMongodb> records = mongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		List<AccountMetadataMongodb> metadataMongodbList = records.stream().map(AccountMongodb::getMetadata).collect(Collectors.toList());
		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(metadataMongodbList);
		Map<String, Account> metadataUserMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		List<MetadataAccount> list = records.stream()
			.map(x -> AccountConverter.convertMetadataAccount(x, metadataUserMap))
			.collect(Collectors.toList());
		return new Page<>(args, list, total);
	}

	/**
	 * 获取账号信息根据账号id
	 *
	 * @param accountId accountId
	 * @return 账号信息
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	public MetadataAccount getAccountInfo(@Valid @NotNull String accountId) {
		Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query query = Query.query(criteria);

		AccountMongodb account = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) return null;

		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(Collections.singleton(account.getMetadata()));
		Map<String, Account> metadataUserMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);
		return AccountConverter.convertMetadataAccount(account, metadataUserMap);
	}

	/**
	 * 创建账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:create_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void createAccount(@Validated CreateAccountArgs args) {
		if (args.getUsername() == null && args.getPhoneNumber() == null && args.getEmail() == null) {
			throw new ConflictBusinessException("登录名,手机号和邮箱最少填一项");
		}

		// 验证用户名格式
		if (args.getUsername() != null && !args.getUsername().trim().isBlank() && !AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("用户名格式错误");
		}

		// 验证手机号格式
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank() && !AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		String password = Optional.ofNullable(args.getPassword()).orElse(PASSWORD_DEFAULT);

		String accountId = accountCommonService.getNewAccountId();
		CreatedAccountMessage createdAccountMessage = transactionTemplate.execute(transactionStatus -> {
			try {
				AccountMongodb newAccountMongodb = AccountMongodb.builder()
					.accountId(accountId)
					.nickname(
						Optional.ofNullable(args.getNickname())
							.or(() -> Optional.ofNullable(args.getPhoneNumber()))
							.or(() -> Optional.ofNullable(args.getEmail()))
							.or(() -> Optional.ofNullable(args.getUsername()))
							.orElse("默认昵称")
					)
					.avatarUrl(Optional.ofNullable(args.getAvatarUrl()).orElse(authProperties.getDefaultAvatarUrl()))
					.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).filter(x -> !x.trim().isEmpty()).orElse(null))
					.username(Optional.ofNullable(args.getUsername()).filter(x -> !x.trim().isEmpty()).orElse(null))
					.email(Optional.ofNullable(args.getEmail()).filter(x -> !x.trim().isEmpty()).orElse(null))
					.enabled(true)
					.locked(false)
					.joinTime(LocalDateTime.now())
					.logoffStatus(AccountLogoffStatus.NO.getLogoffStatusValue())
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.build()
					)
					.build();
				AccountPasswordMongodb newAccountPasswordMongodb = AccountPasswordMongodb.builder()
					.accountId(accountId)
					.type(PasswordType.PASSWORD.getType())
					.password(accountCommonService.getPasswordEncoder().encode(password))
					.metadata(AccountMetadataMongodb.builder()
						.createAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.updateAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.build()
					)
					.build();
				mongoTemplate.insert(newAccountMongodb, MongodbConstants.Collection.ACCOUNT);
				mongoTemplate.insert(newAccountPasswordMongodb, MongodbConstants.Collection.ACCOUNT_PASSWORD);
				return CreatedAccountMessage.builder()
					.accountId(accountId)
					.nickname(newAccountMongodb.getNickname())
					.phoneNumber(newAccountMongodb.getPhoneNumber())
					.username(newAccountMongodb.getUsername())
					.password(password)
					.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
					.eventTime(LocalDateTime.now())
					.build();
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("resetPassword", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("创建账号失败");
			}
		});
		if (createdAccountMessage != null) {
			// 发送已创建账号消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
				objectMapper.writeValueAsString(createdAccountMessage),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}

	}


	/**
	 * 管理员重置用户密码
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:reset_account_password",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "reset_account_password", keys = {"#args.accountId"})
	public void resetAccountPassword(@Validated ResetAccountPasswordArgs args) {
		String password = Optional.ofNullable(args.getPassword()).orElse(PASSWORD_DEFAULT);
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));

				Update accountUpdate = new Update();
				accountUpdate.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				accountUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult accountUpdateResult = mongoTemplate.updateFirst(accountQuery, accountUpdate, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);

				Query accountPasswordQuery = Query.query(Criteria
					.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId())
					.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType())
				);

				Update accountPasswordUpdate = Update.update(AccountPasswordMongodb.FIELD.PASSWORD, accountCommonService.getPasswordEncoder().encode(password));
				accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_COUNT, 0);
				accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.PASSWORD_FAIL_TIME, null);
				accountPasswordUpdate.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult upsert = mongoTemplate.upsert(accountPasswordQuery, accountPasswordUpdate, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
				if (upsert.getModifiedCount() < 1L) {
					throw new ConflictBusinessException("密码重置失败");
				}

			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("resetPassword", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("密码重置失败");
			}
		});

		// 发送修改账号消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.MODIFIED_ACCOUNT_PASSWORD),
			objectMapper.writeValueAsString(ModifiedAccountPasswordMessage.builder()
				.accountId(args.getAccountId())
				.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
				.eventTime(LocalDateTime.now())
				.build()),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
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
	@Lock4j(name = "logoff_account", keys = {"#args.accountId"})
	public void logoffAccount(@Validated LogoffAccountArgs args) {
		AccountMongodb logoffAccount = transactionTemplate.execute(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null) {
					throw new ConflictBusinessException("注销失败（账号不存在）");
				}
				if (accountMongodb.getLogoffStatus() != null && !accountMongodb.getLogoffStatus().equals(AccountLogoffStatus.NO.getLogoffStatusValue())) {
					throw new ConflictBusinessException("注销失败（账号已在注销中）");
				}

				Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.PENDING.getLogoffStatusValue());
				accountUpdate.set(AccountMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plusDays(3));
				accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(accountQuery, accountUpdate, options, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffAccount", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注销账号失败");
			}
		});
		if (logoffAccount != null) {
			cairoAuthAccountService.removeAccountCache(args.getAccountId());
			// 发送账号注销成功消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.LOGOFF_ACCOUNT),
				objectMapper.writeValueAsString(LogoffAccountMessage.builder()
					.accountId(logoffAccount.getAccountId())
					.nickname(logoffAccount.getNickname())
					.avatarUrl(logoffAccount.getAvatarUrl())
					.phoneNumber(logoffAccount.getPhoneNumber())
					.email(logoffAccount.getEmail())
					.username(logoffAccount.getUsername())
					.joinTime(logoffAccount.getJoinTime())
					.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * 注销账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:unlogoff_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "unlogoff_account", keys = {"#args.accountId"})
	public void unlogoffAccount(@Validated UnlogoffAccountArgs args) {
		AccountMongodb modified = transactionTemplate.execute(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null) {
					throw new ConflictBusinessException("取消注销失败（账号不存在）");
				}
				if (!accountMongodb.getLogoffStatus().equals(AccountLogoffStatus.PENDING.getLogoffStatusValue())) {
					throw new ConflictBusinessException("取消注销失败（账号未申请注销流程）");
				}

				Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.NO.getLogoffStatusValue());
				accountUpdate.set(AccountMongodb.FIELD.LOGOFF_PENDING_TIME, null);
				accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(accountQuery, accountUpdate, options, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modified", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注销账号失败");
			}
		});
		if (modified != null) {
			cairoAuthAccountService.removeAccountCache(args.getAccountId());
			// 发送账号注销成功消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.UNLOGOFF_ACCOUNT),
				objectMapper.writeValueAsString(UnlogoffAccountMessage.builder()
					.accountId(modified.getAccountId())
					.nickname(modified.getNickname())
					.avatarUrl(modified.getAvatarUrl())
					.phoneNumber(modified.getPhoneNumber())
					.email(modified.getEmail())
					.username(modified.getUsername())
					.joinTime(modified.getJoinTime())
					.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * 删除账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:delete_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "delete_account", keys = {"#args.accountId"})
	public void deleteAccount(@Validated DeleteAccountArgs args) {
		AccountMongodb deletedAccount = transactionTemplate.execute(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId())
				);

				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null) {
					throw new ConflictBusinessException("删除账号失败（账号不存在）");
				}

//				if (AccountLogoffStatus.PENDING.getLogoffStatusValue().equals(accountMongodb.getLogoffStatus())) {
//					throw new ConflictBusinessException("删除账号失败（注销中不可强制删除）");
//				}

				if (AccountLogoffStatus.SUCCESS.getLogoffStatusValue().equals(accountMongodb.getLogoffStatus())) {
					throw new ConflictBusinessException("删除账号失败（账号已注销）");
				}

				Update accountUpdate = new Update();
				accountUpdate.set(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.SUCCESS.getLogoffStatusValue());
				accountUpdate.currentDate(AccountMongodb.FIELD.LOGOFF_SUCCESS_TIME);
				accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

				// 删除账号
				UpdateResult updateResult = mongoTemplate.updateFirst(accountQuery, accountUpdate, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				AccountMongodb deletedAccountMongodb = mongoTemplate.findAndRemove(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (deletedAccountMongodb != null) {
					//移入到删除表
					AccountMongodb insert = mongoTemplate.insert(deletedAccountMongodb, MongodbConstants.DeletedCollection.ACCOUNT);

					// 删除账号密码表
					Query accountPasswordQuery = Query.query(Criteria
						.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId())
					);
					Update accountPasswordUpdate = new Update();
					accountPasswordUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
					accountPasswordUpdate.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);
					mongoTemplate.updateMulti(accountPasswordQuery, accountPasswordUpdate, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);

					List<AccountPasswordMongodb> removedAccountPasswordList = mongoTemplate.findAllAndRemove(accountPasswordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
					if (!removedAccountPasswordList.isEmpty()) {
						mongoTemplate.insert(removedAccountPasswordList, MongodbConstants.DeletedCollection.ACCOUNT_PASSWORD);
					}

					// 删除账号第三方认证表
					Query accountSnsQuery = Query.query(Criteria.where(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));
					Update accountSnsUpdate = new Update();
					accountSnsUpdate.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
					accountSnsUpdate.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult accountSnsUpdateResult = mongoTemplate.updateMulti(accountSnsQuery, accountSnsUpdate, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					log.debug("account sns update result: {}", accountSnsUpdateResult);
					List<AccountSnsMongodb> removedAccountSnsList = mongoTemplate.findAllAndRemove(accountSnsQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (!removedAccountSnsList.isEmpty()) {
						mongoTemplate.insert(removedAccountSnsList, MongodbConstants.DeletedCollection.ACCOUNT_SNS);
					}

					//移除企业应用管理员账号
					Query tenantAppQuery = Query.query(Criteria.where(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(args.getAccountId()));
					Update tenantAppUpdate = new Update();
					tenantAppUpdate.pull(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS, args.getAccountId());
					tenantAppUpdate.set(TenantAppMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
					tenantAppUpdate.currentDate(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult tenantAppUpdateResult = mongoTemplate.updateMulti(tenantAppQuery, tenantAppUpdate, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
					log.debug("tenant app update result: {}", tenantAppUpdateResult);

					// 移除应用管理员账号
					Query appQuery = Query.query(Criteria.where(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(args.getAccountId()));
					Update appUpdate = new Update();
					tenantAppUpdate.pull(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS, args.getAccountId());
					tenantAppUpdate.currentDate(AppMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult appUpdateResult = mongoTemplate.updateMulti(appQuery, appUpdate, AppMongodb.class, MongodbConstants.Collection.APP);
					log.debug("app update result: {}", appUpdateResult);

				}
				return deletedAccountMongodb;
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteAccount", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("删除账号失败");
			}
		});

		if (deletedAccount != null) {
			//删除缓存
			cairoAuthAccountService.removeAccountCache(args.getAccountId());
			// 删除图标
			fileCommonService.deletePublicFile(FileKeyPrefixConstants.AVATAR_PREFIX, Collections.singletonList(deletedAccount.getAvatarUrl()));
			// 发送账号删除消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.DELETED_ACCOUNT),
				objectMapper.writeValueAsString(DeletedAccountMessage.builder()
					.accountId(deletedAccount.getAccountId())
					.nickname(deletedAccount.getNickname())
					.avatarUrl(deletedAccount.getAvatarUrl())
					.phoneNumber(deletedAccount.getPhoneNumber())
					.email(deletedAccount.getEmail())
					.username(deletedAccount.getUsername())
					.joinTime(deletedAccount.getJoinTime())
					.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
					.eventTime(LocalDateTime.now())
					.build()),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * 修改账号信息
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@Lock4j(name = "modify_account_info", keys = {"#args.accountId"})
	@SneakyThrows
	public void modifyAccountInfo(@Validated ModifyAccountInfoArgs args) {
		if (args.getUsername() == null && args.getPhoneNumber() == null && args.getEmail() == null) {
			throw new ConflictBusinessException("登录名,手机号和邮箱最少填一项");
		}

		// 验证用户名格式
		if (args.getUsername() != null && !args.getUsername().trim().isBlank() && !AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("用户名格式错误");
		}

		// 验证手机号格式
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank() && !AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Query query = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));

				Update update = new Update();
				update.set(AccountMongodb.FIELD.NICKNAME, args.getNickname());
				update.set(AccountMongodb.FIELD.AVATAR_URL, args.getAvatarUrl());

				update.set(AccountMongodb.FIELD.USERNAME, Optional.ofNullable(args.getUsername()).filter(x -> !x.trim().isEmpty()).orElse(null));
				update.set(AccountMongodb.FIELD.PHONE_NUMBER, Optional.ofNullable(args.getPhoneNumber()).filter(x -> !x.trim().isEmpty()).orElse(null));
				update.set(AccountMongodb.FIELD.EMAIL, Optional.ofNullable(args.getEmail()).filter(x -> !x.trim().isEmpty()).orElse(null));

				update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改账号信息失败");
				}
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("resetPassword", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改账号信息失败");
			}

		});

		cairoAuthAccountService.removeAccountCache(args.getAccountId());

	}

	/**
	 * 修改账号状态
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_account_status", keys = {"#args.accountId"})
	public void modifyAccountStatus(ModifyAccountStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));

				Update update = Update.update(AccountMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改账号状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAccountStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改账号状态");
			}
		});

		cairoAuthAccountService.removeAccountCache(args.getAccountId());
	}

	/**
	 * 修改账号状态
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_lock_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void modifyAccountLockStatus(ModifyAccountLockArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId()));

				Update update = Update.update(AccountMongodb.FIELD.LOCKED, args.getLocked());
				if (args.getLocked()) {
					update.currentDate(AccountMongodb.FIELD.LOCKED_TIME);
				} else {
					update.set(AccountMongodb.FIELD.LOCKED_TIME, null);
				}
				update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
				update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改账号锁定状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAccountLockedStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改账号锁定状态失败");
			}
		});

		cairoAuthAccountService.removeAccountCache(args.getAccountId());
	}
}
