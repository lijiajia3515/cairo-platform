package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.login_log.tenant_app_user_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.TenantAppUserLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.tenant_app_user_login_log.GetTenantAppUserLoginLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.tenant_app_user_login_log.TenantAppUserLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [endpoint_user/api] endpoint user login log service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserLoginLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;

	private final TenantAppUserCommonService tenantAppUserCommonService;

	public TenantAppUserLoginLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
																 TenantCommonService tenantCommonService,
																 AppCommonService appCommonService,
																 EndpointCommonService endpointCommonService,
																 ClientCommonService clientCommonService,
																 TenantAppUserCommonService tenantAppUserCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_login_log:get_tenant_app_user_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<TenantAppUserLoginLog> getTenantAppUserLoginLogPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, String endpointId, String clientId, @Validated GetTenantAppUserLoginLogArgs args) {
		Criteria criteria = new Criteria();
		if (tenantId != null && !tenantId.isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.TENANT_ID).is(tenantId);
		}
		if (appId != null && !appId.isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.APP_ID).is(appId);
		}
		if (endpointId != null && !endpointId.isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		}
		if (clientId != null && !clientId.isBlank()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(TenantAppUserLoginLogMongodb.FIELD.LOGIN_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getLoginType() != null && !args.getLoginType().isEmpty()) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.LOGIN_TYPE).is(args.getLoginType());
		}

		if (args.getSuccess() != null) {
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.ERR_MSG).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.OS).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.PLATFORM).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.ENGINE).regex(args.getKeyword()),
				Criteria.where(TenantAppUserLoginLogMongodb.FIELD.APP).regex(args.getKeyword())
			);
		}

		if (args.getUserId() != null && !args.getUserId().isBlank()){
			criteria.and(TenantAppUserLoginLogMongodb.FIELD.USER_ID).is(args.getUserId());
		}

		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc("login_time")));


		List<TenantAppUserLoginLogMongodb> mongodbList = readMongoTemplate.find(query, TenantAppUserLoginLogMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_LOGIN_LOG);
		Set<String> tenantIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getTenantId).collect(Collectors.toSet());
		Map<String, Tenant> tenantMap;
		if (!tenantIds.isEmpty()) {
			tenantMap = tenantCommonService.getBasicTenantMapByTenantIds(tenantIds);
		} else {
			tenantMap = Collections.emptyMap();
		}

		Set<String> appIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getAppId).collect(Collectors.toSet());
		Map<String, App> appMap;
		if (!appIds.isEmpty()) {
			appMap = appCommonService.getAppMapByAppIds(appIds);
		} else {
			appMap = Collections.emptyMap();
		}

		Set<String> endpointIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getEndpointId).collect(Collectors.toSet());
		Map<String, Endpoint> endpointMap;
		if (!endpointIds.isEmpty()) {
			endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);
		} else {
			endpointMap = Collections.emptyMap();
		}

		Set<String> clientIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getClientId).collect(Collectors.toSet());
		Map<String, BasicClient> clientMap;
		if (!clientIds.isEmpty()) {
			clientMap = clientCommonService.getClientMapByClientIds(clientIds);
		} else {
			clientMap = Collections.emptyMap();
		}

		Set<String> userIds = mongodbList.stream().map(TenantAppUserLoginLogMongodb::getUserId).collect(Collectors.toSet());
		Map<String, TenantAppUser> userMap;
		if (!userIds.isEmpty()) {
			userMap = tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds);
		} else {
			userMap = Collections.emptyMap();
		}

		List<TenantAppUserLoginLog> contents = mongodbList.stream()
			.map(x -> endpointUserLoginLog(x, tenantMap, appMap, endpointMap, clientMap, userMap))
			.collect(Collectors.toList());

		return new Page<>(args, contents, total);
	}

	public TenantAppUserLoginLog endpointUserLoginLog(TenantAppUserLoginLogMongodb source, Map<String, Tenant> tenantMap, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap, Map<String, TenantAppUser> userMap) {
		return TenantAppUserLoginLog.builder()
			.logId(source.getLogId())
			.loginTime(source.getLoginTime())
			.tenantId(source.getTenantId())
			.tenantName(Optional.ofNullable(tenantMap.get(source.getTenantId())).map(Tenant::getTenantName).orElse(source.getTenantId()))
			.tenantIcon(Optional.ofNullable(tenantMap.get(source.getTenantId())).map(Tenant::getIcon).orElse(null))
			.appId(source.getAppId())
			.appName(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getAppName).orElse(source.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getIcon).orElse(null))
			.clientId(source.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(source.getClientId())).map(BasicClient::getClientName).orElse(source.getClientId()))
			.user(Optional.ofNullable(userMap.get(source.getUserId())).orElse(TenantAppUser.builder()
				.userId(source.getUserId())
				.nickname(source.getUserId())
				.build())
			)
			.tenantAppUserTokenId(source.getTenantAppUserTokenId())
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
