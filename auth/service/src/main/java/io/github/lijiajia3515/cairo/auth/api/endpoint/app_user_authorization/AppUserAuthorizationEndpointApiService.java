package io.github.lijiajia3515.cairo.auth.api.endpoint.app_user_authorization;


import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_authorization.AppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationConverter;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user_authorization.GetMyAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user_authorization.OfflineMyAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user_authorization.RegisterDeviceArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * [endpoint/api] app user authorization service
 */
@Slf4j
@Validated
@Component
public class AppUserAuthorizationEndpointApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;

	public AppUserAuthorizationEndpointApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
															 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															 TransactionTemplate transactionTemplate,
															 AppCommonService appCommonService,
															 EndpointCommonService endpointCommonService, ClientCommonService clientCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
	}

	/**
	 * 获取我的终端用户会话列表
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @return 我的终端用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_authorization:get_app_user_authorization_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<AppUserAuthorization> getMyAppUserAuthorizationList(String appId, String userId, GetMyAppUserAuthorizationArgs args) {
		Criteria criteria = Criteria.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue());

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.SNS_TYPE).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.LOGIN_TYPE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria).with(
			Sort.by(
				Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
				Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.TOKEN_ID)
			)
		);
		List<AppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		return getAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
	}

	/**
	 * 获取我的终端用户会话分页列表
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @param args   args
	 * @return 我的终端用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_authorization:get_app_user_authorization_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<AppUserAuthorization> getMyAppUserAuthorizationPageList(String appId, String userId, GetMyAppUserAuthorizationArgs args) {
		Criteria criteria = Criteria.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue());
		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.SNS_TYPE).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.LOGIN_TYPE).regex(args.getKeyword())
			);
		}
		Query query = Query.query(criteria).with(
			Sort.by(
				Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
				Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.TOKEN_ID)
			)
		);
		long total = readMongoTemplate.count(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		query.with(args.pageable());
		List<AppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		List<AppUserAuthorization> subappUserAuthorizationList = getAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
		return new Page<>(args, subappUserAuthorizationList, total);
	}

	/**
	 * 下线我的应用用户会话
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "register_my_app_user_device", keys = {"#appId", "#endpointId", "#userId", "#tokenId"})
	@BizLog(
		bizId = "app_user_authorization:register_my_app_user_device",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "tokenId", value = "#tokenId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void registerMyAppUserDevice(String appId, String endpointId, String userId, String tokenId, RegisterDeviceArgs args) {
		// 移除当前用户的其他会话设备信息
		Criteria userCriteria = Criteria
			.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(AppUserAuthorizationMongodb.FIELD.DEVICE_ID).ne(null)
			.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue());

		Update userUpdate = new Update();
		userUpdate.set(AppUserAuthorizationMongodb.FIELD.DEVICE_ID, null);
		userUpdate.currentDate(AppUserAuthorizationMongodb.FIELD.DEVICE_TIME);

		mongoTemplate.updateMulti(Query.query(userCriteria), userUpdate, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);

		// 更新当前会话的设备信息
		Criteria currentCriteria = Criteria
			.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
			.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId);
		Update currentUpdate = new Update();
		currentUpdate.set(AppUserAuthorizationMongodb.FIELD.DEVICE_ID, args.getDeviceId());
		currentUpdate.currentDate(AppUserAuthorizationMongodb.FIELD.DEVICE_TIME);
		// 更新当前会话
		mongoTemplate.updateFirst(Query.query(currentCriteria), currentUpdate, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
	}

	/**
	 * 下线我的应用用户会话
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "offline_my_app_user_authorization", keys = {"#args.tokenId"})
	@BizLog(
		bizId = "app_user_authorization:offline_my_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineMyAppUserAuthorization(String appId, String userId, OfflineMyAppUserAuthorizationArgs args) {
		AppUserAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
						.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(args.getTokenId())
				);
				Update update = new Update();
				update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineMyAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线我的应用用户会话失败");
			}
		});

		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("会话登录状态已失效");
		}
	}

	/**
	 * 下线应用用户会话
	 *
	 * @param appId   appId
	 * @param tokenId tokenId
	 */
	@NewSpan
	@Lock4j(name = "logout_app_user_authorization", keys = {"#tokenId"})
	@BizLog(
		bizId = "app_user_authorization:logout_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "tokenId", value = "#tokenId")
		}
	)
	public void logoutAppUserAuthorization(String appId, String tokenId) {
		AppUserAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(tokenId)
				);
				Update update = new Update();
				update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.LOGOUT.getStatusValue());
				update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoutAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("退出登录失败");
			}
		});

		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("退出登录失败");
		}
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return AppUserAuthorization list
	 */
	List<AppUserAuthorization> getAppUserAuthorizationList(String appId, List<AppUserAuthorizationMongodb> ms) {
		Map<String, App> appMap = Optional.of(Collections.singleton(appId))
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> endpointIds = ms.stream().map(AppUserAuthorizationMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());

		List<String> clientIds = ms.stream().map(AppUserAuthorizationMongodb::getClientId).distinct().collect(Collectors.toList());
		Map<String, BasicClient> clientMap = Optional.of(clientIds)
			.filter(innerClientIds -> !innerClientIds.isEmpty())
			.map(clientCommonService::getClientMapByClientIds)
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> AppUserAuthorizationConverter.convertAppUserAuthorization(appMap, endpointMap, clientMap, x)).collect(Collectors.toList());
	}
}
