package io.github.lijiajia3515.cairo.auth.framework.security.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.message.account.CreatedAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.app_user.CreatedAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant_app_user.CreatedTenantAppUserMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.cairo_auth.AuthProperties;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccount;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserLogoffStatus;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;


@Slf4j
@Validated
@Component
public class CairoAuthCommonService {
	private final ObjectMapper objectMapper;
	private final AuthProperties authProperties;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final AccountCommonService accountCommonService;
	private final AppUserCommonService appUserCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final TransactionTemplate transactionTemplate;

	public CairoAuthCommonService(ObjectMapper objectMapper,
								  @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
								  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								  TransactionTemplate transactionTemplate,
								  RabbitTemplate rabbitTemplate,
								  CairoRabbitmqTool cairoRabbitmqTool,
								  AuthProperties authProperties,
								  AccountCommonService accountCommonService,
								  AppUserCommonService appUserCommonService,
								  TenantAppUserCommonService tenantAppUserCommonService
	) {
		this.objectMapper = objectMapper;
		this.authProperties = authProperties;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.accountCommonService = accountCommonService;
		this.appUserCommonService = appUserCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * 自动注册逻辑
	 *
	 * @return 注册后的账号数据模型
	 */
	@NewSpan
	@SneakyThrows
	public CairoAuthAccount checkAutoRegisterAccountPhoneNumber(String phoneNumber) {
		boolean validPhoneNumber = AccountCommonService.validPhoneNumber(phoneNumber);
		if (authProperties.getAutoRegister() && validPhoneNumber) {
			String accountId = accountCommonService.getNewAccountId();
			AccountMongodb accountMongodb = AccountMongodb.builder()
				.accountId(accountId)
				.phoneNumber(phoneNumber)
				.nickname("p_" + phoneNumber.substring(phoneNumber.length() - 4))
				.avatarUrl(authProperties.getDefaultAvatarUrl())
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
			accountMongodb = mongoTemplate.insert(accountMongodb, MongodbConstants.Collection.ACCOUNT);

			// 发送创建账号消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
				objectMapper.writeValueAsString(
					CreatedAccountMessage.builder()
						.accountId(accountMongodb.getAccountId())
						.nickname(accountMongodb.getNickname())
						.username(accountMongodb.getUsername())
						.phoneNumber(accountMongodb.getPhoneNumber())
						.password(null)
						.eventAccountId(accountMongodb.getAccountId())
						.eventTime(LocalDateTime.now())
						.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);

			return CairoAuthAccount.builder()
				.accountId(accountMongodb.getAccountId())
				.loginname(accountMongodb.getUsername())
				.phoneNumber(accountMongodb.getPhoneNumber())
				.email(accountMongodb.getEmail())
				.nickname(accountMongodb.getNickname())
				.avatarUrl(accountMongodb.getAvatarUrl())
				.enabled(accountMongodb.isEnabled())
				.locked(accountMongodb.isLocked())
				.build();
		}
		return null;
	}


	/**
	 * 检查自动注册
	 *
	 * @param snsInfo 社交登录信息
	 * @return 新注册账号
	 */
	@NewSpan
	@SneakyThrows
	public CairoAuthAccount checkAutoRegisterSns(SnsInfo snsInfo) {
		boolean autoRegister = true;
		Criteria criteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(snsInfo.getProviderId());
		Query query = Query.query(criteria);
		SnsProviderMongodb snsProviderMongodb = readMongoTemplate.findOne(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		if (snsProviderMongodb != null && snsProviderMongodb.getIsAutoRegister() != null && snsProviderMongodb.getIsAutoRegister()) {
			autoRegister = true;
		} else {
			autoRegister = false;
		}

		if (autoRegister) {
			String accountId = accountCommonService.getNewAccountId();
			AccountMongodb accountMongodb = transactionTemplate.execute(status -> {
				try {
					AccountMongodb newAccountMongodb = AccountMongodb.builder()
						.accountId(accountId)
						.nickname(snsInfo.getNickName())
						.avatarUrl(authProperties.getDefaultAvatarUrl())
						.enabled(true)
						.locked(false)
						.logoffStatus(AccountLogoffStatus.NO.getLogoffStatusValue())
						.metadata(AccountMetadataMongodb.builder().createAccountId(accountId).updateAccountId(accountId).build())
						.build();
					AccountSnsMongodb newAccountSnsMongodb = AccountSnsMongodb.builder()
						.recordId(CoreConstants.SNOWFLAKE.nextIdStr())
						.accountId(accountId)
						.snsPartnerId(snsInfo.getPartnerId())
						.snsPartnerOpenId(snsInfo.getPartnerOpenId())
						.nickname(snsInfo.getNickName())
						.avatarUrl(snsInfo.getAvatarUrl())
						.bindTime(LocalDateTime.now())
						.enabled(true)
						.metadata(AccountMetadataMongodb.builder().updateAccountId(accountId).updateAccountId(accountId).build())
						.build();
					mongoTemplate.insert(newAccountMongodb, MongodbConstants.Collection.ACCOUNT);
					mongoTemplate.insert(newAccountSnsMongodb, MongodbConstants.Collection.ACCOUNT_SNS);
					return newAccountMongodb;
				} catch (Exception e) {
					log.warn("第三方账号自动注册失败", e);
					return null;
				}
			});

			if (accountMongodb != null) {
				// 发送创建账号消息
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.CREATED_ACCOUNT),
					objectMapper.writeValueAsString(
						CreatedAccountMessage.builder()
							.accountId(accountMongodb.getAccountId())
							.nickname(accountMongodb.getNickname())
							.username(accountMongodb.getUsername())
							.phoneNumber(accountMongodb.getPhoneNumber())
							.password(null)
							.eventAccountId(accountMongodb.getAccountId())
							.eventTime(LocalDateTime.now())
							.build()
					),
					new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				);
			}
			return CairoAuthAccount.builder()
				.accountId(accountMongodb.getAccountId())
				.loginname(accountMongodb.getUsername())
				.phoneNumber(accountMongodb.getPhoneNumber())
				.email(accountMongodb.getEmail())
				.nickname(accountMongodb.getNickname())
				.avatarUrl(accountMongodb.getAvatarUrl())
				.enabled(accountMongodb.isEnabled())
				.locked(accountMongodb.isLocked())
				.build();
		}
		return null;
	}


	/**
	 * 检查用户自动注册
	 *
	 * @param account  账号
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param clientId clientId
	 * @return 用户数据模型
	 */
	@NewSpan
	@SneakyThrows
	public TenantAppUserMongodb checkTenantAppUserAutoRegister(CairoAuthAccount account, String tenantId, String appId, String clientId) {
		if (account != null) {
			// 自动注册逻辑
			Criteria tenantAppCriteria = Criteria
				.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppMongodb.FIELD.APP_ID).is(appId);
			Query tenantAppQuery = Query.query(tenantAppCriteria);
			TenantAppMongodb tenantAppMongodb = readMongoTemplate.findOne(tenantAppQuery, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);

			if (tenantAppMongodb != null && tenantAppMongodb.getAutoRegister() != null && tenantAppMongodb.getAutoRegister()) {
				boolean admin = Optional.ofNullable(tenantAppMongodb.getAdminAccountIds()).orElse(Collections.emptyList()).contains(account.getAccountId());
				String newUserId = tenantAppUserCommonService.getNewUserId();
				TenantAppUserMongodb user = TenantAppUserMongodb
					.builder()
					.tenantId(tenantId)
					.appId(appId)
					.userId(newUserId)
					.nickname(account.getNickname())
					.phoneNumber(account.getPhoneNumber())
					.admin(admin)
					.roleIds(Collections.emptyList())
					.departmentIds(Collections.emptyList())
					.tagIds(Collections.emptyList())
					.enabled(true)
					.logoffStatus(TenantAppUserLogoffStatus.NO.getLogoffStatusValue())
					.joinTime(LocalDateTime.now())
					.accountId(account.getAccountId())
					.metadata(TenantAppUserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
					.build();
				// 创建用户
				user = mongoTemplate.insert(user, MongodbConstants.Collection.TENANT_APP_USER);
				// 发送用户创建消息
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_TENANT_APP_USER, tenantId, appId),
					objectMapper.writeValueAsString(
						CreatedTenantAppUserMessage.builder()
							.tenantId(tenantId)
							.appId(appId)
							.userId(user.getUserId())
							.nickname(user.getNickname())
							.admin(user.getAdmin())
							.accountId(user.getAccountId())
							.eventUserId(user.getUserId())
							.eventTime(LocalDateTime.now())
							.build()
					),
					new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				);
				return user;
			}
		}
		return null;
	}

	/**
	 * 检查应用用户自动注册
	 *
	 * @param account  账号
	 * @param appId    appId
	 * @param clientId clientId
	 * @return 用户数据模型
	 */
	@NewSpan
	@SneakyThrows
	public AppUserMongodb checkAppUserAutoRegister(CairoAuthAccount account, String appId, String clientId) {
		if (account != null) {
			// 自动注册逻辑
			Criteria appCriteria = Criteria
				.where(AppMongodb.FIELD.APP_ID).is(appId);
			Query tenantAppQuery = Query.query(appCriteria);
			AppMongodb appMongodb = readMongoTemplate.findOne(tenantAppQuery, AppMongodb.class, MongodbConstants.Collection.APP);

			if (appMongodb != null && appMongodb.getAutoRegister() != null && appMongodb.getAutoRegister()) {
				boolean admin = Optional.ofNullable(appMongodb.getAdminAccountIds()).orElse(Collections.emptyList()).contains(account.getAccountId());
				String newUserId = appUserCommonService.getNewAppUserId();
				AppUserMongodb user = AppUserMongodb
					.builder()
					.appId(appId)
					.userId(newUserId)
					.nickname(account.getNickname())
					.phoneNumber(account.getPhoneNumber())
					.admin(admin)
					.roleIds(Collections.emptyList())
					.departmentIds(Collections.emptyList())
					.tagIds(Collections.emptyList())
					.enabled(true)
					.joinTime(LocalDateTime.now())
					.accountId(account.getAccountId())
					.metadata(AppUserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
					.build();
				// 创建用户
				user = mongoTemplate.insert(user, MongodbConstants.Collection.APP_USER);
				// 发送应用用户创建消息
				rabbitTemplate.convertAndSend(
					cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
					cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_APP_USER, appId),
					objectMapper.writeValueAsString(
						CreatedAppUserMessage.builder()
							.appId(appId)
							.userId(user.getUserId())
							.nickname(user.getNickname())
							.accountId(user.getAccountId())
							.eventAppUserId(user.getUserId())
							.eventTime(LocalDateTime.now())
							.build()
					),
					new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
				);
				return user;
			}
		}
		return null;
	}
}
