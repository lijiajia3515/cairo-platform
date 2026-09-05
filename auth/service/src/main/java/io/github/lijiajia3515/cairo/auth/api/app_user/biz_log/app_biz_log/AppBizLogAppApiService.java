package io.github.lijiajia3515.cairo.auth.api.app_user.biz_log.app_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.biz_log.app_biz_log.GetMyAppBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.biz_log.app_biz_log.MyAppBizLog;
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

/**
 * [app_user/api] app endpoint biz log service
 */
@Slf4j
@Validated
@Component
public class AppBizLogAppApiService {
	private final MongoTemplate readMongoTemplate;
	private final EndpointClientApiService endpointClientApiService;
	private final ClientClientApiService clientClientApiService;

	public AppBizLogAppApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												  EndpointClientApiService endpointClientApiService,
												  ClientClientApiService clientClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.endpointClientApiService = endpointClientApiService;
		this.clientClientApiService = clientClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "app_biz_log:get_my_app_biz_log_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MyAppBizLog> getMyAppBizLogList(@Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyAppBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, userId, args));
		query.with(Sort.by(Sort.Order.desc(BizLogAppMongodb.FIELD.START_TIME)));
		query.limit(100);
		List<BizLogAppMongodb> mongodbList = readMongoTemplate.find(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);

		return getMyAppBizLogList(appId, mongodbList);
	}

	@NewSpan
	@BizLog(
		bizId = "app_biz_log:get_my_app_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MyAppBizLog> getMyAppBizLogPageList(@Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMyAppBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, userId, args));

		long total = readMongoTemplate.count(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogAppMongodb.FIELD.START_TIME)));

		List<BizLogAppMongodb> mongodbList = readMongoTemplate.find(query, BizLogAppMongodb.class, MongodbConstants.Collection.BIZ_LOG_APP);
		List<MyAppBizLog> contents = getMyAppBizLogList(appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(String appId, String userId, GetMyAppBizLogArgs args) {
		Criteria criteria = Criteria
			.where(BizLogAppMongodb.FIELD.APP_ID).is(appId)
			.and(BizLogAppMongodb.FIELD.USER_ID).is(userId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(BizLogAppMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(BizLogAppMongodb.FIELD.CLIENT_ID).is(args.getClientId());
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

		return criteria;
	}

	public List<MyAppBizLog> getMyAppBizLogList(String appId, List<BizLogAppMongodb> list) {

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

		return list.stream().map(x -> endpointBizLog(x, appEndointMap, clientMap)).collect(Collectors.toList());
	}

	public MyAppBizLog endpointBizLog(BizLogAppMongodb m, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap) {
		return MyAppBizLog.builder()
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
