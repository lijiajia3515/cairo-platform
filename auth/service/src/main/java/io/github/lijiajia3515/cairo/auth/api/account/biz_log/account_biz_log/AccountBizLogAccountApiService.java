package io.github.lijiajia3515.cairo.auth.api.account.biz_log.account_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.account.biz_log.account_biz_log.GetAccountBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.account.biz_log.account_biz_log.MyAccountBizLog;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogAccountMongodb;
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
 * [account/api] account biz log service
 */
@Slf4j
@Validated
@Component
public class AccountBizLogAccountApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppClientApiService appClientApiService;
	private final ClientClientApiService clientClientApiService;

	public AccountBizLogAccountApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										  AppClientApiService appClientApiService,
										  ClientClientApiService clientClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.clientClientApiService = clientClientApiService;
	}

	/**
	 * 获取我的账号业务日志分页列表
	 *
	 * @param accountId 账号ID
	 * @param args      搜索参数
	 * @return 我的账号日志分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "account_biz_log:get_my_account_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MyAccountBizLog> getMyAccountBizLogPageList(@Valid @NotNull String accountId, @Validated GetAccountBizLogArgs args) {
		Criteria criteria = new Criteria();
		criteria.and(BizLogAccountMongodb.FIELD.ACCOUNT_ID).is(accountId);

		if (args.getAppId() != null && !args.getAppId().isBlank()) {
			criteria.and(BizLogAccountMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(BizLogAccountMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria startTImeCriteria = criteria.and(BizLogAccountMongodb.FIELD.START_TIME);
			if (args.getStartTime() != null) {
				startTImeCriteria.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				startTImeCriteria.lte(args.getEndTime());
			}
		}

		if (args.getSuccess() != null) {
			criteria.and(BizLogAccountMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(BizLogAccountMongodb.FIELD.BIZ_ID).regex(args.getKeyword()),
				Criteria.where(BizLogAccountMongodb.FIELD.SCOPE).regex(args.getKeyword()),
				Criteria.where(BizLogAccountMongodb.FIELD.PARAMS).regex(args.getKeyword()),
				Criteria.where(BizLogAccountMongodb.FIELD.ERROR_MESSAGE).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, BizLogAccountMongodb.class, MongodbConstants.Collection.BIZ_LOG_ACCOUNT);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(BizLogAccountMongodb.FIELD.START_TIME)));

		List<BizLogAccountMongodb> mongodbList = readMongoTemplate.find(query, BizLogAccountMongodb.class, MongodbConstants.Collection.BIZ_LOG_ACCOUNT);
		List<MyAccountBizLog> contents = getAccountBizLogList(mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<MyAccountBizLog> getAccountBizLogList(List<BizLogAccountMongodb> list) {
		// appMap
		Map<String, App> appMap;
		List<String> appIds = list.stream().map(BizLogAccountMongodb::getAppId).distinct().collect(Collectors.toList());
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(appIds)
			.build());
		if (!appIds.isEmpty()) {
			appMap = Optional.ofNullable(appList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(App::getAppId, g -> g));
		} else {
			appMap = Collections.emptyMap();
		}

		// clientMap
		Map<String, BasicClient> clientMap;
		List<String> clientIds = list.stream().map(BizLogAccountMongodb::getClientId).distinct().collect(Collectors.toList());
		if (!clientIds.isEmpty()) {
			List<BasicClient> basicClientList = clientClientApiService.getBasicClientList(GetClientArgs.builder()
				.clientIds(clientIds)
				.build());
			clientMap = Optional.ofNullable(basicClientList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(BasicClient::getClientId, x -> x));
		} else {
			clientMap = Collections.emptyMap();
		}

		return list.stream().map(x -> myAccountBizLog(x, appMap, clientMap)).collect(Collectors.toList());
	}

	public MyAccountBizLog myAccountBizLog(BizLogAccountMongodb m, Map<String, App> appMap, Map<String, BasicClient> clientMap) {
		return MyAccountBizLog.builder()
			.logId(m.getLogId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.accountTokenId(m.getAccountTokenId())
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
