package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.app_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.app_biz_log.AppBizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.app_biz_log.GetAppBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogAppMongodb;
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
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

/**
 * [cairo_web_manage/api] app endpoint biz log service
 */
@Slf4j
@Validated
@Component
public class AppBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final EndpointClientApiService endpointClientApiService;
	private final ClientClientApiService clientClientApiService;

	public AppBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													 AppUserCommonService appUserCommonService,
													 EndpointClientApiService endpointClientApiService,
													 ClientClientApiService clientClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appUserCommonService = appUserCommonService;
		this.endpointClientApiService = endpointClientApiService;
		this.clientClientApiService = clientClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "app_biz_log:get_app_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AppBizLog> getAppBizLogPageList(@Valid @NotNull String appId, String endpointId, String clientId, @Validated GetAppBizLogArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.APP_ID).is(appId);
		}

		if (endpointId != null && !endpointId.isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}

		if (clientId != null && !clientId.isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTImeCriteria = criteria.and(BizLogAppMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTImeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTImeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getUserId() != null && !args.getUserId().isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogAppMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogAppMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogAppMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogAppMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogAppMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);


		long total = readMongoTemplate.count(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogAppMongodb.FIELD.START_TIME)));

		List<BizLogAppMongodb> mongodbList = readMongoTemplate.find(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
		List<AppBizLog> contents = getAppBizLogList(appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<AppBizLog> getAppBizLogList(String appId, List<BizLogAppMongodb> list) {
		Set<String> userIds = list.stream().map(BizLogAppMongodb::getUserId).collect(Collectors.toSet());
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

		Map<String, BasicClient> clientMap;
		List<String> clientIds = list.stream().map(BizLogAppMongodb::getClientId).distinct().collect(Collectors.toList());
		if (!clientIds.isEmpty()) {
			List<BasicClient> basicClientList = clientClientApiService.getBasicClientList(GetClientArgs.builder()
				.clientIds(clientIds)
				.build());
			clientMap = Optional.ofNullable(basicClientList).stream().flatMap(Collection::stream).collect(Collectors.toMap(BasicClient::getClientId, x -> x));
		} else {
			clientMap = Collections.emptyMap();
		}

		return list.stream().map(x -> endpointBizLog(x, appEndointMap, clientMap, userMap)).collect(Collectors.toList());
	}

	public AppBizLog endpointBizLog(BizLogAppMongodb m, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap, Map<String, AppUser> userMap) {
		return AppBizLog.builder()
			.logId(m.getLogId())
			.endpointId(m.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getEndpointName).orElse(m.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(m.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.user(Optional.ofNullable(userMap.get(m.getUserId())).orElse(AppUser.builder()
				.userId(m.getUserId())
				.nickname(m.getUserId())
				.build())
			)
			.endpointUserTokenId(m.getTokenId())
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
