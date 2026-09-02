package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.client;

import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.CreateClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.DeleteClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientSecretArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client.ModifyClientStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.MetadataClient;
import io.github.lijiajia3515.cairo.auth.domain.message.client.CreatedClientMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.client.DeletedClientMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.client.ModifiedClientInfoMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.client.ModifiedClientSecretMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.client.ModifiedClientStatusMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientConverter;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
import java.util.stream.Stream;

/**
 * [cairo_web_manage/api] client service
 */
@Slf4j
@Validated
@Component
public class ClientCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RabbitTemplate rabbitTemplate;

	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final AppUserCommonService appUserCommonService;
	private final SnsProviderCommonService snsProviderCommonService;

	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final CairoSecurityProperties cairoSecurityProperties;


	private static final List<String> AUTHENTICATION_TYPES = Stream.of("client", "account", "app_user", "app_user", "tenant_app_user", "tenant_app_user").collect(Collectors.toList());

	public ClientCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										  TransactionTemplate transactionTemplate,
										  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										  RedisTemplate<String, Object> redisTemplate,
										  RabbitTemplate rabbitTemplate,
										  AppCommonService appCommonService,
										  EndpointCommonService endpointCommonService,
										  AppUserCommonService appUserCommonService,
										  SnsProviderCommonService snsProviderCommonService,
										  ObjectMapper objectMapper,
										  CairoRabbitmqTool cairoRabbitmqTool,
										  CairoSecurityProperties cairoSecurityProperties
	) {

		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.redisTemplate = redisTemplate;

		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.appUserCommonService = appUserCommonService;
		this.snsProviderCommonService = snsProviderCommonService;

		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.cairoSecurityProperties = cairoSecurityProperties;

	}


	/**
	 * 客户端查询
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "client:get_client_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataClient> getClientList(@Validated GetClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<ClientMongodb> tms = readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return getMetadataClientList(tms, args.getExtension());
	}

	/**
	 * 查找
	 *
	 * @return 客户端分页查询
	 */
	@NewSpan
	@BizLog(
		bizId = "client:get_client_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataClient> getClientPageList(@Validated GetClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.METADATA.UPDATE_TIME)));

		long total = readMongoTemplate.count(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.METADATA.UPDATE_TIME)));
		List<MetadataClient> ds = getMetadataClientList(readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT), args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 客户端 - 保存
	 */
	@NewSpan
	@SneakyThrows
	@BizLog(
		bizId = "client:create_client",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createClient(@Validated CreateClientArgs args) {

		ClientMongodb insertedClientMongodb = transactionTemplate.execute(status -> {
			try {
				if (args.getEndpointId() != null) {
					endpointCommonService.checkEndpointId(mongoTemplate, args.getAppId(), args.getEndpointId());
				} else {
					appCommonService.checkAppId(mongoTemplate, args.getAppId());
				}

				//检查三方认证
				if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
					args.getAccountSnsProviderIds().forEach(snsProviderId -> snsProviderCommonService.checkSnsProviderId(mongoTemplate, snsProviderId));
				}

				//检查身份类型
				if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
					List<String> authenticationTypes = args.getAuthenticationTypes().stream()
						.filter(e -> !AUTHENTICATION_TYPES.contains(e))
						.toList();
					if (!authenticationTypes.isEmpty()) {
						throw new ConflictBusinessException("身份类型错误:" + authenticationTypes);
					}
				}


				ClientMongodb insertClient = ClientMongodb.builder()
					.id(CoreConstants.SNOWFLAKE.nextIdStr())
					.appId(args.getAppId())
					.endpointId(args.getEndpointId())
					.clientId(args.getClientId())
					.clientSecret("{noop}" + args.getClientSecret())
					.clientName(args.getClientName())
					.authorizationGrantTypes(args.getAuthorizationGrantTypes().stream().distinct().collect(Collectors.toList()))
					.clientAuthenticationMethods(args.getClientAuthenticationMethods().stream().distinct().collect(Collectors.toList()))
					.scopes(Optional.ofNullable(args.getScopes()).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList()))
					.redirectUris(Optional.ofNullable(args.getRedirectUris()).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList()))
					.authenticationTypes(args.getAuthenticationTypes())
					.accountSnsProviderIds(args.getAccountSnsProviderIds())
					.clientSettings(Optional.ofNullable(args.getClientSettings())
						.map(settings -> ClientMongodb.ClientSettings.builder()
							.requireProofKey(settings.getRequireProofKey())
							.requireAuthorizationConsent(settings.getRequireAuthorizationConsent())
							.tokenEndpointAuthenticationSigningAlgorithm(settings.getTokenEndpointAuthenticationSigningAlgorithm())
							.jwkSetUrl(settings.getJwkSetUrl())
							.build())
						.orElse(ClientMongodb.ClientSettings.builder().build())
					)
					.tokenSettings(Optional.ofNullable(args.getTokenSettings())
						.map(settings -> ClientMongodb.TokenSettings.builder()
							.idTokenFormat(settings.getIdTokenFormat())
							.idTokenSignatureAlgorithm(settings.getIdTokenSignatureAlgorithm())
							.idTokenTimeToLive(settings.getIdTokenTimeToLive())
							.accessTokenFormat(settings.getAccessTokenFormat())
							.accessTokenTimeToLive(settings.getAccessTokenTimeToLive())
							.refreshTokenTimeToLive(settings.getRefreshTokenTimeToLive())
							.reuseRefreshTokens(settings.getReuseRefreshTokens())
							.accountAccessTokenFormat(settings.getAccountAccessTokenFormat())
							.accountAccessTokenTimeToLive(settings.getAccountAccessTokenTimeToLive())
							.accountRefreshTokenTimeToLive(settings.getAccountRefreshTokenTimeToLive())
							.reuseAccountRefreshTokens(settings.getReuseAccountRefreshTokens())
							.tenantAppUserAccessTokenFormat(settings.getTenantAppUserAccessTokenFormat())
							.tenantAppUserAccessTokenTimeToLive(settings.getTenantAppUserAccessTokenTimeToLive())
							.tenantAppUserRefreshTokenTimeToLive(settings.getTenantAppUserRefreshTokenTimeToLive())
							.reuseTenantAppUserRefreshTokens(settings.getReuseTenantAppUserRefreshTokens())
							.appUserAccessTokenFormat(settings.getAppUserAccessTokenFormat())
							.appUserAccessTokenTimeToLive(settings.getAppUserAccessTokenTimeToLive())
							.appUserRefreshTokenTimeToLive(settings.getAppUserRefreshTokenTimeToLive())
							.reuseAppUserRefreshTokens(settings.getReuseAppUserRefreshTokens())
							.build()
						)
						.orElse(ClientMongodb.TokenSettings.builder().build())
					)
					.enabled(args.getEnabled())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();

				mongoTemplate.insert(insertClient, MongodbConstants.Collection.CLIENT);
				return insertClient;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("createClient", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建客户端失败");
			}
		});

		if (insertedClientMongodb != null) {
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.CREATED_CLIENT, insertedClientMongodb.getAppId()),
				objectMapper.writeValueAsString(CreatedClientMessage.builder()
					.id(insertedClientMongodb.getId())
					.appId(insertedClientMongodb.getAppId())
					.endpointId(insertedClientMongodb.getEndpointId())
					.clientId(insertedClientMongodb.getClientId())
					.clientName(insertedClientMongodb.getClientName())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}


	}

	/**
	 * 修改客户端信息
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_client_info", keys = {"#args.id"})
	@BizLog(
		bizId = "client:modify_client_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyClientInfo(@Validated ModifyClientInfoArgs args) {
		ClientMongodb modifiedClient = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(ClientMongodb.FIELD.ID).is(args.getId());
				Query query = Query.query(criteria);


				//检查三方认证
				if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
					args.getAccountSnsProviderIds().forEach(snsProviderId -> snsProviderCommonService.checkSnsProviderId(mongoTemplate, snsProviderId));
				}

				//检查身份类型
				if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
					List<String> authenticationTypes = args.getAuthenticationTypes().stream()
						.filter(e -> !AUTHENTICATION_TYPES.contains(e))
						.toList();
					if (!authenticationTypes.isEmpty()) {
						throw new ConflictBusinessException("身份类型错误:" + authenticationTypes);
					}
				}


				Update update = new Update();
				if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
					update.set(ClientMongodb.FIELD.ENDPOINT_ID, args.getEndpointId());
				} else {
					update.set(ClientMongodb.FIELD.ENDPOINT_ID, null);
				}

				if (args.getClientName() != null) {
					update.set(ClientMongodb.FIELD.CLIENT_NAME, args.getClientName());
				}

				update.set(ClientMongodb.FIELD.AUTHORIZATION_GRANT_TYPES, Optional.ofNullable(args.getAuthorizationGrantTypes()).orElse(Collections.emptyList()));

				update.set(ClientMongodb.FIELD.CLIENT_AUTHENTICATION_METHODS, Optional.ofNullable(args.getClientAuthenticationMethods()).orElse(Collections.emptyList()));
				update.set(ClientMongodb.FIELD.SCOPES, Optional.ofNullable(args.getScopes()).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList()));
				update.set(ClientMongodb.FIELD.REDIRECT_URIS, Optional.ofNullable(args.getRedirectUris()).orElse(Collections.emptyList()).stream().distinct().collect(Collectors.toList()));

				update.set(ClientMongodb.FIELD.AUTHENTICATION_TYPES, Optional.ofNullable(args.getAuthenticationTypes()).orElse(Collections.emptyList()));
				update.set(ClientMongodb.FIELD.ACCOUNT_SNS_PROVIDER_IDS, Optional.ofNullable(args.getAccountSnsProviderIds()).orElse(Collections.emptyList()));

				if (args.getClientSettings() != null) {
					update.set(ClientMongodb.FIELD.CLIENT_SETTINGS.REQUIRE_PROOF_KEY, args.getClientSettings().getRequireProofKey());
					update.set(ClientMongodb.FIELD.CLIENT_SETTINGS.REQUIRE_AUTHORIZATION_CONSENT, args.getClientSettings().getRequireAuthorizationConsent());
					update.set(ClientMongodb.FIELD.CLIENT_SETTINGS.JWK_SET_URL, args.getClientSettings().getJwkSetUrl());
					update.set(ClientMongodb.FIELD.CLIENT_SETTINGS.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM, args.getClientSettings().getTokenEndpointAuthenticationSigningAlgorithm());
				}

				if (args.getTokenSettings() != null) {
					// id token
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ID_TOKEN_FORMAT, args.getTokenSettings().getIdTokenFormat());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ID_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getIdTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ID_TOKEN_SIGNATURE_ALGORITHM, args.getTokenSettings().getIdTokenSignatureAlgorithm());

					// default
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ACCESS_TOKEN_FORMAT, args.getTokenSettings().getAccessTokenFormat());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ACCESS_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getAccessTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.REFRESH_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getRefreshTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.REUSE_REFRESH_TOKENS, args.getTokenSettings().getReuseRefreshTokens());

					// account
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ACCOUNT_ACCESS_TOKEN_FORMAT, args.getTokenSettings().getAccountAccessTokenFormat());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getAccountAccessTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getAccountRefreshTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.REUSE_ACCOUNT_REFRESH_TOKENS, args.getTokenSettings().getReuseAccountRefreshTokens());

					// app endpoint user
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.APP_USER_ACCESS_TOKEN_FORMAT, args.getTokenSettings().getAppUserAccessTokenFormat());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getAppUserAccessTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.APP_USER_REFRESH_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getAppUserRefreshTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.REUSE_APP_USER_REFRESH_TOKENS, args.getTokenSettings().getReuseAppUserRefreshTokens());

					// tenant app endpoint user
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.TENANT_APP_USER_ACCESS_TOKEN_FORMAT, args.getTokenSettings().getTenantAppUserAccessTokenFormat());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getTenantAppUserAccessTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE, args.getTokenSettings().getTenantAppUserRefreshTokenTimeToLive());
					update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.REUSE_TENANT_APP_USER_REFRESH_TOKENS, args.getTokenSettings().getReuseTenantAppUserRefreshTokens());
				}

				update.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);
				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				ClientMongodb modifiedClientMongodb = mongoTemplate.findAndModify(query, update, options, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				if (modifiedClientMongodb == null) {
					throw new ConflictBusinessException("修改客户端信息失败");
				}
				return modifiedClientMongodb;
			} catch (Exception e) {
				log.debug("modifyClientInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改客户端信息失败");
			}
		});

		if (modifiedClient != null) {
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT, modifiedClient.getId()));
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, modifiedClient.getClientId()));
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_CLIENT_INFO, modifiedClient.getAppId()),
				objectMapper.writeValueAsString(ModifiedClientInfoMessage.builder()
					.id(modifiedClient.getId())
					.appId(modifiedClient.getAppId())
					.endpointId(modifiedClient.getEndpointId())
					.clientId(modifiedClient.getClientId())
					.clientName(modifiedClient.getClientName())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * client 修改状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_client_status", keys = {"#args.id"})
	@BizLog(
		bizId = "client:modify_client_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyClientStatus(@Validated ModifyClientStatusArgs args) {
		ClientMongodb modifiedClient = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(ClientMongodb.FIELD.ID).is(args.getId());

				Query query = Query.query(criteria);

				Update update = Update
					.update(ClientMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				ClientMongodb modifiedClientMongodb = mongoTemplate.findAndModify(query, update, options, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				if (modifiedClientMongodb == null) {
					throw new ConflictBusinessException("修改客户端状态失败");
				}
				return modifiedClientMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyClientStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改客户端状态失败");
			}
		});

		if (modifiedClient != null) {
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT, modifiedClient.getId()));
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, modifiedClient.getClientId()));
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_CLIENT_STATUS, modifiedClient.getAppId()),
				objectMapper.writeValueAsString(ModifiedClientStatusMessage.builder()
					.id(modifiedClient.getId())
					.appId(modifiedClient.getAppId())
					.endpointId(modifiedClient.getEndpointId())
					.clientId(modifiedClient.getClientId())
					.enabled(modifiedClient.getEnabled())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}

	}

	/**
	 * 修改客户端密钥
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_client_secret", keys = {"#args.id"})
	@BizLog(
		bizId = "client:modify_client_secret",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void modifyClientSecret(@Validated ModifyClientSecretArgs args) {
		ClientMongodb modifiedClient = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(ClientMongodb.FIELD.ID).is(args.getId());

				Query query = Query.query(criteria);

				Update update = Update
					.update(ClientMongodb.FIELD.CLIENT_SECRET, "{noop}" + args.getClientSecret());
				update.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				ClientMongodb modifiedClientMongodb = mongoTemplate.findAndModify(query, update, options, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				if (modifiedClientMongodb == null) {
					throw new ConflictBusinessException("修改客户端密钥失败");
				}
				return modifiedClientMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyClientStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改客户端密钥失败");
			}
		});

		if (modifiedClient != null) {
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT, modifiedClient.getId()));
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, modifiedClient.getClientId()));
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.MODIFIED_CLIENT_SECRET, modifiedClient.getAppId()),
				objectMapper.writeValueAsString(ModifiedClientSecretMessage.builder()
					.id(modifiedClient.getId())
					.appId(modifiedClient.getAppId())
					.endpointId(modifiedClient.getEndpointId())
					.clientId(modifiedClient.getClientId())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * 删除客户端
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_client", keys = {"#args.id"})
	@BizLog(
		bizId = "client:delete_client",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@SneakyThrows
	public void deleteClient(@Validated DeleteClientArgs args) {
		ClientMongodb modifiedClient = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria.where(ClientMongodb.FIELD.ID).is(args.getId());

				Query query = Query.query(criteria);

				Update update = new Update();
				update.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(ClientMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				log.info("[deleteClient] {}", updateResult);
				ClientMongodb deletedClientMongodb = mongoTemplate.findAndRemove(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				if (deletedClientMongodb == null) {
					throw new ConflictBusinessException("删除客户端失败");
				}
				mongoTemplate.insert(deletedClientMongodb, MongodbConstants.DeletedCollection.CLIENT);
				return deletedClientMongodb;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteClient", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除客户端失败");
			}
		});

		if (modifiedClient != null) {
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT, modifiedClient.getId()));
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, modifiedClient.getClientId()));
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getAppKey(CairoAuthRabbitmqRouteKey.DELETED_CLIENT, modifiedClient.getAppId()),
				objectMapper.writeValueAsString(DeletedClientMessage.builder()
					.id(modifiedClient.getId())
					.appId(modifiedClient.getAppId())
					.endpointId(modifiedClient.getEndpointId())
					.clientId(modifiedClient.getClientId())
					.eventCairoUserId(CairoSecurityContextHolder.getSubappUserId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
			);
		}
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetClientArgs args) {
		Criteria criteria = new Criteria();
		if (args.getAppId() != null && !args.getAppId().isBlank()) {
			criteria.and(ClientMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(ClientMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getAuthorizationGrantTypes() != null && !args.getAuthorizationGrantTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHORIZATION_GRANT_TYPES).in(args.getAuthorizationGrantTypes());
		}

		if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHENTICATION_TYPES).in(args.getAuthenticationTypes());
		}

		if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.ACCOUNT_SNS_PROVIDER_IDS).in(args.getAccountSnsProviderIds());
		}


		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(ClientMongodb.FIELD.CLIENT_NAME).regex(args.getKeyword());
		}

		if (args.getEnabled() != null) {
			criteria.and(ClientMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		return criteria;
	}

	List<MetadataClient> getMetadataClientList(List<ClientMongodb> ms, Map<String, String> extensionMap) {
		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(ClientMongodb::getMetadata).collect(Collectors.toList()));

		List<String> appIds = ms.stream().map(ClientMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = appCommonService.getAppMapByAppIds(appIds);

		List<String> endpointIds = ms.stream().map(ClientMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);

		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> ClientConverter.convertMetadataClient(x, appMap, endpointMap, metadataUserMap)).collect(Collectors.toList());
	}

}
