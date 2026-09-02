package io.github.lijiajia3515.cairo.auth.api.subapp.app_user_authorization;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_authorization.GetAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_authorization.OfflineAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_authorization.AppUserAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationConverter;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
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
 * [subapp_user/api] app subapp user authorization service
 */
@Slf4j
@Validated
@Component
public class AppUserAuthorizationSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;
	private final AppUserAuthorizationCommonService appUserAuthorizationCommonService;

	public AppUserAuthorizationSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
															@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															TransactionTemplate transactionTemplate,
															AppCommonService appCommonService,
															EndpointCommonService endpointCommonService,
															ClientCommonService clientCommonService,
															AppUserAuthorizationCommonService appUserAuthorizationCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
		this.appUserAuthorizationCommonService = appUserAuthorizationCommonService;
	}

	/**
	 * 获取终端用户会话列表
	 *
	 * @param args 参数
	 * @return 终端用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_authorization:get_app_user_authorization_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<AppUserAuthorization> getAppUserAuthorizationList(String appId, @Validated GetAppUserAuthorizationArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
					Sort.Order.desc(AppUserAuthorizationMongodb.FIELD.TOKEN_ID)
				)
			);

		List<AppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		return getAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
	}

	/**
	 * 获取终端用户会话分页列表
	 *
	 * @param args 参数
	 * @return 终端用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_user_authorization:get_app_user_authorization_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<AppUserAuthorization> getAppUserAuthorizationPageList(String appId, @Validated GetAppUserAuthorizationArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
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
	 * 下线终端用户会话
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "offline_app_user_authorization", keys = {"#args.tokenId"})
	@BizLog(
		bizId = "app_user_authorization:offline_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineAppUserAuthorization(String appId, OfflineAppUserAuthorizationArgs args) {
		Query query = Query.query(
			Criteria
				.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
				.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(args.getTokenId())
		);
		AppUserAuthorizationMongodb authorizationMongodb = mongoTemplate.findOne(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		if (authorizationMongodb == null) {
			throw new ConflictBusinessException("终端用户会话不存在");
		}
		if (!AccountAuthorizationStatus.OK.getStatusValue().equals(authorizationMongodb.getStatus())) {
			throw new ConflictBusinessException("终端用户会话状态异常");
		}


		AppUserAuthorizationMongodb authorization = transactionTemplate.execute(status -> {
			try {
				Update update = new Update();
				update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线终端用户会话失败");
			}
		});

		if (authorization != null && authorization.getAccessToken() != null && authorization.getAccessToken().getTokenValue() != null) {
			appUserAuthorizationCommonService.removeAuthorizationCache(appId, authorization.getEndpointId(), authorization.getAccessToken().getTokenValue());
		}
	}

	/**
	 * 下线所有终端用户会话
	 *
	 * @param appId appId
	 */
	@NewSpan
	@Lock4j(name = "offline_all_app_user_authorization", keys = {"#appId"})
	@BizLog(
		bizId = "app_user_authorization:offline_all_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
		}
	)
	public void offlineAllAppUserAuthorization(String appId) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue()));
				List<AppUserAuthorizationMongodb> appUserAuthorizationMongodbs = mongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
				if (!appUserAuthorizationMongodbs.isEmpty()) {
					Query tokenIdQuery = Query.query(
						Criteria
							.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).in(appUserAuthorizationMongodbs.stream().map(AppUserAuthorizationMongodb::getTokenId).collect(Collectors.toSet())));

					Update update = new Update();
					update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
					update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
					mongoTemplate.updateMulti(tokenIdQuery, update, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
				}

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineAllAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线终端用户会话失败");
			}
		});

		appUserAuthorizationCommonService.removeAllAuthorizationCache(appId);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数0
	 */
	private Criteria buildCriteria(String appId, GetAppUserAuthorizationArgs args) {
		Criteria criteria = Criteria.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId);

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getUserId() != null && !args.getUserId().isBlank()) {
			criteria.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(AppUserAuthorizationMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getStatus() != null && !args.getStatus().isBlank()) {
			criteria.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(args.getStatus());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.SNS_TYPE).regex(args.getKeyword()),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.LOGIN_TYPE).regex(args.getKeyword())
			);
		}
		return criteria;
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
