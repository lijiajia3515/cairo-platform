package io.github.lijiajia3515.cairo.auth.api.subapp.app_user;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.CreateAccountAndAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.CreateAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.DeleteAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.GetAppUserListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.LogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.ModifyAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.ModifyAppUserStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.TransferAppUserToOtherAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user.UnlogoffAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUserMetadata;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserTag;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.DeletedAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.LogoffSuccessAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.UnlogoffAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserConverter;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.app_user_tag.AppUserTagCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


/**
 * [subapp_user/api] app user service
 */
@Slf4j
@Validated
@Component
public class AppUserSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final AppUserAuthorizationCommonService appUserAuthorizationCommonService;
	private final CairoAuthAppUserService cairoAuthAppUserService;
	private final AppDepartmentCommonService departmentCommonService;
	private final AppRoleCommonService roleCommonService;
	private final AccountCommonService accountCommonService;
	private final AppUserCommonService appUserCommonService;
	private final AppUserTagCommonService appUserTagCommonService;

	private final ObjectMapper objectMapper;
	private final CairoRabbitmqTool cairoRabbitmqTool;

	private final AuthProperties authProperties;


	public AppUserSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									   TransactionTemplate transactionTemplate,
									   RabbitTemplate rabbitTemplate,
									   CairoRabbitmqTool cairoRabbitmqTool,
									   AppUserAuthorizationCommonService appUserAuthorizationCommonService,
									   CairoAuthAppUserService cairoAuthAppUserService,
									   AppDepartmentCommonService departmentCommonService,
									   AppRoleCommonService roleCommonService,
									   AccountCommonService accountCommonService,
									   AppUserCommonService appUserCommonService,
									   AppUserTagCommonService appUserTagCommonService,
									   AuthProperties authProperties,
									   ObjectMapper objectMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;

		this.appUserAuthorizationCommonService = appUserAuthorizationCommonService;
		this.cairoAuthAppUserService = cairoAuthAppUserService;
		this.departmentCommonService = departmentCommonService;
		this.roleCommonService = roleCommonService;
		this.appUserTagCommonService = appUserTagCommonService;
		this.accountCommonService = accountCommonService;
		this.appUserCommonService = appUserCommonService;

		this.objectMapper = objectMapper;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.authProperties = authProperties;
	}


	/**
	 * getUserList
	 *
	 * @param appId appId
	 * @param args  args
	 * @return user list
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user:get_app_user_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<AppUserMetadata> getAppUserList(@Valid @NotNull String appId, @Validated GetAppUserListArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(AppUserMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<AppUserMongodb> users = mongoTemplate.find(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		return getAppUserList(readMongoTemplate, appId, users, args.getExtension());
	}


	/**
	 * 获取应用级用户列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 应用级用户分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user:get_app_user_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AppUserMetadata> getUserPageList(@Valid @NotNull String appId, @Validated GetAppUserListArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = mongoTemplate.count(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(AppUserMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<AppUserMongodb> ms = mongoTemplate.find(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);

		List<AppUserMetadata> contents = getAppUserList(readMongoTemplate, appId, ms, args.getExtension());
		return new Page<>(args, contents, total);
	}

	/**
	 * 根据应用级用户ID获取应用级用户
	 *
	 * @return return app_user
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user:get_app_user_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public AppUserMetadata getAppUserInfo(@Valid @NotNull String appId, @Valid @NotNull String userId) {
		return getAppUserInfo(readMongoTemplate, appId, userId)
			.orElseThrow(() -> new ConflictBusinessException("应用级用户不存在"));
	}


	/**
	 * 获取应用级用户根据应用级用户ID
	 *
	 * @return userId
	 */
	@NewSpan
	public Optional<AppUserMetadata> getAppUserInfo(MongoTemplate template, String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId);

		Query query = Query.query(criteria);
		return Optional.ofNullable(template.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER))
			.flatMap(m -> getAppUserList(template, appId, Collections.singletonList(m), Collections.singletonMap(CairoAuthExtensionConstants.APP_USER, AppUserExtension.FULL_INFO.name()))
				.stream().findFirst());
	}

	/**
	 * 后台 新增应用级用户
	 *
	 * @param args args
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "app_user:create_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAppUser(@Valid @NotNull String appId, @Validated CreateAppUserArgs args) {
		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		AccountMongodb account = readMongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new ConflictBusinessException("账号不存在");
		}

		Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		AppUserMongodb existsAppUserMongodb = readMongoTemplate.findOne(Query.query(criteria), AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
		if (existsAppUserMongodb != null) {
			throw new ConflictBusinessException(String.format("该账号已绑定应用级用户: %s(%s)", existsAppUserMongodb.getNickname(), existsAppUserMongodb.getUserId()));
		}

		Criteria appCriteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).is(appId)
			.and(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(args.getAccountId());
		Query appQuery = Query.query(appCriteria);
		boolean admin = readMongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

		AppUserMongodb insertedAppUser = transactionTemplate.execute(status -> {
			try {
				AppUserMongodb insertAppUser = AppUserMongodb.builder()
					.appId(appId)
					.userId(appUserCommonService.getNewAppUserId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(account.getNickname()))
					.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).orElse(account.getPhoneNumber()))
					.enabled(true)
					.admin(admin)
					.departmentIds(args.getDepartmentIds())
					.roleIds(args.getRoleIds())
					.tagIds(args.getTagIds())
					.joinTime(LocalDateTime.now())
					.accountId(account.getAccountId())
					.position(args.getPosition())
					.mainDepartmentId(args.getMainDepartmentId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				return mongoTemplate.insert(insertAppUser, MongodbConstants.Collection.APP_USER);
			} catch (Exception e) {
				log.debug("createAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建应用级用户失败");
			}
		});

		if (insertedAppUser == null) {
			throw new ConflictBusinessException("创建应用级用户失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER, appId),
			objectMapper.writeValueAsString(
				CreatedAppUserMessage.builder()
					.appId(appId)
					.admin(admin)
					.userId(insertedAppUser.getUserId())
					.nickname(insertedAppUser.getNickname())
					.accountId(insertedAppUser.getAccountId())
					.eventAppUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 创建账号并且创建应用级用户
	 *
	 * @param appId 应用id
	 * @param args  参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "app_user:create_account_and_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAccountAndAppUser(@Valid @NotNull String appId, @Validated CreateAccountAndAppUserArgs args) {
		Criteria accountCriteria = new Criteria();
		boolean phoneNumber = false, app_username = false, email = false;
		List<Criteria> criteriaList = new ArrayList<>();
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank()) {
			phoneNumber = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber().trim()));
		}
		if (args.getUsername() != null && !args.getUsername().trim().isBlank()) {
			app_username = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.USERNAME).is(args.getUsername().trim()));
		}
		if (args.getEmail() != null && !args.getEmail().trim().isBlank()) {
			email = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.EMAIL).is(args.getEmail().trim()));
		}

		if (!(phoneNumber || app_username || email)) {
			throw new ConflictBusinessException("手机号，应用级用户名，邮箱，必须三选一");
		}

		// 验证应用级用户名格式
		if (args.getUsername() != null && !args.getUsername().trim().isBlank() && !AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("应用级用户名格式错误");
		}

		// 验证手机号格式
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank() && !AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		accountCriteria.orOperator(criteriaList);
		boolean exists = mongoTemplate.exists(Query.query(accountCriteria), MongodbConstants.Collection.ACCOUNT);
		AccountMongodb accountMongodb;
		if (!exists) {
			// 创建账号
			String accountId = accountCommonService.getNewAccountId();
			accountMongodb = AccountMongodb.builder()
				.accountId(accountId)
				.nickname(Optional.ofNullable(args.getNickname()).orElse(accountId))
				.avatarUrl(Optional.ofNullable(args.getAvatarUrl()).orElse(authProperties.getDefaultAvatarUrl()))
				.username(args.getUsername())
				.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).filter(x -> !x.trim().isEmpty()).orElse(null))
				.email(Optional.ofNullable(args.getEmail()).filter(x -> !x.isBlank()).orElse(null))
				.enabled(true)
				.locked(false)
				.joinTime(LocalDateTime.now())
				.logoffStatus(AccountLogoffStatus.NO.getLogoffStatusValue())
				.metadata(AccountMetadataMongodb.builder()
					.createAccountId(accountId)
					.updateAccountId(accountId)
					.build()
				)
				.build();
			String password = Optional.ofNullable(args.getPassword()).orElse(accountCommonService.getNewPassword());
			String encodePassword = accountCommonService.getPasswordEncoder().encode(password);
			AccountPasswordMongodb accountPasswordMongodb = AccountPasswordMongodb.builder()
				.accountId(accountId)
				.type(PasswordType.PASSWORD.getType())
				.password(encodePassword)
				.metadata(AccountMetadataMongodb.builder()
					.createAccountId(accountId)
					.updateAccountId(accountId)
					.build()
				)
				.build();

			transactionTemplate.executeWithoutResult(status -> {
				try {
					mongoTemplate.insert(accountMongodb, MongodbConstants.Collection.ACCOUNT);
					mongoTemplate.insert(accountPasswordMongodb, MongodbConstants.Collection.ACCOUNT_PASSWORD);
				} catch (Exception e) {
					log.debug("账号创建失败", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("账号创建失败");
				}
			});
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
				objectMapper.writeValueAsString(
					CreatedAccountMessage.builder()
						.accountId(accountMongodb.getAccountId())
						.nickname(accountMongodb.getNickname())
						.phoneNumber(accountMongodb.getPhoneNumber())
						.username(accountMongodb.getUsername())
						.password(password)
						.eventAccountId(CairoSecurityContextHolder.getSubappAccountId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		} else {
			// 获取账号
			accountMongodb = mongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			if (accountMongodb == null) {
				throw new ConflictBusinessException("账号查询失败");
			}
		}

		// 创建应用级用户
		AppUserMongodb insertedAppUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId());
				AppUserMongodb existsAppUser = mongoTemplate.findOne(Query.query(criteria), AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (existsAppUser != null) {
					throw new ConflictBusinessException(
						String.format("账号信息【%s(%s)】已被用户【%s(%s)】使用",
							Optional.ofNullable(accountMongodb.getPhoneNumber()).orElse(Optional.ofNullable(accountMongodb.getUsername()).orElse(accountMongodb.getAccountId())),
							accountMongodb.getAccountId(),
							Optional.ofNullable(existsAppUser.getNickname()).orElse(existsAppUser.getUserId()),
							existsAppUser.getUserId())
					);
				}
				Criteria appCriteria = Criteria
					.where(AppMongodb.FIELD.APP_ID).is(appId)
					.and(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(accountMongodb.getAccountId());
				Query appQuery = Query.query(appCriteria);
				boolean admin = readMongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

				AppUserMongodb user = AppUserMongodb.builder()
					.appId(appId)
					.userId(appUserCommonService.getNewAppUserId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(accountMongodb.getNickname()))
					.phoneNumber(accountMongodb.getPhoneNumber())
					.roleIds(args.getRoleIds())
					.departmentIds(args.getDepartmentIds())
					.tagIds(args.getTagIds())
					.position(args.getPosition())
					.mainDepartmentId(args.getMainDepartmentId())
					.enabled(true)
					.admin(admin)
					.joinTime(LocalDateTime.now())
					.accountId(accountMongodb.getAccountId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(user, MongodbConstants.Collection.APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("创建应用级用户失败", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建应用级用户失败");
			}
		});

		if (insertedAppUserMongodb != null) {
			// 发送创建应用级用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER, appId),
				objectMapper.writeValueAsString(
					CreatedAppUserMessage.builder()
						.appId(appId)
						.admin(insertedAppUserMongodb.getAdmin())
						.userId(insertedAppUserMongodb.getUserId())
						.nickname(insertedAppUserMongodb.getNickname())
						.accountId(insertedAppUserMongodb.getAccountId())
						.eventAppUserId(CairoSecurityContextHolder.getSubappUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}

	/**
	 * 修改 账号角色,岗位,部门,标签..
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_user_info", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:modify_app_user_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppUserInfo(@Valid @NotNull String appId, @Validated ModifyAppUserInfoArgs args) {
		AppUserMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId()));

				Update update = Update
					.update(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId())
					.set(AppUserMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
				Optional.ofNullable(args.getNickname()).ifPresent(x -> update.set(AppUserMongodb.FIELD.NICKNAME, x));
				Optional.ofNullable(args.getPhoneNumber()).ifPresent(x -> update.set(AppUserMongodb.FIELD.PHONE_NUMBER, x));
				Optional.ofNullable(args.getRoleIds()).ifPresent(x -> update.set(AppUserMongodb.FIELD.ROLE_IDS, x));
				Optional.ofNullable(args.getTagIds()).ifPresent(x -> update.set(AppUserMongodb.FIELD.TAG_IDS, x));
				Optional.ofNullable(args.getDepartmentIds()).ifPresent(x -> update.set(AppUserMongodb.FIELD.DEPARTMENT_IDS, x));
				Optional.ofNullable(args.getPosition()).ifPresent(x -> update.set(AppUserMongodb.FIELD.POSITION, x));
				Optional.ofNullable(args.getMainDepartmentId()).ifPresent(x -> update.set(AppUserMongodb.FIELD.MAIN_DEPARTMENT_ID, x));
				return mongoTemplate.findAndModify(query, update, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
			} catch (Exception e) {
				log.debug("modifyAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("应用级用户修改失败");
			}
		});

		if (userMongodb == null) {
			throw new ConflictBusinessException("应用级用户修改失败");
		}

		cairoAuthAppUserService.removeAppUserCache(appId, userMongodb.getUserId());
	}


	@NewSpan
	@Lock4j(name = "modify_app_user_status", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:modify_app_user_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppUserStatus(@Valid @NotNull String appId, @Validated ModifyAppUserStatusArgs args) {
		AppUserMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId())
				);
				AppUserMongodb appUserMongodb = mongoTemplate.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (appUserMongodb == null) {
					throw new ConflictBusinessException("应用级用户不存在");
				}

				if (appUserMongodb.getAdmin() != null && appUserMongodb.getAdmin()) {
					throw new ConflictBusinessException("修改应用级用户状态失败，请联系平台管理员移除当前操作账号管理员权限后再试");
				}

				Update update = new Update();
				update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(AppUserMongodb.FIELD.ENABLED, x));

				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, findAndModifyOptions, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
			} catch (Exception e) {
				log.info("modifyAppUserStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用级用户状态失败");
			}
		});

		if (userMongodb == null) {
			throw new ConflictBusinessException("修改应用级用户状态失败");
		}

		cairoAuthAppUserService.removeAppUserCache(appId, userMongodb.getUserId());
	}

	@NewSpan
	@Lock4j(name = "transfer_app_user_to_other_account", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:transfer_app_user_to_other_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void transferAppUserToOtherAccount(@Valid @NotNull String appId, @Validated TransferAppUserToOtherAccountArgs args) {
		Account account = accountCommonService.getAccount(args.getOtherAccountId());
		if (account == null) {
			throw new ConflictBusinessException("转移用户失败（转移账号不存在）");
		}
		AppUserMongodb modifiedAppUserMongodb = transactionTemplate.execute(status -> {
			try {
				Query userQuery = Query.query(Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId())
				);
				userQuery.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.ACCOUNT_ID, AppUserMongodb.FIELD.NICKNAME);
				AppUserMongodb userMongodb = mongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("转移用户失败（用户不存在）");
				}

				if (userMongodb.getLogoffStatus() != null && userMongodb.getLogoffStatus().equals(AppUserLogoffStatus.PENDING.getLogoffStatusValue())) {
					throw new ConflictBusinessException("转移用户失败（注销中用户无法转移）");
				}

				if (userMongodb.getAccountId() != null && userMongodb.getAccountId().equals(args.getOtherAccountId())) {
					throw new ConflictBusinessException("转移用户失败（账号已绑定当前用户）");
				}

				if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
					throw new ConflictBusinessException("转移用户失败（平台管理员无法转移）");
				}

				Query otherAccountAppUserQuery = Query.query(Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.ACCOUNT_ID).is(args.getOtherAccountId())
				);
				otherAccountAppUserQuery.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.ACCOUNT_ID, AppUserMongodb.FIELD.NICKNAME);
				AppUserMongodb otherAccountAppUserAppUserMongodb = mongoTemplate.findOne(otherAccountAppUserQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (otherAccountAppUserAppUserMongodb != null) {
					throw new ConflictBusinessException(String.format("转移用户失败（绑定账号已存在用户【%s(%s)】)", otherAccountAppUserAppUserMongodb.getNickname(), otherAccountAppUserAppUserMongodb.getUserId()));
				}

				Update userUpdate = Update.update(AppUserMongodb.FIELD.ACCOUNT_ID, args.getOtherAccountId());
				userUpdate.currentDate(AppUserMongodb.FIELD.TRANSFER_ACCOUNT_TIME);
				userUpdate.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.NO.getLogoffStatusValue());
				userUpdate.set(AppUserMongodb.FIELD.LOGOFF_PENDING_TIME, null);
				userUpdate.set(AppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME, null);
				userUpdate.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				userUpdate.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(userQuery, userUpdate, findAndModifyOptions, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("transferAppUserToOtherAccount", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("转移用户失败");
			}
		});

		if (modifiedAppUserMongodb == null) {
			throw new ConflictBusinessException("转移用户失败");
		}
		// 删除缓存
		cairoAuthAppUserService.removeAppUserCache(appId, args.getUserId());
		// 退出登录
		appUserAuthorizationCommonService.offlineEndpointAuthorization(appId, args.getUserId());
	}

	/**
	 * 直接注销应用级用户
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "logoff_app_user", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:logoff_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void logoffAppUser(@Valid @NotNull String appId, @Validated LogoffAppUserArgs args) {
		AppUserMongodb logoffSuccessAppUser = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId());

				Query.query(criteria);
				Query query = Query.query(criteria);

				AppUserMongodb userMongodb = mongoTemplate.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("注销失败（用户不存在）");
				}

				if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
					throw new ConflictBusinessException("注销失败（系统管理员无法注销）");
				}

				if (userMongodb.getLogoffStatus() != null && Objects.equals(AppUserLogoffStatus.SUCCESS.getLogoffStatusValue(), userMongodb.getLogoffStatus())) {
					throw new ConflictBusinessException("注销失败（无需重复注销）");
				}

				Update update = new Update();
				update.set(AppUserMongodb.FIELD.ACCOUNT_ID, null);
				update.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
				update.set(AppUserMongodb.FIELD.ADMIN, false); // 注销成功用户取消管理员身份
				update.set(AppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME, LocalDateTime.now());
				update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				AppUserMongodb modifiedAppUserMongodb = mongoTemplate.findAndModify(query, update, options, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (modifiedAppUserMongodb == null) {
					throw new ConflictBusinessException("注销应用级用户失败");
				}
				// 将老账号ID，返回给外部发送消息到队列中
				modifiedAppUserMongodb.setAccountId(userMongodb.getAccountId());
				return modifiedAppUserMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注销应用级用户失败");
			}
		});
		if (logoffSuccessAppUser != null) {
			// 发送注销应用级用户成功消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_SUCCESS_APP_USER, appId),
				objectMapper.writeValueAsString(
					LogoffSuccessAppUserMessage.builder()
						.appId(appId)
						.userId(logoffSuccessAppUser.getUserId())
						.nickname(logoffSuccessAppUser.getNickname())
						.accountId(logoffSuccessAppUser.getAccountId())
						.eventAppUserId(CairoSecurityContextHolder.getSubappUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
		// 清理缓存
		cairoAuthAppUserService.removeAppUserCache(appId, args.getUserId());
	}

	/**
	 * 取消注销应用级用户
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "unlogoff_app_user", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:unlogoff_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void unlogoffAppUser(@Valid @NotNull String appId, @Validated UnlogoffAppUserArgs args) {
		AppUserMongodb modified = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId());

				Query.query(criteria);
				Query query = Query.query(criteria);

				AppUserMongodb userMongodb = mongoTemplate.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("取消注销失败（用户不存在）");
				}

				if (userMongodb.getLogoffStatus() != null && userMongodb.getLogoffStatus().equals(AppUserLogoffStatus.NO.getLogoffStatusValue())) {
					throw new ConflictBusinessException("取消注销失败（未申请注销流程）");
				}


				Update update = new Update();
				update.set(AppUserMongodb.FIELD.LOGOFF_STATUS, AppUserLogoffStatus.NO.getLogoffStatusValue());
				update.set(AppUserMongodb.FIELD.LOGOFF_PENDING_TIME, null);
				update.set(AppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME, null);
				update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				AppUserMongodb modifiedAppUserMongodb = mongoTemplate.findAndModify(query, update, options, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (modifiedAppUserMongodb == null) {
					throw new ConflictBusinessException("取消注销失败");
				}
				return modifiedAppUserMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("unlogoffAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("取消注销失败");
			}
		});

		if (modified != null) {
			// 发送注销应用级用户成功消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.UNLOGOFF_APP_USER, appId),
				objectMapper.writeValueAsString(
					UnlogoffAppUserMessage.builder()
						.appId(appId)
						.userId(modified.getUserId())
						.nickname(modified.getNickname())
						.accountId(modified.getAccountId())
						.eventAppUserId(CairoSecurityContextHolder.getSubappUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}

		cairoAuthAppUserService.removeAppUserCache(appId, args.getUserId());
	}

	/**
	 * 删除应用级用户
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_app_user", keys = {"#appId", "#args.userId"})
	@BizLog(
		bizId = "app_user:delete_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void deleteAppUser(@Valid @NotNull String appId, @Validated DeleteAppUserArgs args) {
		AppUserMongodb deletedAppUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppUserMongodb.FIELD.APP_ID).is(appId)
					.and(AppUserMongodb.FIELD.USER_ID).is(args.getUserId());
				Query query = Query.query(criteria);
				AppUserMongodb userMongodb = mongoTemplate.findOne(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("删除用户失败（用户不存在）");
				}

				// 20240828 关闭管理员检查，因注销流程一定会取消管理员身份，或者脏数据导致用户不能删除在此放开限制
				// if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
				// 	throw new ConflictBusinessException("删除用户失败（系统管理员不允许删除）");
				// }

				if (userMongodb.getLogoffStatus() != null && !userMongodb.getLogoffStatus().equals(AppUserLogoffStatus.SUCCESS.getLogoffStatusValue())) {
					throw new ConflictBusinessException("删除用户失败（请先完成注销用户流程）");
				}
				Update update = new Update();
				update.set(AppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				log.trace("updateResult:" + updateResult);
				AppUserMongodb appUserMongodb = mongoTemplate.findAndRemove(query, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
				if (appUserMongodb != null) {
					mongoTemplate.insert(appUserMongodb, MongodbConstants.DeletedCollection.APP_USER);
				}

				return appUserMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用级用户失败");
			}
		});

		if (deletedAppUserMongodb != null) {
			// remove cache
			cairoAuthAppUserService.removeAppUserCache(appId, args.getUserId());
			// 发送删除应用级用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_APP_USER, appId),
				objectMapper.writeValueAsString(
					DeletedAppUserMessage.builder()
						.appId(appId)
						.userId(deletedAppUserMongodb.getUserId())
						.nickname(deletedAppUserMongodb.getNickname())
						.accountId(deletedAppUserMongodb.getAccountId())
						.eventAppUserId("应用级用户")
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}


	Criteria buildCriteria(String appId, GetAppUserListArgs args) {
		Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isEmpty())
			.map(x -> new Criteria[]{
				Criteria.where(AppUserMongodb.FIELD.USER_ID).regex(x),
				Criteria.where(AppUserMongodb.FIELD.NICKNAME).regex(x),
				Criteria.where(AppUserMongodb.FIELD.PHONE_NUMBER).regex(x),
				Criteria.where(AppUserMongodb.FIELD.ACCOUNT_ID).regex(x),
			}).ifPresent(criteria::orOperator);

		if (args.getAccountIds() != null && !args.getAccountIds().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getUserIds() != null && !args.getUserIds().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.USER_ID).in(args.getUserIds());
		}

		if (args.getDepartmentIds() != null && !args.getDepartmentIds().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.DEPARTMENT_IDS).in(args.getDepartmentIds());
		}

		if (args.getRoleIds() != null && !args.getRoleIds().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.ROLE_IDS).in(args.getRoleIds());
		}

		if (args.getTagIds() != null && !args.getTagIds().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.TAG_IDS).in(args.getTagIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(AppUserMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getLogoffStatuses() != null && !args.getLogoffStatuses().isEmpty()) {
			criteria.and(AppUserMongodb.FIELD.LOGOFF_STATUS).in(args.getLogoffStatuses());
		}

		return criteria;
	}

	@NewSpan
	protected List<AppUserMetadata> getAppUserList(MongoTemplate template, String appId, List<AppUserMongodb> ms, Map<String, String> extensionMap) {
		Set<String> metadataAppUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppUserMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataAppUserMap = Optional.of(metadataAppUserIds)
			.filter(app_userIds -> !app_userIds.isEmpty())
			.map(app_userIds -> appUserCommonService.getAppUserMapByAppUserIds(appId, app_userIds))
			.orElse(Collections.emptyMap());


		List<String> accountIds = ms.stream().map(AppUserMongodb::getAccountId).distinct().collect(Collectors.toList());
		Map<String, AccountMongodb> accountMap = Optional.of(accountIds).filter(x -> !x.isEmpty())
			.map(ids -> {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
				Query accountQuery = Query.query(accountCriteria);
				return template.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT).stream().collect(toMap(AccountMongodb::getAccountId, x -> x));
			}).orElse(Collections.emptyMap());

		AppUserExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.APP_USER)).map(AppUserExtension::valueOf).orElse(AppUserExtension.ALL);

		Map<String, AppRole> roleMap = Optional.of(extension.fields())
			.filter(x -> x.contains(AppUserField.ROLE))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getRoleIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> roleCommonService.getRoleList(appId, x).stream().collect(Collectors.toMap(AppRole::getRoleId, z -> z)))
			.orElse(Collections.emptyMap());

		Map<String, PathAppDepartment> departmentMap = Optional.of(extension.fields())
			.filter(x -> x.contains(AppUserField.DEPARTMENT))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getDepartmentIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> departmentCommonService.getPathAppDepartmentMap(appId, x))
			.orElse(Collections.emptyMap());

		Map<String, AppUserTag> tagMap = Optional.of(extension.fields())
			.filter(x -> x.contains(AppUserField.TAG))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getTagIds()).orElse(Collections.emptyList()).stream()).distinct().collect(Collectors.toList()))
			.map(x -> appUserTagCommonService.getAppUserTagListByTagIds(appId, x).stream().collect(toMap(AppUserTag::getTagId, y -> y)))
			.orElse(Collections.emptyMap());


		return ms.stream().map(m -> AppUserConverter.convertMetadataAppUser(
				m,
				roleMap,
				departmentMap,
				tagMap,
				accountMap,
				metadataAppUserMap,
				extension)
			)
			.collect(Collectors.toList());
	}

}
