package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.biz_log.tenant_app_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.biz_log.tenant_app_biz_log.GetMyTenantAppBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.biz_log.tenant_app_biz_log.MyTenantAppBizLog;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantAppMongodb;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [tenant_endpoint/api] tenant app endpoint biz log service
 */
@Slf4j
@Validated
@Component
public class TenantAppBizLogTenantAppApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final EndpointClientApiService endpointClientApiService;
	private final ClientClientApiService clientClientApiService;

	public TenantAppBizLogTenantAppApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															  TenantAppUserCommonService tenantAppUserCommonService,
															  EndpointClientApiService endpointClientApiService,
															  ClientClientApiService clientClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.endpointClientApiService = endpointClientApiService;
		this.clientClientApiService = clientClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_biz_log:get_my_tenant_app_biz_log_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MyTenantAppBizLog> getMyTenantAppBizLogList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyTenantAppBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, tenantId, userId, args));
		query.with(Sort.by(Sort.Order.desc(BizLogTenantAppMongodb.FIELD.START_TIME)));
		query.limit(100);
		List<BizLogTenantAppMongodb> mongodbList = readMongoTemplate.find(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);

		return getMyTenantAppBizLogList(tenantId, appId, mongodbList);
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_biz_log:get_my_tenant_app_user_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MyTenantAppBizLog> getMyTenantAppBizLogPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyTenantAppBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, tenantId, userId, args));

		long total = readMongoTemplate.count(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogTenantAppMongodb.FIELD.START_TIME)));

		List<BizLogTenantAppMongodb> mongodbList = readMongoTemplate.find(query, BizLogTenantAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_APP);
		List<MyTenantAppBizLog> contents = getMyTenantAppBizLogList(tenantId, appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(String appId, String tenantId, String userId, GetMyTenantAppBizLogArgs args) {
		Criteria criteria = Criteria
			.where(BizLogTenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizLogTenantAppMongodb.FIELD.APP_ID).is(appId)
			.and(BizLogTenantAppMongodb.FIELD.USER_ID).is(userId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(BizLogTenantAppMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(BizLogTenantAppMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(BizLogTenantAppMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogTenantAppMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogTenantAppMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogTenantAppMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogTenantAppMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogTenantAppMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		return criteria;
	}

	public List<MyTenantAppBizLog> getMyTenantAppBizLogList(String tenantId, String appId, List<BizLogTenantAppMongodb> list) {
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(list.stream().map(BizLogTenantAppMongodb::getMetadata).collect(Collectors.toList())));

		// 终端
		Map<String, Endpoint> appEndointMap;
		List<GetEndpointByAppClientArgs.EndpointInfo> endpointInfos = list.stream().map(x -> GetEndpointByAppClientArgs.EndpointInfo.builder().appId(x.getAppId()).endpointId(x.getEndpointId()).build()).distinct().collect(Collectors.toList());
		if (!endpointInfos.isEmpty()) {
			List<Endpoint> endpointByAppList = endpointClientApiService.getEndpointByAppList(GetEndpointByAppClientArgs.builder().EndpointInfos(endpointInfos).build());
			appEndointMap = Optional.ofNullable(endpointByAppList).stream().flatMap(Collection::stream).collect(Collectors.toMap(Endpoint::getEndpointId, x -> x));
		} else {
			appEndointMap = Collections.emptyMap();
		}

		Map<String, BasicClient> clientMap;
		List<String> clientIds = list.stream().map(BizLogTenantAppMongodb::getClientId).distinct().collect(Collectors.toList());
		if (!clientIds.isEmpty()) {
			List<BasicClient> basicClientList = clientClientApiService.getBasicClientList(GetClientArgs.builder()
				.clientIds(clientIds)
				.build());
			clientMap = Optional.ofNullable(basicClientList).stream().flatMap(Collection::stream).collect(Collectors.toMap(BasicClient::getClientId, x -> x));
		} else {
			clientMap = Collections.emptyMap();
		}

		return list.stream().map(x -> tenantAppBizLog(x, appEndointMap, clientMap, metadataUserMap)).collect(Collectors.toList());
	}

	public MyTenantAppBizLog tenantAppBizLog(BizLogTenantAppMongodb m, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap, Map<String, TenantAppUser> metadataUserMap) {
		return MyTenantAppBizLog.builder()
			.logId(m.getLogId())
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.tokenId(m.getTokenId())
			.bizId(m.getBizId())
			.scope(m.getScope())
			.params(m.getParams())
			.success(m.isSuccess())
			.errorMessage(m.getErrorMessage())
			.ip(m.getIp())
			.startTime(m.getStartTime())
			.endTime(m.getEndTime())
			.mills(m.getMills())
			.build();
	}
}
