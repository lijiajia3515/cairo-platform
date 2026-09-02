package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.open_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.open_biz_log.GetOpenBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.open_biz_log.OpenBizLog;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogOpenMongodb;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [cairo_web_manage/api] open biz log service
 */
@Slf4j
@Validated
@Component
public class OpenBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;

	private final AppClientApiService appClientApiService;

	public OpenBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												  AppClientApiService appClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "open_biz_log:get_open_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<OpenBizLog> getOpenBizLogPageList(String appId, @Validated GetOpenBizLogArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(BizLogOpenMongodb.FIELD.APP_ID).is(appId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTImeCriteria = criteria.and(BizLogOpenMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTImeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTImeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogOpenMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogOpenMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogOpenMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogOpenMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogOpenMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);


		long total = readMongoTemplate.count(query, BizLogOpenMongodb.class, MongodbConstants.Collection.BIZ_LOG_OPEN);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogOpenMongodb.FIELD.START_TIME)));

		List<BizLogOpenMongodb> mongodbList = readMongoTemplate.find(query, BizLogOpenMongodb.class, MongodbConstants.Collection.BIZ_LOG_OPEN);
		List<OpenBizLog> contents = getOpenBizLog(mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<OpenBizLog> getOpenBizLog(List<BizLogOpenMongodb> list) {

		// appMap
		Map<String, App> appMap;
		List<String> appIds = list.stream().map(BizLogOpenMongodb::getAppId).distinct().collect(Collectors.toList());
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(appIds)
			.build());
		if (!appIds.isEmpty()) {
			appMap = Optional.ofNullable(appList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(App::getAppId, g -> g));
		} else {
			appMap = Collections.emptyMap();
		}

		return list.stream().map(x -> openBizLog(x, appMap)).collect(Collectors.toList());
	}

	public OpenBizLog openBizLog(BizLogOpenMongodb m, Map<String, App> appMap) {
		return OpenBizLog.builder()
			.logId(m.getLogId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
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
