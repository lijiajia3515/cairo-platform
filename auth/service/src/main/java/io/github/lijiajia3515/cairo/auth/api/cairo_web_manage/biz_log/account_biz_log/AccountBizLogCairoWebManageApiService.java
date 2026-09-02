package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.biz_log.account_biz_log;

import io.github.lijiajia3515.cairo.auth.domain.api.client.account.GetAccountListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.api.client.account.AccountClientApiService;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.account_biz_log.AccountBizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.biz_log.account_biz_log.GetAccountBizLogArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.biz_log.BizLogAccountMongodb;
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
 * [cairo_web_manage/api] account biz log service
 */
@Slf4j
@Validated
@Component
public class AccountBizLogCairoWebManageApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppClientApiService appClientApiService;
	private final ClientClientApiService clientClientApiService;
	private final AccountClientApiService accountClientApiService;

	public AccountBizLogCairoWebManageApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, AppClientApiService appClientApiService, ClientClientApiService clientClientApiService, AccountClientApiService accountClientApiService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.clientClientApiService = clientClientApiService;
		this.accountClientApiService = accountClientApiService;
	}

	@NewSpan
	@BizLog(
		bizId = "account_biz_log:get_account_biz_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AccountBizLog> getAccountBizLogPageList(String appId, String clientId, @Validated GetAccountBizLogArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(BizLogAccountMongodb.FIELD.APP_ID).is(appId);
		}

		if (clientId != null && !clientId.isBlank()) {
			criteria.and(BizLogAccountMongodb.FIELD.CLIENT_ID).is(clientId);
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

		if (args.getAccountId() != null && !args.getAccountId().isBlank()) {
			criteria.and(BizLogAccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
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
		List<AccountBizLog> contents = getAccountBizLogList(mongodbList);
		return new Page<>(args, contents, total);
	}

	public List<AccountBizLog> getAccountBizLogList(List<BizLogAccountMongodb> list) {
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
			clientMap = Optional.ofNullable(basicClientList).stream().flatMap(Collection::stream).collect(Collectors.toMap(BasicClient::getClientId, x -> x));
		} else {
			clientMap = Collections.emptyMap();
		}
		// accountMap
		List<String> accountIds = list.stream().map(BizLogAccountMongodb::getAccountId).distinct().collect(Collectors.toList());
		Map<String, Account> accountMap = accountClientApiService.getAccountList(GetAccountListArgs.builder().accountIds(accountIds).build()).stream()
			.collect(Collectors.toMap(Account::getAccountId, g -> g));;

		return list.stream().map(x -> accountBizLog(x, appMap, clientMap, accountMap)).collect(Collectors.toList());
	}

	public AccountBizLog accountBizLog(BizLogAccountMongodb m, Map<String, App> appMap, Map<String, BasicClient> clientMap, Map<String, Account> accountMap) {
		return AccountBizLog.builder()
			.logId(m.getLogId())
			.appId(m.getAppId())
			.appName(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getAppName).orElse(m.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(m.getAppId())).map(App::getIcon).orElse(null))
			.clientId(m.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(m.getClientId())).map(BasicClient::getClientName).orElse(m.getClientId()))
			.account(Optional.ofNullable(accountMap.get(m.getAccountId())).orElse(Account.builder()
				.accountId(m.getAccountId())
				.nickname(m.getAccountId())
				.build())
			)
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
