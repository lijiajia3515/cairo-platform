package io.github.lijiajia3515.cairo.auth.api.endpoint.biz_log.subapp_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.subapp.SubappClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.subapp_biz_log.GetMySubappBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.subapp_biz_log.MySubappBizLog;
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
import java.util.stream.Collectors;

/**
 * [app_user/api] app subapp biz log service
 */
@Slf4j
@Validated
@Component
public class SubappBizLogAppApiService {
	private final MongoTemplate readMongoTemplate;
	private final EndpointClientApiService endpointClientApiService;
	private final SubappClientApiService subappClientApiService;

	public SubappBizLogAppApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												 EndpointClientApiService endpointClientApiService,
												 SubappClientApiService subappClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.endpointClientApiService = endpointClientApiService;
		this.subappClientApiService = subappClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "subapp_biz_log:get_my_subapp_biz_log_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MySubappBizLog> getMySubappBizLogList(@Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMySubappBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, userId, args));
		query.with(Sort.by(Sort.Order.desc(BizLogSubappMongodb.FIELD.START_TIME)));
		query.limit(100);
		List<BizLogSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);

		return getMySubappBizLogList(appId, mongodbList);
	}

	@NewSpan
	@BizLog(
		bizId = "subapp_biz_log:get_my_subapp_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MySubappBizLog> getMySubappBizLogPageList(@Valid @NotNull String appId, @Valid @NotNull String userId, @Validated GetMySubappBizLogArgs args) {
		Query query = Query.query(buildCriteria(appId, userId, args));

		long total = readMongoTemplate.count(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogSubappMongodb.FIELD.START_TIME)));

		List<BizLogSubappMongodb> mongodbList = readMongoTemplate.find(query, BizLogSubappMongodb.class, MongodbConstants.Collection.BIZ_LOG_SUBAPP);
		List<MySubappBizLog> contents = getMySubappBizLogList(appId, mongodbList);
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(String appId, String userId, GetMySubappBizLogArgs args) {
		Criteria criteria = Criteria
			.where(BizLogSubappMongodb.FIELD.APP_ID).is(appId)
			.and(BizLogSubappMongodb.FIELD.USER_ID).is(userId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(BizLogSubappMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getSubappId() != null && !args.getSubappId().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.SUBAPP_ID).is(args.getSubappId());
		}
		if (args.getSubappVersion() != null && !args.getSubappVersion().isBlank()) {
			criteria.and(BizLogSubappMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
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

		return criteria;
	}

	public List<MySubappBizLog> getMySubappBizLogList(String appId, List<BizLogSubappMongodb> list) {

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

		return list.stream().map(x -> subappLog(x, appEndointMap, subappMap)).collect(Collectors.toList());
	}

	public MySubappBizLog subappLog(BizLogSubappMongodb m, Map<String, Endpoint> endpointMap, Map<String, Subapp> subappMap) {
		return MySubappBizLog.builder()
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
