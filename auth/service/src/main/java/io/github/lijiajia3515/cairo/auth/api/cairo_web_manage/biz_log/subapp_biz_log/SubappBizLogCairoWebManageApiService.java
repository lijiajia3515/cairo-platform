package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.subapp.SubappClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.subapp_biz_log.SubappBizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.subapp_biz_log.GetSubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogSubappMongodb;
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
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] app endpoint biz log service
 */
@Slf4j
@Validated
@Component
public class SubappBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final EndpointClientApiService endpointClientApiService;
	private final SubappClientApiService subappClientApiService;

	public SubappBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													AppUserCommonService appUserCommonService,
												 EndpointClientApiService endpointClientApiService,
													SubappClientApiService subappClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appUserCommonService = appUserCommonService;
		this.endpointClientApiService = endpointClientApiService;
		this.subappClientApiService = subappClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "subapp_biz_log:get_subapp_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<SubappBizLog> getSubappBizLogPageList(@Valid @NotNull String appId, String endpointId, @Validated GetSubappBizLogArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.APP_ID).is(appId);
		}

		if (endpointId != null && !endpointId.isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}

		if (args.getSubappId() != null && !args.getSubappId().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}

		if (args.getSubappVersion() != null && !args.getSubappVersion().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		}


		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTImeCriteria = criteria.and(BizLogSubappMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTImeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTImeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getUserId() != null && !args.getUserId().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogSubappMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogSubappMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogSubappMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogSubappMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogSubappMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);


		long total = readMongoTemplate.count(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogSubappMongodb.FIELD.START_TIME)));

		List<BizLogSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
		List<SubappBizLog> contents = getSubappBizLogList(appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<SubappBizLog> getSubappBizLogList(String appId, List<BizLogSubappMongodb> list) {
		Set<String> userIds = list.stream().map(BizLogSubappMongodb::getUserId).collect(Collectors.toSet());
		Map<String, AppUser> userMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);

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
			GetSubappClientArgs.builder()
				.appId(appId)
				.build()
		).stream().collect(Collectors.toMap(Subapp::getSubappId, x -> x));

		return list.stream().map(x -> subappBizLog(x, appEndointMap, subappMap, userMap)).collect(Collectors.toList());
	}

	public SubappBizLog subappBizLog(BizLogSubappMongodb m, Map<String, Endpoint> endpointMap, Map<String, Subapp> subappMap, Map<String, AppUser> userMap) {
		return SubappBizLog.builder()
			.logId(m.getLogId())
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.subappId(m.getSubappId())
			.subappVersion(m.getSubappVersion())
			.subappName(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappName).orElse(m.getSubappId()))
			.subappIcon(Optional.ofNullable(subappMap.get(m.getSubappId())).map(Subapp::getSubappIcon).orElse(null))
			.user(Optional.ofNullable(userMap.get(m.getUserId())).orElse(AppUser.builder()
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
