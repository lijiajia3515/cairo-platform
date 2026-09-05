package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.tenant_app_user_authorization;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_authorization.TenantAppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationConverter;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.GetMyTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.OfflineMyTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.tenant_app_user_authorization.RegisterDeviceArgs;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * [tenant_app/api] tenant app user authorization service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserAuthorizationTenantAppUserApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;
	private final RedisTemplate<String, Object> redisTemplate;

	public TenantAppUserAuthorizationTenantAppUserApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																		 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
																		 TransactionTemplate transactionTemplate,
																		 AppCommonService appCommonService,
																		 EndpointCommonService endpointCommonService, ClientCommonService clientCommonService,
																		 RedisTemplate<String, Object> redisTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 获取我的企业应用级用户会话列表
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @return 我的企业应用级用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_authorization:get_tenant_app_user_authorization_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	public List<TenantAppUserAuthorization> getMyTenantAppUserAuthorizationList(String tenantId, String appId, String userId) {
		Criteria criteria = Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId);
		Query query = Query.query(criteria).with(
			Sort.by(
				Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
				Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID)
			)
		);
		List<TenantAppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		return getTenantAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
	}

	/**
	 * 获取我的企业应用级用户会话分页列表
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @param args   args
	 * @return 我的企业应用级用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_authorization:get_tenant_app_user_authorization_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<TenantAppUserAuthorization> getMyTenantAppUserAuthorizationPageList(String tenantId, String appId, String userId, GetMyTenantAppUserAuthorizationArgs args) {
		Criteria criteria = Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId);
		Query query = Query.query(criteria).with(
			Sort.by(
				Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
				Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID)
			)
		);
		long total = readMongoTemplate.count(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		query.with(args.pageable());
		List<TenantAppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		List<TenantAppUserAuthorization> subappUserAuthorizationList = getTenantAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
		return new Page<>(args, subappUserAuthorizationList, total);
	}

	@NewSpan
	@Lock4j(name = "register_tenant_app_user_device", keys = {"#tenantId", "#appId", "#endpointId", "#userId", "#tokenId"})
	@BizLog(
		bizId = "tenant_app_user_authorization:offline_my_tenant_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void registerMyTenantAppUserDevice(String tenantId, String appId, String endpointId, String userId, String tokenId, RegisterDeviceArgs args) {
		// 移除当前用户的其他会话设备信息
		Criteria userCriteria = Criteria
			.where(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.DEVICE_ID).ne(null)
			.and(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(TenantAppUserAuthorizationStatus.OK.getStatusValue());

		Update userUpdate = new Update();
		userUpdate.set(TenantAppUserAuthorizationMongodb.FIELD.DEVICE_ID, null);
		userUpdate.currentDate(TenantAppUserAuthorizationMongodb.FIELD.DEVICE_TIME);

		mongoTemplate.updateMulti(Query.query(userCriteria), userUpdate, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

		Criteria criteria = Criteria
			.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId);
		Update update = new Update();
		update.set(TenantAppUserAuthorizationMongodb.FIELD.DEVICE_ID, args.getDeviceId());
		update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.DEVICE_TIME);

		mongoTemplate.updateFirst(Query.query(criteria), update, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
	}

	/**
	 * 下线我的应用级用户会话
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "offline_my_tenant_app_user_authorization", keys = {"#args.tokenId"})
	@BizLog(
		bizId = "tenant_app_user_authorization:offline_my_tenant_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineMyTenantAppUserAuthorization(String tenantId, String appId, String userId, OfflineMyTenantAppUserAuthorizationArgs args) {
		TenantAppUserAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(args.getTokenId())
				);
				Update update = new Update();
				update.set(TenantAppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineMyTenantAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线我的应用级用户会话失败");
			}
		});

		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("会话登录状态已失效");
		}
	}

	/**
	 * 下线应用级用户会话
	 *
	 * @param appId   appId
	 * @param tokenId 会话ID
	 */
	@NewSpan
	@Lock4j(name = "logout_tenant_app_user_authorization", keys = {"#tokenId"})
	@BizLog(
		bizId = "tenant_app_user_authorization:logout_tenant_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tokenId", value = "#tokenId")
		}
	)
	public void logoutTenantAppUserAuthorization(String tenantId, String appId, String tokenId) {
		TenantAppUserAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId)
				);
				Update update = new Update();
				update.set(TenantAppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.LOGOUT.getStatusValue());
				update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoutTenantAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("退出登录失败");
			}
		});

		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("退出登录失败");
		}
		evictAccessTokenCache(accountAuthorizationMongodb);
	}

	/**
	 * 删除该会话 access token 的 Redis 缓存：登出只改 Mongo 状态，不删缓存会让
	 * 资源侧状态检查（findByToken 缓存路径）在缓存 TTL 内读到旧 OK 记录，吊销不生效
	 */
	private void evictAccessTokenCache(TenantAppUserAuthorizationMongodb authorization) {
		try {
			redisTemplate.delete(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN,
				authorization.getTenantId(), authorization.getAppId(), authorization.getEndpointId(),
				authorization.getAccessToken().getTokenValue()));
		} catch (Exception e) {
			log.warn("evict tenant app user access token cache failed", e);
		}
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return TenantAppUserAuthorization list
	 */
	List<TenantAppUserAuthorization> getTenantAppUserAuthorizationList(String appId, List<TenantAppUserAuthorizationMongodb> ms) {
		Map<String, App> appMap = Optional.of(Collections.singleton(appId))
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> endpointIds = ms.stream().map(TenantAppUserAuthorizationMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());

		List<String> clientIds = ms.stream().map(TenantAppUserAuthorizationMongodb::getClientId).distinct().collect(Collectors.toList());
		Map<String, BasicClient> clientMap = Optional.of(clientIds)
			.filter(innerClientIds -> !innerClientIds.isEmpty())
			.map(clientCommonService::getClientMapByClientIds)
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> TenantAppUserAuthorizationConverter.convertTenantAppUserAuthorization(appMap, endpointMap, clientMap, x)).collect(Collectors.toList());
	}


}
