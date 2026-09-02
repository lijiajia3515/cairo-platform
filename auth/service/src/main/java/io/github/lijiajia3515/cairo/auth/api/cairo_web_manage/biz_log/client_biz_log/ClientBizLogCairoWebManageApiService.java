package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.client_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.client_biz_log.ClientBizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.client_biz_log.GetClientBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogClientMongodb;
import io.micrometer.tracing.annotation.NewSpan;
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
 * [cairo_web_manage/api] client biz log service
 */
@Slf4j
@Validated
@Component
public class ClientBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final ClientClientApiService clientClientApiService;

	public ClientBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													ClientClientApiService clientClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.clientClientApiService = clientClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "client_biz_log:get_client_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<ClientBizLog> getClientBizLogPageList(String appId, String clientId, @Validated GetClientBizLogArgs args) {
		Criteria criteria = new Criteria();
		criteria.and(BizLogClientMongodb.FIELD.APP_ID).is(appId);

		if (clientId != null && !clientId.isBlank()) {
			criteria.and(BizLogClientMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTImeCriteria = criteria.and(BizLogClientMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTImeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTImeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogClientMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogClientMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogClientMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogClientMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogClientMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);


		long total = readMongoTemplate.count(query, BizLogClientMongodb.class, MongodbConstants.Collection.BIZ_LOG_CLIENT);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogClientMongodb.FIELD.START_TIME)));

		List<BizLogClientMongodb> mongodbList = readMongoTemplate.find(query, BizLogClientMongodb.class, MongodbConstants.Collection.BIZ_LOG_CLIENT);
		List<ClientBizLog> contents = getClientBizLogList(mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<ClientBizLog> getClientBizLogList(List<BizLogClientMongodb> list) {
		Map<String, BasicClient> clientMap;
		List<String> clientIds = list.stream().map(BizLogClientMongodb::getClientId).distinct().collect(Collectors.toList());
		if (!clientIds.isEmpty()) {
			List<BasicClient> basicClientList = clientClientApiService.getBasicClientList(GetClientArgs.builder()
				.clientIds(clientIds)
				.build());
			clientMap = Optional.ofNullable(basicClientList).stream().flatMap(Collection::stream).collect(Collectors.toMap(BasicClient::getClientId, x -> x));
		} else {
			clientMap = Collections.emptyMap();
		}

		return list.stream().map(x -> clientBizLog(x, clientMap)).collect(Collectors.toList());
	}

	public ClientBizLog clientBizLog(BizLogClientMongodb m, Map<String, BasicClient> clientMap) {
		return ClientBizLog.builder()
			.logId(m.getLogId())
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.clientTokenId(m.getClientTokenId())
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
