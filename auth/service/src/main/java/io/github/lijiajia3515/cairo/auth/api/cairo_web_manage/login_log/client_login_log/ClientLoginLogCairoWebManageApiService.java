package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.login_log.client_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.ClientLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.client_login_log.ClientLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.client_login_log.GetClientLoginLogArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
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
 * [cairo_endpoint_user/api] endpoint user login log service
 */
@Slf4j
@Validated
@Component
public class ClientLoginLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final ClientCommonService clientCommonService;


	public ClientLoginLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												  AppCommonService appCommonService,
												  EndpointCommonService endpointCommonService,
												  ClientCommonService clientCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.clientCommonService = clientCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "client_login_log:get_client_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<ClientLoginLog> getClientLoginLogPageList(String appId, String clientId, @Validated GetClientLoginLogArgs args) {
		Criteria criteria = new Criteria();
		if (appId != null && !appId.isBlank()) {
			criteria.and(ClientLoginLogMongodb.FIELD.APP_ID).is(appId);
		}

		if (clientId != null && !clientId.isBlank()) {
			criteria.and(ClientLoginLogMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(ClientLoginLogMongodb.FIELD.LOGIN_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getGrantType() != null && !args.getGrantType().isEmpty()) {
			criteria.and(ClientLoginLogMongodb.FIELD.GRANT_TYPE).is(args.getGrantType());
		}

		if (args.getMethod() != null && !args.getMethod().isEmpty()) {
			criteria.and(ClientLoginLogMongodb.FIELD.METHOD).is(args.getMethod());
		}

		if (args.getSuccess() != null) {
			criteria.and(ClientLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(ClientLoginLogMongodb.FIELD.ERR_MSG).regex(args.getKeyword()),
				Criteria.where(ClientLoginLogMongodb.FIELD.OS).regex(args.getKeyword()),
				Criteria.where(ClientLoginLogMongodb.FIELD.PLATFORM).regex(args.getKeyword()),
				Criteria.where(ClientLoginLogMongodb.FIELD.ENGINE).regex(args.getKeyword()),
				Criteria.where(ClientLoginLogMongodb.FIELD.APP).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, ClientLoginLogMongodb.class, MongodbConstants.Collection.CLIENT_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(ClientLoginLogMongodb.FIELD.LOGIN_TIME)));


		List<ClientLoginLogMongodb> mongodbList = readMongoTemplate.find(query, ClientLoginLogMongodb.class, MongodbConstants.Collection.CLIENT_LOGIN_LOG);

		Set<String> appIds = mongodbList.stream().map(ClientLoginLogMongodb::getAppId).collect(Collectors.toSet());
		Map<String, App> appMap;
		if (!appIds.isEmpty()) {
			appMap = appCommonService.getAppMapByAppIds(appIds);
		} else {
			appMap = Collections.emptyMap();
		}

		Set<String> endpointIds = mongodbList.stream().map(ClientLoginLogMongodb::getEndpointId).collect(Collectors.toSet());
		Map<String, Endpoint> endpointMap;
		if (!endpointIds.isEmpty()) {
			endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);
		} else {
			endpointMap = Collections.emptyMap();
		}

		Set<String> clientIds = mongodbList.stream().map(ClientLoginLogMongodb::getClientId).collect(Collectors.toSet());
		Map<String, BasicClient> clientMap;
		if (!clientIds.isEmpty()) {
			clientMap = clientCommonService.getClientMapByClientIds(clientIds);
		} else {
			clientMap = Collections.emptyMap();
		}


		List<ClientLoginLog> contents = mongodbList.stream()
			.map(x -> clientLoginLog(x, appMap, endpointMap, clientMap))
			.collect(Collectors.toList());

		return new Page<>(args, contents, total);
	}

	public ClientLoginLog clientLoginLog(ClientLoginLogMongodb source, Map<String, App> appMap, Map<String, Endpoint> endpointMap, Map<String, BasicClient> clientMap) {
		return ClientLoginLog.builder()
			.logId(source.getLogId())
			.loginTime(source.getLoginTime())
			.appId(source.getAppId())
			.appName(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getAppName).orElse(source.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getIcon).orElse(null))
			.endpointId(source.getEndpointId())
			.endpointName(Optional.ofNullable(endpointMap.get(source.getEndpointId())).map(Endpoint::getAppName).orElse(source.getEndpointId()))
			.endpointIcon(Optional.ofNullable(endpointMap.get(source.getEndpointId())).map(Endpoint::getIcon).orElse(null))
			.clientId(source.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(source.getClientId())).map(BasicClient::getClientName).orElse(source.getClientId()))
			.clientTokenId(source.getClientTokenId())
			.grantType(source.getGrantType())
			.method(source.getMethod())
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
