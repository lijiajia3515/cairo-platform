package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.login_log.tenant_app_user_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.TenantAppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.login_log.tenant_app_user_login_log.GetMyTenantAppUserLoginLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.login_log.tenant_app_user_login_log.MyTenantAppUserLoginLog;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [tenant_endpoint/api] tenant app user login log service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserLoginLogTenantAppUserApiService {
	private final MongoTemplate readMongoTemplate;

	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;

	public TenantAppUserLoginLogTenantAppUserApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, EndpointCommonService endpointCommonService, ClientCommonService clientCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_login_log:get_my_tenant_app_user_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MyTenantAppUserLoginLog> getMyTenantAppUserLoginLogPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyTenantAppUserLoginLogArgs args) {
		Query query = Query.query(buildCriteria(appId, tenantId, userId, args));

		long total = readMongoTemplate.count(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc("login_time")));

		List<TenantAppUserLoginLogMongodb> mongodbList = readMongoTemplate.find(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		List<MyTenantAppUserLoginLog> contents = getMyTenantAppUserLoginLogList(mongodbList);
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(String appId, String tenantId, String userId, GetMyTenantAppUserLoginLogArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserLoginLogMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserLoginLogMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserLoginLogMongodb.FIELD.USER_ID).is(userId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(TenantAppUserLoginLogMongodb.FIELD.LOGIN_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getLoginType() != null && !args.getLoginType().isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.LOGIN_TYPE).is(args.getLoginType());
		}

		if (args.getSuccess() != null) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.ERR_MSG).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.OS).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.PLATFORM).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.ENGINE).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.APP).regex(args.getKeyword())
			);
		}

		return criteria;
	}

	public List<MyTenantAppUserLoginLog> getMyTenantAppUserLoginLogList(List<TenantAppUserLoginLogMongodb> mongodbList) {
		Set<String> endpointIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getEndpointId).collect(Collectors.toSet());
		Map<String, Endpoint> endpointMap;
		if (!endpointIds.isEmpty()) {
			endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);
		} else {
			endpointMap = Collections.emptyMap();
		}

		Set<String> clientIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getClientId).collect(Collectors.toSet());
		Map<String, BasicClient> clientMap;
		if (!clientIds.isEmpty()) {
			clientMap = clientCommonService.getClientMapByClientIds(clientIds);
		} else {
			clientMap = Collections.emptyMap();
		}
		return mongodbList.stream().map(x -> myTenantAppUserLoginLog(x, endpointMap, clientMap)).collect(Collectors.toList());
	}

	public MyTenantAppUserLoginLog myTenantAppUserLoginLog(TenantAppUserLoginLogMongodb source, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap) {
		return MyTenantAppUserLoginLog.builder()
			.logId(source.getLogId())
			.endpointId(source.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(source.getEndpointId())).map(Endpoint::getEndpointName).orElse(source.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(source.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(source.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(source.getClientId())).map(BasicClient::getClientName).orElse(source.getClientId()))
			.loginTime(source.getLoginTime())
			.loginType(source.getLoginType())
			.success(source.getSuccess())
			.errMsg(source.getErrMsg())
			.ip(source.getIp())
			.os(source.getOs())
			.platform(source.getPlatform())
			.engine(source.getEngine())
			.app(source.getApp())
			.region(source.getRegion())
			.build();
	}
}
