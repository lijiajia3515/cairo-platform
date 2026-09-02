package io.github.lijiajia3515.cairo.auth.api.client.account;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.SearchAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoAuthBusiness;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AccountNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.authentication.CairoOAuthAccountPrincipal;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.AccountExtension;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CairoAccountAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.CreateAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountPasswordStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountAvatarUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.account.ModifyAccountUsernameArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.ModifiedAccountPasswordMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.business.DefaultBusiness;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [client/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountClientApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RabbitTemplate rabbitTemplate;

	private final VerifyCodeService verifyCodeService;
	private final CairoAuthAccountService cairoAuthAccountService;
	private final AccountCommonService accountCommonService;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final AuthProperties authProperties;
	private final PublicFileClientApiService publicFileClientApiService;

	/**
	 * 默认密码
	 */
	public static final String PASSWORD_DEFAULT = "123456";

	public AccountClientApiService(MongoTemplate mongoTemplate,
								   TransactionTemplate transactionTemplate,
								   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, RedisTemplate<String, Object> redisTemplate,
								   RabbitTemplate rabbitTemplate,
								   CairoAuthAccountService cairoAuthAccountService,
								   AccountCommonService accountCommonService,
								   VerifyCodeService verifyCodeService,
								   PublicFileClientApiService publicFileClientApiService,
								   CairoRabbitmqTool cairoRabbitmqTool,
								   ObjectMapper objectMapper,
								   AuthProperties authProperties) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.rabbitTemplate = rabbitTemplate;

		this.verifyCodeService = verifyCodeService;
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.accountCommonService = accountCommonService;
		this.publicFileClientApiService = publicFileClientApiService;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.authProperties = authProperties;
	}

	/**
	 * 获取账号认证模型
	 *
	 * @param args 参数
	 * @return 账号认证模型
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_auth",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public CairoAccountAuthModel getAccountAuth(@Validated GetAccountAuthArgs args) {
		CairoAccountAuthModel.CairoAccountAuthModelBuilder<?, ?> builder = CairoAccountAuthModel.builder();
		try {
			CairoAuthAccount account = cairoAuthAccountService.getAuthAccountModel(args.getAppId(), args.getClientId(), args.getAccountId());

			if (account.isLocked()) {
				throw new LockedException("账号被锁定");
			}

			if (!account.isEnabled()) {
				throw new DisabledException("账号被禁用");
			}

			CairoOAuthAccountPrincipal principal = CairoOAuthAccountPrincipal.builder()
				.appId(args.getAppId())
				.clientId(args.getClientId())
				.accountId(account.getAccountId())
				.nickname(account.getNickname())
				.username(account.getLoginname())
				.phoneNumber(account.getPhoneNumber())
				.email(account.getEmail())
				.avatarUrl(account.getAvatarUrl())
				.roles(Collections.emptyList())
				.departments(Collections.emptyList())
				.tags(Collections.emptyList())
				.locked(account.isLocked())
				.enabled(account.isEnabled())
				.build();

			return builder.status(DefaultBusiness.SUCCESS.getCode())
				.principal(principal)
				.authorities(account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.build();
		} catch (UsernameNotFoundException | AccountNotFoundException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_NOT_FOUND.getCode());
		} catch (LockedException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_LOCKED.getCode());
		} catch (DisabledException e) {
			builder.status(CairoAuthBusiness.ACCOUNT_DISABLED.getCode());
		} catch (AuthenticationException e) {
			builder.status(CairoAuthBusiness.ERROR.getCode());
		}
		return builder.build();

	}

	/**
	 * 根据账号id获取账号信息
	 *
	 * @param args args
	 * @return 账号信息
	 */
	@BizLog(
		bizId = "account:get_account_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@NewSpan
	public Account getAccountInfo(@Validated @NotNull GetAccountInfoArgs args) {
		AccountExtension accountExtension = Optional.ofNullable(args.getExtension()).map(x -> x.get(CairoAuthExtensionConstants.ACCOUNT)).map(AccountExtension::valueOf).orElse(AccountExtension.BASIC);

		Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		Query query = Query.query(criteria);

		AccountMongodb account = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) return null;
		return AccountConverter.convertAccount(account, accountExtension);
	}

	/**
	 * 获取账号 分页模式
	 *
	 * @param args 分页参数
	 * @return 账号信息 分页模型
	 */
	@BizLog(
		bizId = "account:get_account_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@NewSpan
	public Page<Account> getAccountPageList(@Validated GetAccountPageListArgs args) {
		Criteria criteria = new Criteria();
		if (args.getAccountIds() != null && args.getAccountIds().isEmpty()) {
			return new Page<>(args, Collections.emptyList(), 0L);
		}

		if (args.getAccountIds() != null) {
			criteria.and(AccountMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(AccountMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AccountMongodb.FIELD.METADATA.UPDATE_TIME)));

		long total = readMongoTemplate.count(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		query.with(args.pageable())
			.with(Sort.by(Sort.Order.desc(AccountMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<AccountMongodb> records = readMongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		List<Account> list = getAccountList(records, args.getExtension());
		return new Page<>(args, list, total);
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
	public List<Account> getAccountList(@Validated GetAccountListArgs args) {
		Criteria criteria = new Criteria();
		if (args.getAccountIds() != null && args.getAccountIds().isEmpty()) {
			return Collections.emptyList();
		}

		if (args.getAccountIds() != null) {
			criteria.and(AccountMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(AccountMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AccountMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<AccountMongodb> records = readMongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		return getAccountList(records, args.getExtension());
	}

	/**
	 * 搜索账号
	 *
	 * @param args 分页参数
	 * @return 账号信息
	 */
	@NewSpan
	@BizLog(
		bizId = "account:search_account",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Account searchAccountInfo(@Validated SearchAccountArgs args) {
		Criteria criteria = new Criteria();
		SearchAccountArgs.Type type = SearchAccountArgs.Type.ofTypeValue(args.getType()).orElse(SearchAccountArgs.Type.PHONE_NUMBER);
		if (type.equals(SearchAccountArgs.Type.ACCOUNT_ID)) {
			if (args.getAccountId() == null || args.getAccountId().isBlank()) {
				throw new ParamsErrorBusinessException("accountId不能为空");
			}
			criteria.and(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId().trim());
		} else if (type.equals(SearchAccountArgs.Type.PHONE_NUMBER)) {
			if (args.getPhoneNumber() == null || args.getPhoneNumber().isBlank()) {
				throw new ParamsErrorBusinessException("phoneNumber不能为空");
			}
			criteria.and(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber().trim());
		} else if (type.equals(SearchAccountArgs.Type.USERNAME)) {
			if (args.getUsername() == null || args.getUsername().isBlank()) {
				throw new ParamsErrorBusinessException("username不能为空");
			}
			criteria.and(AccountMongodb.FIELD.USERNAME).is(args.getUsername().trim());
		} else if (type.equals(SearchAccountArgs.Type.EMAIL)) {
			if (args.getEmail() == null || args.getEmail().isBlank()) {
				throw new ParamsErrorBusinessException("email不能为空");
			}
			criteria.and(AccountMongodb.FIELD.EMAIL).is(args.getEmail().trim());
		} else {
			throw new ConflictBusinessException("搜索类型不支持");
		}
		Query query = Query.query(criteria);
		query.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.AVATAR_URL, AccountMongodb.FIELD.NICKNAME, AccountMongodb.FIELD.PHONE_NUMBER, AccountMongodb.FIELD.USERNAME, AccountMongodb.FIELD.EMAIL, AccountMongodb.FIELD.JOIN_TIME);
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb == null) {
			throw new ConflictBusinessException("账号不存在");
		}
		return Account.builder()
			.accountId(accountMongodb.getAccountId())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.nickname(accountMongodb.getNickname())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.username(accountMongodb.getUsername())
			.email(accountMongodb.getEmail())
			.joinTime(accountMongodb.getJoinTime())
			.logoffStatus(accountMongodb.getLogoffStatus())
			.logoffPendingTime(accountMongodb.getLogoffPendingTime())
			.logoffSuccessTime(accountMongodb.getLogoffSuccessTime())
			.build();
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
	public Optional<String> createAccount(@Validated CreateAccountArgs args) {
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
		//图片
		String avatarUrl;
		if (args.getAvatarUrl() != null && !args.getAvatarUrl().isBlank()) {
			String fileName = FileKeyPrefixConstants.AVATAR_PREFIX + CoreConstants.SNOWFLAKE.nextIdStr();
			CairoMultipartFile cairoMultipartFile = FilesUtil.urlConvertCairoMultipart(args.getAvatarUrl(),
				fileName.concat(FilesUtil.getType(args.getAvatarUrl())));
			List<String> avatarUrls = publicFileClientApiService.uploadFile(fileName, cairoMultipartFile);
			avatarUrl = Optional.ofNullable(avatarUrls).filter(z -> z.size() == 3).map(x -> x.get(2)).orElse(null);
		} else {
			avatarUrl = null;
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
					.avatarUrl(Optional.ofNullable(avatarUrl).orElse(authProperties.getDefaultAvatarUrl()))
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

		return Optional.of(accountId);
	}

	/**
	 * 修改当前账号用户名
	 *
	 * @param accountId 账号ID
	 * @param args      参数
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_username",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_account_username", keys = {"#accountId"})
	public void modifyAccountUsername(@Valid @NotNull String accountId, @Validated ModifyAccountUsernameArgs args) {
		// 验证用户名格式
		if (!AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("用户名格式错误");
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
				Query query = Query.query(criteria);

				Update update = Update.update(AccountMongodb.FIELD.USERNAME, args.getUsername());
				update.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
				update.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.upsert(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (updateResult.getModifiedCount() < 1L) {
					throw new ConflictBusinessException("修改用户名失败");
				}
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("修改用户名失败", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改用户名失败");
			}
		});

		cairoAuthAccountService.removeAccountCache(args.getAccountId());
	}

	/**
	 * 修改当前账号手机号
	 *
	 * @param accountId 账号ID
	 * @param args      参数
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_account_phone_number", keys = {"#accountId"})
	public void modifyAccountPhoneNumber(@Valid @NotNull String accountId, @Validated ModifyAccountPhoneNumberArgs args) {
		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Query query = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));
				query.fields().include(AccountMongodb.FIELD.PHONE_NUMBER);
				AccountMongodb accountMongodb = mongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null) {
					throw new ConflictBusinessException("修改失败");
				}

				// 验证旧手机号
				if (accountMongodb.getPhoneNumber() != null) {
					if (accountMongodb.getPhoneNumber().equals(args.getPhoneNumber())) {
						throw new ConflictBusinessException("手机号相同，无需重复修改");
					}

					VerifyCodeStat oldVerifyCodeStat = verifyCodeService.verify(
						VerifyVerifyCodeArgs.builder()
							.bizCode(CairoAuthVerifyCodeConstants.AUTH)
							.target(accountMongodb.getPhoneNumber())
							.maxFailCount(3)
							.verifyCode(args.getSourceVerifyCode())
							.build()
					);

					if (!VerifyCodeStat.SUCCESS.equals(oldVerifyCodeStat)) {
						throw new ConflictBusinessException("手机号验证码错误");
					}
				}

				Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber()));
				boolean exists = mongoTemplate.exists(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (exists) {
					throw new ConflictBusinessException("修改失败(手机号已绑定其他账号)");
				}

				VerifyCodeStat oldVerifyCodeStat = verifyCodeService.verify(
					VerifyVerifyCodeArgs.builder()
						.bizCode(CairoAuthVerifyCodeConstants.AUTH)
						.target(args.getPhoneNumber())
						.maxFailCount(3)
						.verifyCode(args.getVerifyCode())
						.build()
				);

				if (!VerifyCodeStat.SUCCESS.equals(oldVerifyCodeStat)) {
					throw new ConflictBusinessException("新手机号验证码错误");
				}

				Update update = Update.update(AccountMongodb.FIELD.PHONE_NUMBER, args.getPhoneNumber());
				update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
				update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				log.debug("updateResult: {}", updateResult);
			} catch (BusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAccountPhoneNumber", e);
				throw new ConflictBusinessException("修改账号手机号失败");
			}
		});

		cairoAuthAccountService.removeAccountCache(args.getAccountId());
	}

	/**
	 * 获取帐号是否设置密码
	 *
	 * @param accountId 账号ID
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_password_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	public boolean getAccountPasswordStatus(@Valid @NotNull String accountId) {
		Criteria accountPasswordCriteria = Criteria
			.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
			.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());
		Query accountPasswordQuery = Query.query(accountPasswordCriteria);
		return readMongoTemplate.exists(accountPasswordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
	}

	/**
	 * 修改账号密码
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_password",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_account_password", keys = {"#accountId"})
	public Optional<String> modifyPassword(@Validated ModifyAccountPasswordArgs args) {
		String encodeNewPassword = accountCommonService.getPasswordEncoder().encode(args.getNewPassword());
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria criteria = Criteria
					.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId())
					.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());
				Query query = Query.query(criteria);
				String oldEncoderPassword = Optional.ofNullable(mongoTemplate.findOne(query, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD))
					.map(AccountPasswordMongodb::getPassword).orElse(null);
				boolean match = false;
				if (oldEncoderPassword == null) {
					match = true;
				} else {
					if (args.getPassword() != null) {
						match = accountCommonService.getPasswordEncoder().matches(args.getPassword(), oldEncoderPassword);
					}
				}

				if (!match) {
					throw new ConflictBusinessException("密码错误");
				}

				Update update = Update.update(AccountPasswordMongodb.FIELD.PASSWORD, encodeNewPassword);
				update.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
				update.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.upsert(query, update, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);

				if (updateResult.getModifiedCount() < 1L && updateResult.getUpsertedId() == null) {
					throw new ConflictBusinessException("修改密码失败");
				}

			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("密码修改失败", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改密码失败");
			}
		});

		// 发送密码变更通知
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.MODIFIED_ACCOUNT_PASSWORD),
			objectMapper.writeValueAsString(ModifiedAccountPasswordMessage.builder()
				.accountId(args.getAccountId())
				.eventAccountId(args.getAccountId())
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
		);
		return Optional.empty();
	}

	/**
	 * 修改账号头像
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_account_password",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_account_avatar", keys = {"#accountId"})
	public Optional<String> modifyAccountAvatar(@Validated ModifyAccountAvatarUrlArgs args) {
		Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		Query query = Query.query(criteria);
		Update update = new Update();
		Optional.ofNullable(args.getAvatarUrl()).ifPresent(x -> update.set(AccountMongodb.FIELD.AVATAR_URL, x));

		update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, args.getAccountId());
		update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

		transactionTemplate.execute(status -> {
			try {
				AccountMongodb account = mongoTemplate.findAndModify(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (account == null) {
					throw new ConflictBusinessException("账号不存在");
				}
				return account;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyMyAccountAvatarUrl", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改头像失败");
			}
		});
		cairoAuthAccountService.removeAccountCache(args.getAccountId());
		return Optional.empty();
	}

	public Map<String, Account> getAccountMap(GetAccountListArgs args) {
		return getAccountList(args).stream().collect(Collectors.toMap(Account::getAccountId, x -> x, (x1, x2) -> x1));
	}

	public Optional<String> modifyAccountUsername(ModifyAccountUsernameArgs args) {
		modifyAccountUsername(args.getAccountId(), args);
		return Optional.empty();
	}

	public Optional<String> modifyAccountPhoneNumber(ModifyAccountPhoneNumberArgs args) {
		modifyAccountPhoneNumber(args.getAccountId(), args);
		return Optional.empty();
	}

	public Optional<Boolean> getAccountPasswordStatus(GetAccountPasswordStatusArgs args) {
		return Optional.of(getAccountPasswordStatus(args.getAccountId()));
	}


	public List<Account> getAccountList(List<AccountMongodb> accountMongodbList, Map<String, String> extensionMap) {
		AccountExtension accountExtension = Optional.ofNullable(extensionMap).map(x -> x.get(CairoAuthExtensionConstants.ACCOUNT)).map(AccountExtension::valueOf).orElse(AccountExtension.FULL_INFO);

		return accountMongodbList.stream()
			.map(x -> AccountConverter.convertAccount(x, accountExtension))
			.collect(Collectors.toList());
	}
}
