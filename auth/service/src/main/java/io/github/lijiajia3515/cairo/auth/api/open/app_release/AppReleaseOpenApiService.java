package io.github.lijiajia3515.cairo.auth.api.open.app_release;


import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.CheckForUpdateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.CurrentAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetCurrentAppReleasePageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetLatestAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.GetPreviewAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppReleaseMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_release.AppReleaseConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.AppReleaseType;
import io.github.lijiajia3515.cairo.auth.domain.api.open.app_release.OpenAppRelease;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppReleaseOpenApiService {
	private final MongoTemplate readMongoTemplate;
	private final EndpointClientApiService endpointClientApiService;

	AppReleaseOpenApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,EndpointClientApiService endpointClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.endpointClientApiService = endpointClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:getLatestReleaseWeb",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestReleaseWeb(GetLatestAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.WEB.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(true)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:get_latest_release_app",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestReleaseAndroid(GetLatestAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.ANDROID.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(true)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:get_latest_release_ios",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestReleaseIos(GetLatestAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.IOS.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(true)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}


	@NewSpan
	@BizLog(
		bizId = "app_release:get_latest_preview_web",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestPreviewWeb(GetPreviewAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.WEB.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(false)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:get_latest_preview_android",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestPreviewAndroid(GetPreviewAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.ANDROID.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(false)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:get_latest_preview_ios",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease getLatestPreviewIos(GetPreviewAppReleaseArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.IOS.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(false)
			.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(true);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}


	@NewSpan
	@BizLog(
		bizId = "app_release:check_for_updates_android",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease checkForUpdatesAndroid(CheckForUpdateArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.ANDROID.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(args.isReleaseVersion());

		if (args.getCurrentAppVersion() != null) {
			criteria.and(AppReleaseMongodb.FIELD.APP_VERSION).gt(args.getCurrentAppVersion());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:check_for_updates_ios",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenAppRelease checkForUpdatesIos(CheckForUpdateArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(AppReleaseType.IOS.getTypeValue())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(args.isReleaseVersion());

		if (args.getCurrentAppVersion() != null) {
			criteria.and(AppReleaseMongodb.FIELD.APP_VERSION).gt(args.getCurrentAppVersion());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)));
		AppReleaseMongodb one = readMongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
		if (one == null) return null;
		return getAppReleaseVersionList(Collections.singletonList(one)).stream().findFirst().orElse(null);
	}

	@NewSpan
	@BizLog(
		bizId = "app_release:get_current_app_release_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<CurrentAppRelease> getCurrentAppReleasePageList(GetCurrentAppReleasePageListArgs args) {
		Criteria criteria = Criteria
			.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppReleaseMongodb.FIELD.TYPE).is(args.getType())
			.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(args.isReleaseVersion());

		Query query = Query.query(criteria);
		//
		long total = readMongoTemplate.count(query, MongodbConstants.Collection.APP_RELEASE);

		// 排序
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.APP_VERSION)))
			.with(args.pageable());

		// 查询
		List<AppReleaseMongodb> appReleaseMongodbList = readMongoTemplate.find(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);

		List<CurrentAppRelease> currentAppReleaseList = appReleaseMongodbList.stream().map(x -> CurrentAppRelease.builder()
			.appVersion(x.getAppVersion())
			.remark(x.getRemark())
			.title(x.getTitle())
			.updateTime(x.getMetadata().getUpdateTime())
			.build()).collect(Collectors.toList());
		return new Page<>(args, currentAppReleaseList, total);
	}


	List<OpenAppRelease> getAppReleaseVersionList(List<AppReleaseMongodb> as) {
		// 终端
		List<GetEndpointByAppClientArgs.EndpointInfo> endpointInfos = new ArrayList<>();
		as.forEach(x -> {
			GetEndpointByAppClientArgs.EndpointInfo endpointInfo = GetEndpointByAppClientArgs.EndpointInfo.builder().appId(x.getAppId()).endpointId(x.getEndpointId()).build();
			endpointInfos.add(endpointInfo);
		});

		List<Endpoint> endpointByAppList = endpointClientApiService.getEndpointByAppList(GetEndpointByAppClientArgs.builder().EndpointInfos(endpointInfos).build());
		Map<String, Endpoint> appEndointMap = Optional.ofNullable(endpointByAppList).stream().flatMap(Collection::stream).collect(Collectors.toMap(Endpoint::getEndpointId, x -> x));

		return as.stream().map(x -> AppReleaseConverter.convertOpenAppReleaseVersion(x, appEndointMap)).collect(Collectors.toList());
	}
}
