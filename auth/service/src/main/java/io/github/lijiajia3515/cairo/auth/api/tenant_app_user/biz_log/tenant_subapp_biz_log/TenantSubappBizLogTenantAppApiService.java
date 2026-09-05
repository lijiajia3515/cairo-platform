package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.biz_log.tenant_subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.subapp.SubappClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.biz_log.tenant_subapp_biz_log.GetMyTenantSubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app.biz_log.tenant_subapp_biz_log.MyTenantSubappBizLog;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [tenant_endpoint/api] tenant app subapp biz log service
 */
@Slf4j
@Validated
@Component
public class TenantSubappBizLogTenantAppApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final EndpointClientApiService endpointClientApiService;
	private final SubappClientApiService subappClientApiService;

	public TenantSubappBizLogTenantAppApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
															 TenantAppUserCommonService tenantAppUserCommonService,
															 EndpointClientApiService endpointClientApiService, SubappClientApiService subappClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.endpointClientApiService = endpointClientApiService;
		this.subappClientApiService = subappClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_subapp_biz_log:get_my_tenant_subapp_biz_log_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MyTenantSubappBizLog> getMyTenantSubappBizLogList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyTenantSubappBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, tenantId, userId, args));
		query.with(Sort.by(Sort.Order.desc(BizLogTenantSubappMongodb.FIELD.START_TIME)));
		query.limit(100);
		List<BizLogTenantSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);

		return getMyTenantSubappBizLogList(tenantId, appId, mongodbList);
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_subapp_biz_log:get_my_tenant_subapp_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MyTenantSubappBizLog> getMyTenantSubappBizLogPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyTenantSubappBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, tenantId, userId, args));

		long total = readMongoTemplate.count(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogTenantSubappMongodb.FIELD.START_TIME)));

		List<BizLogTenantSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
		List<MyTenantSubappBizLog> contents = getMyTenantSubappBizLogList(tenantId, appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(String appId, String tenantId, String userId, GetMyTenantSubappBizLogArgs args) {
		Criteria criteria = Criteria
			.where(BizLogTenantSubappMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizLogTenantSubappMongodb.FIELD.APP_ID).is(appId)
			.and(BizLogTenantSubappMongodb.FIELD.USER_ID).is(userId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(BizLogTenantSubappMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isEmpty()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getSubappId() != null && !args.getSubappId().isEmpty()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getSubappVersion() != null && !args.getSubappVersion().isEmpty()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogTenantSubappMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogTenantSubappMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogTenantSubappMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogTenantSubappMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		return criteria;
	}

	public List<MyTenantSubappBizLog> getMyTenantSubappBizLogList(String tenantId, String appId, List<BizLogTenantSubappMongodb> list) {
		Map<String, TenantAppUser> metadataUserMap = tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(list.stream().map(BizLogTenantSubappMongodb::getMetadata).collect(Collectors.toList())));

		// 终端
		Map<String, Endpoint> appEndointMap;
		List<GetEndpointByAppClientArgs.EndpointInfo> endpointInfos = list.stream().map(x -> GetEndpointByAppClientArgs.EndpointInfo.builder().appId(x.getAppId()).endpointId(x.getEndpointId()).build()).distinct().collect(Collectors.toList());
		if (!endpointInfos.isEmpty()) {
			List<Endpoint> endpointByAppList = endpointClientApiService.getEndpointByAppList(GetEndpointByAppClientArgs.builder().EndpointInfos(endpointInfos).build());
			appEndointMap = Optional.ofNullable(endpointByAppList).stream().flatMap(Collection::stream).collect(Collectors.toMap(Endpoint::getEndpointId, x -> x));
		} else {
			appEndointMap = Collections.emptyMap();
		}

		Map<String, Subapp> subappMap = subappClientApiService.getSubappList(
			GetSubappClientArgs.builder().appId(appId).build()
		).stream().collect(Collectors.toMap(Subapp::getSubappId, x -> x));


		return list.stream().map(x -> tenantSubappBizLog(x, appEndointMap, subappMap, metadataUserMap)).collect(Collectors.toList());
	}

	public MyTenantSubappBizLog tenantSubappBizLog(BizLogTenantSubappMongodb m, Map<String, Endpoint> endpointMap, Map<String, Subapp> subappMap, Map<String, TenantAppUser> metadataUserMap) {
		return MyTenantSubappBizLog.builder()
			.logId(m.getLogId())
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappVersion(m.getSubappVersion())
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(m.getSubappId()))
			.subappIcon(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappIcon).orElse(null))
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
