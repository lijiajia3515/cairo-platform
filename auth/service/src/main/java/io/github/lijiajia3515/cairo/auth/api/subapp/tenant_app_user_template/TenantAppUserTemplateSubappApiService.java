package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_user_template;

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
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template.TenantAppDepartmentTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role_template.TenantAppRoleTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.MetadataTenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template.TenantAppUserTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template.TenantAppUserTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.CreateAccountAndTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.CreateTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.DeleteTenantAppUserTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.ModifyTenantAppUserTemplateInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_user_template.ModifyTenantAppUserTemplateStatusArgs;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


/**
 * [subapp_user/api] tenant_app_user_template service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTemplateSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final TenantAppDepartmentTemplateCommonService tenantDepartmentTemplateCommonService;
	private final TenantAppRoleTemplateCommonService tenantAppRoleTemplateCommonService;
	private final AccountCommonService accountCommonService;
	private final TenantAppUserTemplateCommonService tenantAppUserTemplateCommonService;
	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;
	private final AppUserCommonService appUserCommonService;

	public TenantAppUserTemplateSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													 TransactionTemplate transactionTemplate,
													 RabbitTemplate rabbitTemplate,
													 CairoRabbitmqTool cairoRabbitmqTool,
													 TenantAppDepartmentTemplateCommonService tenantDepartmentTemplateCommonService,
													 TenantAppRoleTemplateCommonService tenantAppRoleTemplateCommonService,
													 AccountCommonService accountCommonService,
													 TenantAppUserTemplateCommonService tenantAppUserTemplateCommonService,
													 AuthProperties authProperties,
													 ObjectMapper objectMapper,
													 AppUserCommonService appUserCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.tenantDepartmentTemplateCommonService = tenantDepartmentTemplateCommonService;
		this.tenantAppRoleTemplateCommonService = tenantAppRoleTemplateCommonService;
		this.accountCommonService = accountCommonService;
		this.tenantAppUserTemplateCommonService = tenantAppUserTemplateCommonService;
		this.authProperties = authProperties;
		this.objectMapper = objectMapper;
		this.appUserCommonService = appUserCommonService;
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
		bizId = "tenant_app_user_template:get_tenant_app_user_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<MetadataTenantAppUserTemplate> getTenantAppUserTemplateList(@Valid @NotNull String appId, @Validated GetTenantAppUserTemplateListArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserTemplateMongodb> users = mongoTemplate.find(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);

		return getTenantAppUserTemplateList(readMongoTemplate, appId, users, args.getExtension());
	}


	/**
	 * 获取企业用户模板列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 企业用户模板分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_template:get_tenant_app_user_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataTenantAppUserTemplate> getUserPageList(@Valid @NotNull String appId, @Validated GetTenantAppUserTemplateListArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = mongoTemplate.count(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<TenantAppUserTemplateMongodb> ms = mongoTemplate.find(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);

		List<MetadataTenantAppUserTemplate> contents = getTenantAppUserTemplateList(readMongoTemplate, appId, ms, args.getExtension());
		return new Page<>(args, contents, total);
	}

	/**
	 * 根据企业用户模板ID获取企业用户模板
	 *
	 * @return return tenant_app_user_template
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_template:get_tenant_app_user_template_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public MetadataTenantAppUserTemplate getTenantAppUserTemplateInfo(@Valid @NotNull String appId, @Valid @NotNull String userId) {
		return getTenantAppUserTemplateInfo(readMongoTemplate, appId, userId)
			.orElseThrow(() -> new ConflictBusinessException("企业用户模板不存在"));
	}


	/**
	 * 获取企业用户模板根据企业用户模板ID
	 *
	 * @return userId
	 */
	@NewSpan
	public Optional<MetadataTenantAppUserTemplate> getTenantAppUserTemplateInfo(MongoTemplate template, String appId, @Valid @NotNull String userId) {
		Criteria criteria = Criteria
			.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).is(userId);

		Query query = Query.query(criteria);
		return Optional.ofNullable(template.findOne(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE))
			.flatMap(m -> getTenantAppUserTemplateList(template, appId, Collections.singletonList(m), Collections.singletonMap(CairoAuthExtensionConstants.TENANT_APP_USER_TEMPLATE, TenantAppUserTemplateExtension.FULL_INFO.name()))
				.stream().findFirst());
	}

	/**
	 * 后台 新增企业用户模板
	 *
	 * @param args args
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user_template:create_tenant_app_user_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createTenantAppUserTemplate(@Valid @NotNull String appId, @Validated CreateTenantAppUserTemplateArgs args) {
		Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		AccountMongodb account = readMongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) {
			throw new ConflictBusinessException("账号不存在");
		}

		Criteria criteria = Criteria
			.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		TenantAppUserTemplateMongodb existsTenantAppUserTemplateMongodb = readMongoTemplate.findOne(Query.query(criteria), TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
		if (existsTenantAppUserTemplateMongodb != null) {
			throw new ConflictBusinessException(String.format("该账号已绑定企业用户模板: %s(%s)", existsTenantAppUserTemplateMongodb.getNickname(), existsTenantAppUserTemplateMongodb.getTenantAppUserTemplateId()));
		}

		Criteria appCriteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).is(appId)
			.and(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(args.getAccountId());
		Query appQuery = Query.query(appCriteria);
		boolean admin = readMongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

		TenantAppUserTemplateMongodb insertedTenantAppUserTemplate = transactionTemplate.execute(status -> {
			try {
				TenantAppUserTemplateMongodb insertTenantAppUserTemplate = TenantAppUserTemplateMongodb.builder()
					.appId(appId)
					.tenantAppUserTemplateId(tenantAppUserTemplateCommonService.getNewTenantAppUserTemplateId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(account.getNickname()))
					.phoneNumber(Optional.ofNullable(args.getPhoneNumber()).orElse(account.getPhoneNumber()))
					.enabled(true)
					.admin(admin)
					.tenantAppDepartmentTemplateIds(args.getTenantAppDepartmentTemplateIds())
					.tenantAppRoleTemplateIds(args.getTenantAppRoleTemplateIds())
					.accountId(account.getAccountId())
					.position(args.getPosition())
					.tenantMainDepartmentTemplateId(args.getTenantMainDepartmentTemplateId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				return mongoTemplate.insert(insertTenantAppUserTemplate, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
			} catch (Exception e) {
				log.debug("createTenantAppUserTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业用户模板失败");
			}
		});

		if (insertedTenantAppUserTemplate == null) {
			throw new ConflictBusinessException("创建企业用户模板失败");
		}
	}

	/**
	 * 创建账号并且创建企业用户模板
	 *
	 * @param appId 应用id
	 * @param args  参数
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "tenant_app_user_template:create_account_and_tenant_app_user_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAccountAndTenantAppUserTemplate(@Valid @NotNull String appId, @Validated CreateAccountAndTenantAppUserTemplateArgs args) {
		Criteria accountCriteria = new Criteria();
		boolean phoneNumber = false, tenant_app_user_templatename = false, email = false;
		List<Criteria> criteriaList = new ArrayList<>();
		if (args.getPhoneNumber() != null && !args.getPhoneNumber().trim().isBlank()) {
			phoneNumber = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber().trim()));
		}
		if (args.getUsername() != null && !args.getUsername().trim().isBlank()) {
			tenant_app_user_templatename = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.USERNAME).is(args.getUsername().trim()));
		}
		if (args.getEmail() != null && !args.getEmail().trim().isBlank()) {
			email = true;
			criteriaList.add(Criteria.where(AccountMongodb.FIELD.EMAIL).is(args.getEmail().trim()));
		}

		if (!(phoneNumber || tenant_app_user_templatename || email)) {
			throw new ConflictBusinessException("手机号，企业用户模板名，邮箱，必须三选一");
		}

		// 验证企业用户模板名格式
		if (args.getUsername() != null && !args.getUsername().trim().isBlank() && !AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("企业用户模板名格式错误");
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
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		} else {
			// 获取账号
			accountMongodb = mongoTemplate.findOne(Query.query(accountCriteria), AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
			if (accountMongodb == null) {
				throw new ConflictBusinessException("账号查询失败");
			}
		}

		// 创建企业用户模板
		TenantAppUserTemplateMongodb insertedTenantAppUserTemplateMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).is(accountMongodb.getAccountId());
				TenantAppUserTemplateMongodb existsTenantAppUserTemplate = mongoTemplate.findOne(Query.query(criteria), TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
				if (existsTenantAppUserTemplate != null) {
					throw new ConflictBusinessException(
						String.format("账号信息【%s(%s)】已被用户【%s(%s)】使用",
							Optional.ofNullable(accountMongodb.getPhoneNumber()).orElse(Optional.ofNullable(accountMongodb.getUsername()).orElse(accountMongodb.getAccountId())),
							accountMongodb.getAccountId(),
							Optional.ofNullable(existsTenantAppUserTemplate.getNickname()).orElse(existsTenantAppUserTemplate.getTenantAppUserTemplateId()),
							existsTenantAppUserTemplate.getTenantAppUserTemplateId())
					);
				}
				Criteria appCriteria = Criteria
					.where(AppMongodb.FIELD.APP_ID).is(appId)
					.and(AppMongodb.FIELD.ADMIN_ACCOUNT_IDS).in(accountMongodb.getAccountId());
				Query appQuery = Query.query(appCriteria);
				boolean admin = readMongoTemplate.exists(appQuery, AppMongodb.class, MongodbConstants.Collection.APP);

				TenantAppUserTemplateMongodb user = TenantAppUserTemplateMongodb.builder()
					.appId(appId)
					.tenantAppUserTemplateId(tenantAppUserTemplateCommonService.getNewTenantAppUserTemplateId())
					.nickname(Optional.ofNullable(args.getNickname()).orElse(accountMongodb.getNickname()))
					.phoneNumber(accountMongodb.getPhoneNumber())
					.tenantAppRoleTemplateIds(args.getTenantAppRoleTemplateIds())
					.tenantAppDepartmentTemplateIds(args.getTenantAppDepartmentTemplateIds())
					.position(args.getPosition())
					.tenantMainDepartmentTemplateId(args.getTenantMainDepartmentTemplateId())
					.enabled(true)
					.admin(admin)
					.accountId(accountMongodb.getAccountId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(user, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("创建企业用户模板失败", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建企业用户模板失败");
			}
		});

	}

	/**
	 * 修改 企业应用用户角色,岗位,部门
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_template_info", keys = {"#appId", "#args.tenantAppUserTemplateId"})
	@BizLog(
		bizId = "tenant_app_user_template:modify_tenant_app_user_template_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppUserTemplateInfo(@Valid @NotNull String appId, @Validated ModifyTenantAppUserTemplateInfoArgs args) {
		TenantAppUserTemplateMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).is(args.getTenantAppUserTemplateId()));

				Update update = Update
					.update(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId())
					.set(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
				Optional.ofNullable(args.getNickname()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.NICKNAME, x));
				Optional.ofNullable(args.getPhoneNumber()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.PHONE_NUMBER, x));
				Optional.ofNullable(args.getTenantAppRoleTemplateIds()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_IDS, x));
				Optional.ofNullable(args.getTenantAppDepartmentTemplateIds()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_IDS, x));
				Optional.ofNullable(args.getPosition()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.POSITION, x));
				Optional.ofNullable(args.getTenantMainDepartmentTemplateId()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.TENANT_MAIN_DEPARTMENT_TEMPLATE_ID, x));
				return mongoTemplate.findAndModify(query, update, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
			} catch (Exception e) {
				log.debug("modifyTenantAppUserTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("企业用户模板修改失败");
			}
		});

		if (userMongodb == null) {
			throw new ConflictBusinessException("企业用户模板修改失败");
		}
	}


	@NewSpan
	@Lock4j(name = "modify_tenant_app_user_template_status", keys = {"#appId", "#args.tenantAppUserTemplateId"})
	@BizLog(
		bizId = "tenant_app_user_template:modify_tenant_app_user_template_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppUserTemplateStatus(@Valid @NotNull String appId, @Validated ModifyTenantAppUserTemplateStatusArgs args) {
		TenantAppUserTemplateMongodb userMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).is(args.getTenantAppUserTemplateId())
				);
				TenantAppUserTemplateMongodb tenantAppUserTemplateMongodb = mongoTemplate.findOne(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
				if (tenantAppUserTemplateMongodb == null) {
					throw new ConflictBusinessException("企业用户模板不存在");
				}

				if (tenantAppUserTemplateMongodb.getAdmin() != null && tenantAppUserTemplateMongodb.getAdmin()) {
					throw new ConflictBusinessException("修改企业用户模板状态失败，请联系平台管理员移除当前操作账号管理员权限后再试");
				}

				Update update = new Update();
				update.set(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				Optional.ofNullable(args.getEnabled()).ifPresent(x -> update.set(TenantAppUserTemplateMongodb.FIELD.ENABLED, x));

				FindAndModifyOptions findAndModifyOptions = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, findAndModifyOptions, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
			} catch (Exception e) {
				log.info("modifyTenantAppUserTemplateStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业用户模板状态失败");
			}
		});

		if (userMongodb == null) {
			throw new ConflictBusinessException("修改企业用户模板状态失败");
		}
	}

	/**
	 * 删除企业用户模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_user_template", keys = {"#appId", "#args.tenantAppUserTemplateId"})
	@BizLog(
		bizId = "tenant_app_user_template:delete_tenant_app_user_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	public void deleteTenantAppUserTemplate(@Valid @NotNull String appId, @Validated DeleteTenantAppUserTemplateArgs args) {
		TenantAppUserTemplateMongodb deletedTenantAppUserTemplateMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).is(args.getTenantAppUserTemplateId());
				Query query = Query.query(criteria);
				TenantAppUserTemplateMongodb userMongodb = mongoTemplate.findOne(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
				if (userMongodb == null) {
					throw new ConflictBusinessException("删除用户失败（用户不存在）");
				}

				Update update = new Update();
				update.set(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
				log.trace("updateResult:" + updateResult);
				TenantAppUserTemplateMongodb tenantAppUserTemplateMongodb = mongoTemplate.findAndRemove(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);
				if (tenantAppUserTemplateMongodb != null) {
					mongoTemplate.insert(tenantAppUserTemplateMongodb, MongodbConstants.DeletedCollection.TENANT_APP_USER_TEMPLATE);
				}

				return tenantAppUserTemplateMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteTenantAppUserTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业用户模板失败");
			}
		});

	}


	Criteria buildCriteria(String appId, GetTenantAppUserTemplateListArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isEmpty())
			.map(x -> new Criteria[]{
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.NICKNAME).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.PHONE_NUMBER).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).regex(x),
			}).ifPresent(criteria::orOperator);

		if (args.getAccountIds() != null && !args.getAccountIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getTenantAppUserTemplateIds() != null && !args.getTenantAppUserTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).in(args.getTenantAppUserTemplateIds());
		}

		if (args.getTenantAppDepartmentTemplateIds() != null && !args.getTenantAppDepartmentTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_IDS).in(args.getTenantAppDepartmentTemplateIds());
		}

		if (args.getTenantAppRoleTemplateIds() != null && !args.getTenantAppRoleTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_IDS).in(args.getTenantAppRoleTemplateIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		return criteria;
	}

	@NewSpan
	protected List<MetadataTenantAppUserTemplate> getTenantAppUserTemplateList(MongoTemplate template, String appId, List<TenantAppUserTemplateMongodb> ms, Map<String, String> extensionMap) {
		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(TenantAppUserTemplateMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(appId, userIds))
			.orElse(Collections.emptyMap());

		List<String> accountIds = ms.stream().map(TenantAppUserTemplateMongodb::getAccountId).distinct().collect(Collectors.toList());
		Map<String, AccountMongodb> accountMap = Optional.of(accountIds).filter(x -> !x.isEmpty())
			.map(ids -> {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
				Query accountQuery = Query.query(accountCriteria);
				return template.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT).stream().collect(toMap(AccountMongodb::getAccountId, x -> x));
			}).orElse(Collections.emptyMap());

		TenantAppUserTemplateExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.TENANT_APP_ROLE_TEMPLATE)).map(TenantAppUserTemplateExtension::valueOf).orElse(TenantAppUserTemplateExtension.ALL);

		Map<String, TenantAppRoleTemplate> roleMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserTemplateField.ROLE))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getTenantAppRoleTemplateIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantAppRoleTemplateCommonService.getTenantAppRoleTemplateList(appId, x).stream().collect(Collectors.toMap(TenantAppRoleTemplate::getTenantAppRoleTemplateId, z -> z)))
			.orElse(Collections.emptyMap());

		Map<String, PathTenantAppDepartmentTemplate> departmentMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserTemplateField.DEPARTMENT))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getTenantAppDepartmentTemplateIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantDepartmentTemplateCommonService.getPathTenantAppDepartmentTemplateMap(appId, x))
			.orElse(Collections.emptyMap());


		return ms.stream().map(m -> TenantAppUserTemplateConverter.convertMetadataTenantAppUserTemplate(
				m,
				roleMap,
				departmentMap,
				accountMap,
				metadataUserMap,
				extension)
			)
			.collect(Collectors.toList());
	}

}
