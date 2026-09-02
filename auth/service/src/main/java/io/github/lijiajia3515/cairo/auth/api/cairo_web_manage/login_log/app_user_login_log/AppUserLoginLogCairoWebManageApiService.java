package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.login_log.app_user_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.app_user_login_log.AppUserLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.app_user_login_log.GetAppUserLoginLogArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [cairo_endpoint_user/api] app endpoint user login log service
 */
@Slf4j
@Validated
@Component
public class AppUserLoginLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;

	private final AppUserCommonService appUserCommonService;

	public AppUserLoginLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														   AppCommonService appCommonService,
														   EndpointCommonService endpointCommonService,
														   ClientCommonService clientCommonService,
														   AppUserCommonService appUserCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
		this.appUserCommonService = appUserCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "app_user_login_log:get_app_user_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AppUserLoginLog> getAppUserLoginLogPageList(@Valid @NotNull String appId, String endpointId, String clientId, @Validated GetAppUserLoginLogArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null) {
			criteria.and(AppUserLoginLogMongodb.FIELD.APP_ID).is(appId);
		}
		if (endpointId != null) {
			criteria.and(AppUserLoginLogMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}
		if (clientId != null) {
			criteria.and(AppUserLoginLogMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(AppUserLoginLogMongodb.FIELD.LOGIN_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getLoginType() != null && !args.getLoginType().isEmpty()) {
			criteria.and(AppUserLoginLogMongodb.FIELD.LOGIN_TYPE).is(args.getLoginType());
		}

		if (args.getSuccess() != null) {
			criteria.and(AppUserLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AppUserLoginLogMongodb.FIELD.ERR_MSG).regex(args.getKeyword()),
				Criteria.where(AppUserLoginLogMongodb.FIELD.OS).regex(args.getKeyword()),
				Criteria.where(AppUserLoginLogMongodb.FIELD.PLATFORM).regex(args.getKeyword()),
				Criteria.where(AppUserLoginLogMongodb.FIELD.ENGINE).regex(args.getKeyword()),
				Criteria.where(AppUserLoginLogMongodb.FIELD.APP).regex(args.getKeyword())
			);
		}

		//添加userid作为查询条件 2024/06/26 Mr.wang
		if (args.getUserId() != null && !args.getUserId().isEmpty()) {
			criteria.and(AppUserLoginLogMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, AppUserLoginLogMongodb.class, MongodbConstants.Collection.APP_USER_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc("login_time")));


		List<AppUserLoginLogMongodb> mongodbList = readMongoTemplate.find(query, AppUserLoginLogMongodb.class, MongodbConstants.Collection.APP_USER_LOGIN_LOG);

		Set<String> appIds = mongodbList.stream().map(AppUserLoginLogMongodb::getAppId).collect(Collectors.toSet());
		Map<String, App> appMap;
		if (!appIds.isEmpty()) {
			appMap = appCommonService.getAppMapByAppIds(appIds);
		} else {
			appMap = Collections.emptyMap();
		}

		Set<String> endpointIds = mongodbList.stream().map(AppUserLoginLogMongodb::getEndpointId).collect(Collectors.toSet());
		Map<String, Endpoint> endpointMap;
		if (!endpointIds.isEmpty()) {
			endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);
		} else {
			endpointMap = Collections.emptyMap();
		}

		Set<String> clientIds = mongodbList.stream().map(AppUserLoginLogMongodb::getClientId).collect(Collectors.toSet());
		Map<String, BasicClient> clientMap;
		if (!clientIds.isEmpty()) {
			clientMap = clientCommonService.getClientMapByClientIds(clientIds);
		} else {
			clientMap = Collections.emptyMap();
		}

		Set<String> userIds = mongodbList.stream().map(AppUserLoginLogMongodb::getUserId).collect(Collectors.toSet());
		Map<String, AppUser> userMap;
		if (!userIds.isEmpty()) {
			userMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);
		} else {
			userMap = Collections.emptyMap();
		}

		List<AppUserLoginLog> contents = mongodbList.stream()
			.map(x -> appUserEndpointLoginLog(x, appMap, endpointMap, clientMap, userMap))
			.collect(Collectors.toList());

		return new Page<>(args, contents, total);
	}

	public AppUserLoginLog appUserEndpointLoginLog(AppUserLoginLogMongodb source, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap, Map<String, AppUser> userMap) {
		return AppUserLoginLog.builder()
			.logId(source.getLogId())
			.loginTime(source.getLoginTime())
			.appId(source.getAppId())
			.appName(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getAppName).orElse(source.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getIcon).orElse(null))
			.clientId(source.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(source.getClientId())).map(BasicClient::getClientName).orElse(source.getClientId()))
			.user(Optional.ofNullable(userMap.get(source.getUserId())).orElse(AppUser.builder()
				.userId(source.getUserId())
				.nickname(source.getUserId())
				.build())
			)
			.appUserTokenId(source.getAppUserTokenId())
			.loginType(source.getLoginType())
			.success(source.getSuccess())
			.errMsg(source.getErrMsg())
			.ip(source.getIp())
			.region(source.getRegion())
			.os(source.getOs())
			.platform(source.getPlatform())
			.engine(source.getEngine())
			.app(source.getApp())
			.build();
	}
}
