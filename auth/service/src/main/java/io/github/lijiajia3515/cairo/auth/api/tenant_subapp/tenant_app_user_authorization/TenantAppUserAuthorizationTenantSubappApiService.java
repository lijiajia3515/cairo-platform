package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_user_authorization;

import com.baomidou.lock.annotation.Lock4j;
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
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_authorization.OfflineTenantAppUserAuthorizationArgs;
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
 * [tenant_subapp_user_api/api] tenant  user authorization service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserAuthorizationTenantSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;

	public TenantAppUserAuthorizationTenantSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
																		TransactionTemplate transactionTemplate,
																		AppCommonService appCommonService,
																		EndpointCommonService endpointCommonService,
																		ClientCommonService clientCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
	}

	/**
	 * 获取企业用户会话列表
	 *
	 * @param args 参数
	 * @return 企业用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_authorization:get_tenant_app_user_authorization_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<TenantAppUserAuthorization> getTenantAppUserAuthorizationList(String tenantId, String appId, @Validated GetTenantAppUserAuthorizationArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.UPDATE_TIME),
					Sort.Order.desc(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID)
				)
			);

		List<TenantAppUserAuthorizationMongodb> appUserAuthorizationMongodbs = readMongoTemplate.find(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		return getTenantAppUserAuthorizationList(appId, appUserAuthorizationMongodbs);
	}

	/**
	 * 获取企业用户会话分页列表
	 *
	 * @param args 参数
	 * @return 企业用户会话列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_authorization:get_tenant_app_user_authorization_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<TenantAppUserAuthorization> getTenantAppUserAuthorizationPageList(String tenantId, String appId, @Validated GetTenantAppUserAuthorizationArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query
			.query(criteria)
			.with(
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

	/**
	 * 下线企业用户会话
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "offline_tenant_app_user_authorization", keys = {"#args.tokenId"})
	@BizLog(
		bizId = "tenant_app_user_authorization:offline_tenant_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineTenantAppUserAuthorization(String tenantId, String appId, OfflineTenantAppUserAuthorizationArgs args) {
		Query query = Query.query(
			Criteria
				.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
				.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(args.getTokenId())
		);
		TenantAppUserAuthorizationMongodb authorizationMongodb = mongoTemplate.findOne(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		if (authorizationMongodb == null) {
			throw new ConflictBusinessException("企业用户会话不存在");
		}
		if (!AccountAuthorizationStatus.OK.getStatusValue().equals(authorizationMongodb.getStatus())) {
			throw new ConflictBusinessException("企业用户会话状态异常");
		}


		TenantAppUserAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Update update = new Update();
				update.set(TenantAppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineTenantAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线企业用户会话失败");
			}
		});

		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("下线企业用户会话失败");
		}
	}

	/**
	 * 下线所有企业用户会话
	 *
	 * @param appId appId
	 */
	@NewSpan
	@Lock4j(name = "offline_all_tenant_app_user_authorization", keys = {"#appId"})
	@BizLog(
		bizId = "tenant_app_user_authorization:offline_all_tenant_app_user_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
		}
	)
	public void offlineAllTenantAppUserAuthorization(String tenantId, String appId) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(
					Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue()));
				List<TenantAppUserAuthorizationMongodb> appUserAuthorizationMongodbs = mongoTemplate.find(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
				if (!appUserAuthorizationMongodbs.isEmpty()) {
					Query tokenIdQuery = Query.query(
						Criteria
							.where(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).in(appUserAuthorizationMongodbs.stream().map(TenantAppUserAuthorizationMongodb::getTokenId).collect(Collectors.toSet())));

					Update update = new Update();
					update.set(TenantAppUserAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
					update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
					mongoTemplate.updateMulti(tokenIdQuery, update, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
				}

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineAllTenantAppUserAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线企业用户会话失败");
			}
		});


	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数0
	 */
	private Criteria buildCriteria(String tenantId, String appId, GetTenantAppUserAuthorizationArgs args) {
		Criteria criteria = Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId);

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getUserId() != null && !args.getUserId().isBlank()) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getStatus() != null && !args.getStatus().isBlank()) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(args.getStatus());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).regex(args.getKeyword()),
				Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.SNS_TYPE).regex(args.getKeyword()),
				Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.LOGIN_TYPE).regex(args.getKeyword())
			);
		}
		return criteria;
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
