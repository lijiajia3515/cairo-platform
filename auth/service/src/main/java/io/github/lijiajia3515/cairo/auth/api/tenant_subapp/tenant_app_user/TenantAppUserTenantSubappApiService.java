package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.MetadataTenantAppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUserExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUserField;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.DeletedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.LogoffSuccessTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.UnlogoffTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.CreateAccountAndTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.CreateTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.DeleteTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.GetTenantAppUserListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.LogoffTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.ModifyTenantAppUserInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.ModifyTenantAppUserStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.TransferTenantAppUserToOtherAccountArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user.UnlogoffTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_tag.TenantAppUserTag;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_tag.TenantAppUserTagCommonService;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


/**
 * [tenant_subapp_user/api] tenant app user service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTenantSubappApiService {
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

	public TenantAppUserTenantSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
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

	/**
	 * getUserList
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return user list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:get_tenant_app_user_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<MetadataTenantAppUser> getTenantAppUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetTenantAppUserListArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserMongodb> users = mongoTemplate.find(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		return getTenantAppUserList(readMongoTemplate, tenantId, appId, users, args.getExtension());
	}


	/**
	 * 获取用户列表
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return 用户分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:get_tenant_app_user_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataTenantAppUser> getTenantAppUserPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetTenantAppUserListArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);

		long total = mongoTemplate.count(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<TenantAppUserMongodb> ms = mongoTemplate.find(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);

		List<MetadataTenantAppUser> contents = getTenantAppUserList(readMongoTemplate, tenantId, appId, ms, args.getExtension());
		return new Page<>(args, contents, total);
	}

	/**
	 * 根据用户ID获取用户
	 *
	 * @return return user
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:get_tenant_app_user_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public MetadataTenantAppUser getTenantAppUserInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId) {
		return getTenantAppUserInfo(readMongoTemplate, tenantId, appId, userId)
			.orElseThrow(() -> new ConflictBusinessException("用户不存在"));
	}


	public Optional<MetadataTenantAppUser> getTenantAppUserInfo(MongoTemplate template, String tenantId, String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId);

		Query query = Query.query(criteria);
		return Optional.ofNullable(template.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER))
			.flatMap(m -> getTenantAppUserList(template, tenantId, appId, Collections.singletonList(m), Collections.singletonMap(CairoAuthExtensionConstants.USER, TenantAppUserExtension.FULL_INFO.name()))
				.stream().findFirst());
	}

	/**
	 * 后台 新增用户
	 *
	 * @param args args
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user:create_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated CreateTenantAppUserArgs args) {
		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		AccountMongodb account = readMongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new ConflictBusinessException("账号不存在");
		}

		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		TenantAppUserMongodb existsUserMongodb = readMongoTemplate.findOne(Query.query(criteria), TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		if (existsUserMongodb != null) {
			throw new ConflictBusinessException(String.format("该账号已绑定用户: %s(%s)", existsUserMongodb.getNickname(), existsUserMongodb.getUserId()));
		}

		Criteria tenantAppCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(args.getAccountId());
		Query tenantAppQuery = Query.query(tenantAppCriteria);
		boolean admin = readMongoTemplate.exists(tenantAppQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

		TenantAppUserMongodb insertedUser = transactionTemplate.execute(status -> {
			try {
				TenantAppUserMongodb insertUser = TenantAppUserMongodb.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userId(tenantAppUserCommonService.getNewUserId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(account.getNickname()))
					.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).filter(x -> !x.trim().isEmpty()).orElse(null))
					.admin(admin)
					.roleIds(args.getRoleIds())
					.position(args.getPosition())
					.mainDepartmentId(args.getMainDepartmentId())
					.departmentIds(args.getDepartmentIds())
					.tagIds(args.getTagIds())
					.enabled(true)
					.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
					.joinTime(LocalDateTime.now())
					.accountId(account.getAccountId())
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.build())
					.build();
				return mongoTemplate.insert(insertUser, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建用户失败");
			}
		});

		if (insertedUser == null) {
			throw new ConflictBusinessException("创建用户失败");
		}

		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, tenantId, appId),
			objectMapper.writeValueAsString(
				CreatedTenantAppUserMessage.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userId(insertedUser.getUserId())
					.nickname(insertedUser.getNickname())
					.admin(insertedUser.getAdmin())
					.accountId(insertedUser.getAccountId())
					.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.eventTime(LocalDateTime.now())
					.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 创建账号并且创建用户
	 *
	 * @param tenantId 租户id
	 * @param appId    应用id
	 * @param args     参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user:create_account_and_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAccountAndTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated CreateAccountAndTenantAppUserArgs args) {
		Criteria accountCriteria = new Criteria();
		boolean phoneNumber = false, username = false, email = false;
		List<Criteria> criteriaList = new ArrayList<>();
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank()) {
			phoneNumber = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber().trim()));
		}
		if (args.getUsername() != null && !args.getUsername().trim().isBlank()) {
			username = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.USERNAME).is(args.getUsername().trim()));
		}
		if (args.getEmail() != null && !args.getEmail().trim().isBlank()) {
			email = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.EMAIL).is(args.getEmail().trim()));
		}

		if (!(phoneNumber || username || email)) {
			throw new ConflictBusinessException("手机号，用户名，邮箱，必须三选一");
		}

		// 验证用户名格式
		if (args.getUsername() != null && !args.getUsername().trim().isBlank() && !AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("用户名格式错误");
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
				.username((Optional.ofNullable(args.getUsername()).filter(x -> !x.trim().isEmpty()).orElse(null)))
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
						.eventAccountId(CairoSecurityContextHolder.getAccountId())
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

		Criteria tenantAppCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(accountMongodb.getAccountId());
		Query tenantAppQuery = Query.query(tenantAppCriteria);
		boolean admin = readMongoTemplate.exists(tenantAppQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

		// 创建用户
		TenantAppUserMongodb insertedUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId());
				TenantAppUserMongodb existsUser = mongoTemplate.findOne(Query.query(criteria), TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (existsUser != null) {
					throw new ConflictBusinessException(
						String.format("账号信息【%s(%s)】已被用户【%s(%s)】使用",
							Optional.ofNullable(accountMongodb.getPhoneNumber()).orElse(Optional.ofNullable(accountMongodb.getUsername()).orElse(accountMongodb.getAccountId())),
							accountMongodb.getAccountId(),
							Optional.ofNullable(existsUser.getNickname()).orElse(existsUser.getUserId()),
							existsUser.getUserId())
					);
				}

				TenantAppUserMongodb user = TenantAppUserMongodb.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userId(tenantAppUserCommonService.getNewUserId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(accountMongodb.getNickname()))
					.phoneNumber(accountMongodb.getPhoneNumber())
					.admin(admin)
					.roleIds(args.getRoleIds())
					.position(args.getPosition())
					.mainDepartmentId(args.getMainDepartmentId())
					.departmentIds(args.getDepartmentIds())
					.tagIds(args.getTagIds())
					.enabled(true)
					.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
					.joinTime(LocalDateTime.now())
					.accountId(accountMongodb.getAccountId())
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(user, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("创建用户失败", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建用户失败");
			}
		});

		if (insertedUserMongodb != null) {
			// 发送创建用户消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, tenantId, appId),
				objectMapper.writeValueAsString(
					CreatedTenantAppUserMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.userId(insertedUserMongodb.getUserId())
						.nickname(insertedUserMongodb.getNickname())
						.admin(insertedUserMongodb.getAdmin())
						.accountId(insertedUserMongodb.getAccountId())
						.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
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
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_info", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:modify_tenant_app_user_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppUserInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyTenantAppUserInfoArgs args) {
		TenantAppUserMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId()));

				Update update = Update
					.update(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId())
					.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
				Optional.ofNullable(args.getNickname()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.NICKNAME, x));
				Optional.ofNullable(args.getPhoneNumber()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.PHONE_NUMBER, x));
				Optional.ofNullable(args.getRoleIds()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.ROLE_IDS, x));
				Optional.ofNullable(args.getTagIds()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.TAG_IDS, x));
				Optional.ofNullable(args.getDepartmentIds()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.DEPARTMENT_IDS, x));
				Optional.ofNullable(args.getPosition()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.POSITION, x));
				Optional.ofNullable(args.getMainDepartmentId()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.MAIN_DEPARTMENT_ID, x));
				return mongoTemplate.findAndModify(query, update, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("用户修改失败");
			}
		});

		if (userMongodb == null) {
			throw new ConflictBusinessException("用户修改失败");
		}
	}


	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_status", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:modify_tenant_app_user_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppUserStatus(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyTenantAppUserStatusArgs args) {
		TenantAppUserMongodb modifiedUserMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId())
				);
				TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("用户不存在");
				}

				if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
					throw new ConflictBusinessException("修改用户状态失败，请联系平台管理员移除当前操作账号管理员权限后再试");
				}

				Update update = new Update();
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(TenantAppUserMongodb.FIELD.ENABLED, x));

				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, findAndModifyOptions, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyUserStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改用户状态失败");
			}
		});

		if (modifiedUserMongodb == null) {
			throw new ConflictBusinessException("修改用户状态失败");
		}
	}

	@NewSpan
	@Lock4j(name = "transfer_tenant_app_user_to_other_account", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:transfer_tenant_app_user_to_other_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void transferTenantAppUserToOtherAccount(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated TransferTenantAppUserToOtherAccountArgs args) {
		Account account = accountCommonService.getAccount(args.getOtherAccountId());
		if (account == null) {
			throw new ConflictBusinessException("转移用户失败（转移账号不存在）");
		}
		TenantAppUserMongodb modifiedUserMongodb = transactionTemplate.execute(status -> {
			try {
				Query userQuery = Query.query(Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId())
				);
				userQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.ACCOUNT_ID, TenantAppUserMongodb.FIELD.NICKNAME, TenantAppUserMongodb.FIELD.ADMIN);
				TenantAppUserMongodb userMongodb = mongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("转移用户失败（用户不存在）");
				}

				if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
					throw new ConflictBusinessException("转移用户失败(企业管理员不可转移)");
				}

				if (userMongodb.getAccountId() != null && userMongodb.getAccountId().equals(args.getOtherAccountId())) {
					throw new ConflictBusinessException("转移用户失败（已转移至该账号）");
				}

				if (userMongodb.getLogoffStatus() != null && userMongodb.getLogoffStatus().equals(TenantAppUserLogoffStatus.PENDING.getLogoffStatusValue())) {
					throw new ConflictBusinessException("转移用户失败(注销中用户不可转移)");
				}

				Query otherAccountUserQuery = Query.query(Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).is(args.getOtherAccountId())
				);
				otherAccountUserQuery.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.ACCOUNT_ID, TenantAppUserMongodb.FIELD.NICKNAME);
				TenantAppUserMongodb otherAccountUserUserMongodb = mongoTemplate.findOne(otherAccountUserQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (otherAccountUserUserMongodb != null) {
					throw new ConflictBusinessException(String.format("转移用户失败（绑定账号已存在用户【%s(%s)】)", otherAccountUserUserMongodb.getNickname(), otherAccountUserUserMongodb.getUserId()));
				}

				Update userUpdate = Update.update(TenantAppUserMongodb.FIELD.ACCOUNT_ID, args.getOtherAccountId());
				userUpdate.currentDate(TenantAppUserMongodb.FIELD.TRANSFER_ACCOUNT_TIME);
				userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.NO.getLogoffStatusValue());
				userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_PENDING_TIME, null);
				userUpdate.set(TenantAppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME, null);
				userUpdate.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				userUpdate.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(userQuery, userUpdate, findAndModifyOptions, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("transferUserToOtherAccount", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("转移用户失败");
			}
		});

		if (modifiedUserMongodb == null) {
			throw new ConflictBusinessException("转移用户失败");
		}
	}

	/**
	 * 直接注销用户
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "logoff_tenant_app_user", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:logoff_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void logoffTenantAppUser(@Valid @NotNull String tenantId,
									@Valid @NotNull String appId,
									@Validated LogoffTenantAppUserArgs args) {
		TenantAppUserMongodb logoffSuccessUser = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId());

				Query.query(criteria);
				Query query = Query.query(criteria);

				TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("注销失败（用户不存在）");
				}

				if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
					throw new ConflictBusinessException("注销失败（企业管理员无法注销)）");
				}

				if (userMongodb.getLogoffStatus() != null && Objects.equals(TenantAppUserLogoffStatus.SUCCESS.getLogoffStatusValue(), userMongodb.getLogoffStatus())) {
					throw new ConflictBusinessException("注销失败（无需重复注销）");
				}

				Update update = new Update();
				update.set(TenantAppUserMongodb.FIELD.ADMIN, false); // 注销完成取消管理员身份
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_STATUS, TenantAppUserLogoffStatus.SUCCESS.getLogoffStatusValue());
				update.currentDate(TenantAppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME);
				update.set(TenantAppUserMongodb.FIELD.ACCOUNT_ID, null);
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				TenantAppUserMongodb modifiedUserMongodb = mongoTemplate.findAndModify(query, update, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (modifiedUserMongodb == null) {
					throw new ConflictBusinessException("注销用户失败");
				}
				// 将老账号ID，返回给外部发送消息到队列中
				modifiedUserMongodb.setAccountId(userMongodb.getAccountId());
				return modifiedUserMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffTenantAppUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("注销用户失败");
			}
		});
		if (logoffSuccessUser == null) {
			throw new ConflictBusinessException("注销用户失败");
		}

		// 发送注销用户成功消息
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.LOGOFF_SUCCESS_TENANT_APP_USER, tenantId, appId),
			objectMapper.writeValueAsString(
				LogoffSuccessTenantAppUserMessage.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userId(logoffSuccessUser.getUserId())
					.nickname(logoffSuccessUser.getNickname())
					.accountId(logoffSuccessUser.getAccountId())
					.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.eventTime(LocalDateTime.now())
					.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}

	/**
	 * 取消注销应用用户
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "unlogoff_tenant_app_user", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:unlogoff_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void unlogoffTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated UnlogoffTenantAppUserArgs args) {
		TenantAppUserMongodb modified = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId());

				Query.query(criteria);
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
				update.set(TenantAppUserMongodb.FIELD.LOGOFF_SUCCESS_TIME, null);
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

				TenantAppUserMongodb modifiedTenantAppUserMongodb = mongoTemplate.findAndModify(query, update, options, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (modifiedTenantAppUserMongodb == null) {
					throw new ConflictBusinessException("取消注销失败");
				}
				return modifiedTenantAppUserMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("unlogoffTenantAppUser", e);
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


	/**
	 * 删除用户，将用户设置为无名用户
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_user", keys = {"#tenantId", "#appId", "#args.userId"})
	@BizLog(
		bizId = "tenant_app_user:delete_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void deleteTenantAppUser(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated DeleteTenantAppUserArgs
		args) {
		TenantAppUserMongodb deletedUserMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserMongodb.FIELD.USER_ID).is(args.getUserId());
				Query query = Query.query(criteria);
				TenantAppUserMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("删除用户失败（用户不存在）");
				}
				// 20240828 关闭管理员检查，因注销流程一定会取消管理员身份，或者脏数据导致用户不能删除在此放开限制
				//	if (userMongodb.getAdmin() != null && userMongodb.getAdmin()) {
				//	    throw new ConflictBusinessException("删除用户失败（企业管理员无法注销）");
				//	}

				if (userMongodb.getLogoffStatus() != null && !userMongodb.getLogoffStatus().equals(TenantAppUserLogoffStatus.SUCCESS.getLogoffStatusValue())) {
					throw new ConflictBusinessException("删除用户失败（请先完成注销用户流程）");
				}

				Update update = new Update();
				update.set(TenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				userMongodb = mongoTemplate.findAndRemove(query, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
				if (userMongodb == null) {
					throw new ConflictBusinessException("删除用户失败，用户不存在");
				}

				mongoTemplate.insert(userMongodb, MongodbConstants.DeletedCollection.TENANT_APP_USER);
				return userMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteUser", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除用户失败");
			}
		});

		// 发送删除用户消息
		if (deletedUserMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.DELETED_TENANT_APP_USER, tenantId, appId),
				objectMapper.writeValueAsString(
					DeletedTenantAppUserMessage.builder()
						.tenantId(tenantId)
						.appId(appId)
						.userId(deletedUserMongodb.getUserId())
						.nickname(deletedUserMongodb.getNickname())
						.accountId(deletedUserMongodb.getAccountId())
						.eventUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}


	Criteria buildCriteria(String tenantId, String appId, GetTenantAppUserListArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isBlank())
			.map(x -> new Criteria[]{
				Criteria.where(TenantAppUserMongodb.FIELD.USER_ID).regex(x),
				Criteria.where(TenantAppUserMongodb.FIELD.NICKNAME).regex(x),
				Criteria.where(TenantAppUserMongodb.FIELD.PHONE_NUMBER).regex(x),
				Criteria.where(TenantAppUserMongodb.FIELD.ACCOUNT_ID).regex(x),
			}).ifPresent(criteria::orOperator);

		if (args.getAccountIds() != null && !args.getAccountIds().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getUserIds() != null && !args.getUserIds().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.USER_ID).in(args.getUserIds());
		}

		if (args.getDepartmentIds() != null && !args.getDepartmentIds().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.DEPARTMENT_IDS).in(args.getDepartmentIds());
		}

		if (args.getRoleIds() != null && !args.getRoleIds().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.ROLE_IDS).in(args.getRoleIds());
		}

		if (args.getTagIds() != null && !args.getTagIds().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.TAG_IDS).in(args.getTagIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(TenantAppUserMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getLogoffStatuses() != null && !args.getLogoffStatuses().isEmpty()) {
			criteria.and(TenantAppUserMongodb.FIELD.LOGOFF_STATUS).in(args.getLogoffStatuses());
		}

		return criteria;
	}

	@NewSpan
	protected List<MetadataTenantAppUser> getTenantAppUserList(MongoTemplate template, String tenantId, String appId, List<TenantAppUserMongodb> ms, Map<String, String> extensionMap) {
		Set<String> metadataUserIds = CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(ms.stream().map(TenantAppUserMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, TenantAppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds))
			.orElse(Collections.emptyMap());


		List<String> accountIds = ms.stream().map(TenantAppUserMongodb::getAccountId).distinct().collect(Collectors.toList());
		Map<String, AccountMongodb> accountMap = Optional.of(accountIds).filter(x -> !x.isEmpty())
			.map(ids -> {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
				Query accountQuery = Query.query(accountCriteria);
				return template.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT).stream().collect(toMap(AccountMongodb::getAccountId, x -> x));
			}).orElse(Collections.emptyMap());


		Criteria appCriteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId);

		Query appQuery = Query.query(appCriteria);
		Set<String> appAdminAccountIds = mongoTemplate.find(appQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP).stream()
			.map(TenantAppMongodb::getAdminAccountIds).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toSet());

		TenantAppUserExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.USER)).map(TenantAppUserExtension::valueOf).orElse(TenantAppUserExtension.ALL);

		Map<String, TenantAppRole> roleMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserField.ROLE))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getRoleIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantAppRoleCommonService.getRoleList(tenantId, appId, x).stream().collect(Collectors.toMap(TenantAppRole::getRoleId, z -> z)))
			.orElse(Collections.emptyMap());

		Map<String, PathTenantAppDepartment> departmentMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserField.DEPARTMENT))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getDepartmentIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantAppDepartmentCommonService.getPathDepartmentMap(tenantId, appId, x))
			.orElse(Collections.emptyMap());

		Map<String, TenantAppUserTag> userTagMap = tenantAppUserTagCommonService.getUserTagListByTagIds(tenantId, appId, null).stream()
			.collect(toMap(TenantAppUserTag::getTagId, x -> x));

		return ms.stream().map(m -> TenantAppUserConverter.convertMetadataUser(m, userTagMap, accountMap, metadataUserMap, roleMap, departmentMap, appAdminAccountIds, extension)).collect(Collectors.toList());
	}

}
