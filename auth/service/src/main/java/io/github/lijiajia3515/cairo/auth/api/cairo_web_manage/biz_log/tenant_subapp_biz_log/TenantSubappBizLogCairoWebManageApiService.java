package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.tenant_subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.subapp.SubappClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.tenant_subapp_biz_log.GetTenantSubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.tenant_subapp_biz_log.TenantSubappBizLog;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogTenantSubappMongodb;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo_web_manage/api] tenant app subapp biz log service
 */
@Slf4j
@Validated
@Component
public class TenantSubappBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final EndpointClientApiService endpointClientApiService;
	private final SubappClientApiService subappClientApiService;

	public TenantSubappBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														  TenantAppUserCommonService tenantAppUserCommonService,
														  EndpointClientApiService endpointClientApiService,
														  SubappClientApiService subappClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.endpointClientApiService = endpointClientApiService;
		this.subappClientApiService = subappClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_biz_log:get_tenant_app_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<TenantSubappBizLog> getTenantSubappBizLogPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, String endpointId, @Validated GetTenantSubappBizLogArgs args) {
		Criteria criteria = new Criteria();

		if (tenantId != null && !tenantId.isBlank()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.TENANT_ID).is(tenantId);
		}
		if (appId != null && !appId.isBlank()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.APP_ID).is(appId);
		}

		if (endpointId != null && !endpointId.isBlank()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}

		if (args.getSubappId() != null && !args.getSubappId().isBlank()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getSubappVersion() != null && !args.getSubappVersion().isBlank()) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTimeCriteria = criteria.and(BizLogTenantSubappMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTimeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTimeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getUserId() != null) {
			criteria.and(BizLogTenantSubappMongodb.FIELD.USER_ID).is(args.getUserId());
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

		Query query = Query.query(criteria);


		long total = readMongoTemplate.count(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogTenantSubappMongodb.FIELD.START_TIME)));

		List<BizLogTenantSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogTenantSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_TENANT_SUBAPP);
		List<TenantSubappBizLog> contents = getTenantSubappBizLogList(tenantId, appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<TenantSubappBizLog> getTenantSubappBizLogList(String tenantId, String appId, List<BizLogTenantSubappMongodb> list) {
		Set<String> userIds = list.stream().map(x -> x.getUserId()).collect(Collectors.toSet());
		Map<String, TenantAppUser> userMap = tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds);

		// 终端
		Map<String, Endpoint> appEndointMap;
		List<GetEndpointByAppClientArgs.EndpointInfo> endpointInfos = list.stream().map(x -> GetEndpointByAppClientArgs.EndpointInfo.builder().appId(x.getAppId()).endpointId(x.getEndpointId()).build()).distinct().collect(Collectors.toList());
		if (!endpointInfos.isEmpty()) {
			List<Endpoint> endpointByAppList = endpointClientApiService.getEndpointByAppList(GetEndpointByAppClientArgs.builder().EndpointInfos(endpointInfos).build());
			appEndointMap = Optional.ofNullable(endpointByAppList).stream().flatMap(Collection::stream).collect(Collectors.toMap(Endpoint::getEndpointId, x -> x));
		} else {
			appEndointMap = Collections.emptyMap();
		}

		// 子应用
		Map<String, Subapp> subappMap = subappClientApiService.getSubappList(
			GetSubappClientArgs.builder().appId(appId).build()
		).stream().collect(Collectors.toMap(Subapp::getSubappId, x -> x));

		return list.stream().map(x -> tenantSubappBizLog(x, appEndointMap, subappMap, userMap)).collect(Collectors.toList());
	}

	public TenantSubappBizLog tenantSubappBizLog(BizLogTenantSubappMongodb m, Map<String, Endpoint> endpointMap, Map<String, Subapp> subappMap, Map<String, TenantAppUser> userMap) {
		return TenantSubappBizLog.builder()
			.logId(m.getLogId())
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappVersion(m.getSubappVersion())
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappId).orElse(m.getSubappId()))
			.subappIcon(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappIcon).orElse(null))
			.user(Optional.ofNullable(userMap.get(m.getUserId())).orElse(TenantAppUser.builder()
				.userId(m.getUserId())
				.nickname(m.getUserId())
				.build())
			)
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
