package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.login_log.account_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.account_login_log.AccountLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.login_log.account_login_log.GetAccountLoginLogArgs;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [endpoint_user/api] account login log service
 */
@Slf4j
@Validated
@Component
public class AccountLoginLogCairoEndpointUserApiService {
	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final ClientCommonService clientCommonService;
	private final AccountCommonService accountCommonService;

	public AccountLoginLogCairoEndpointUserApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													  AppCommonService appCommonService,
													  ClientCommonService clientCommonService,
													  AccountCommonService accountCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.clientCommonService = clientCommonService;
		this.accountCommonService = accountCommonService;
	}

	@NewSpan
	@BizLog(
		bizId = "account_login_log:get_account_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AccountLoginLog> getAccountLoginLogPageList(String appId, String clientId, @Validated GetAccountLoginLogArgs args) {
		Criteria criteria = new Criteria();
		if (appId != null && !appId.isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.APP_ID).is(appId);
		}

		if (clientId != null && !clientId.isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.CLIENT_ID).is(clientId);
		}

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and(AccountLoginLogMongodb.FIELD.LOGIN_TIME);
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getLoginType() != null && !args.getLoginType().isEmpty()) {
			criteria.and(AccountLoginLogMongodb.FIELD.LOGIN_TYPE).is(args.getLoginType());
		}

		if (args.getSuccess() != null) {
			criteria.and(AccountLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
		}

		if (args.getAccountId() != null && !args.getAccountId().isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AccountLoginLogMongodb.FIELD.ERR_MSG).regex(args.getKeyword()),
				Criteria.where(AccountLoginLogMongodb.FIELD.OS).regex(args.getKeyword()),
				Criteria.where(AccountLoginLogMongodb.FIELD.PLATFORM).regex(args.getKeyword()),
				Criteria.where(AccountLoginLogMongodb.FIELD.ENGINE).regex(args.getKeyword()),
				Criteria.where(AccountLoginLogMongodb.FIELD.APP).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(AccountLoginLogMongodb.FIELD.LOGIN_TIME)));


		List<AccountLoginLogMongodb> mongodbList = readMongoTemplate.find(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);

		Set<String> appIds = mongodbList.stream().map(AccountLoginLogMongodb::getAppId).collect(Collectors.toSet());
		Map<String, App> appMap;
		if (!appIds.isEmpty()) {
			appMap = appCommonService.getAppMapByAppIds(appIds);
		} else {
			appMap = Collections.emptyMap();
		}

		Set<String> clientIds = mongodbList.stream().map(AccountLoginLogMongodb::getClientId).collect(Collectors.toSet());
		Map<String, BasicClient> clientMap;
		if (!clientIds.isEmpty()) {
			clientMap = clientCommonService.getClientMapByClientIds(clientIds);
		} else {
			clientMap = Collections.emptyMap();
		}

		Set<String> accountIds = mongodbList.stream().map(AccountLoginLogMongodb::getAccountId).collect(Collectors.toSet());
		Map<String, Account> accountMap;
		if (!accountIds.isEmpty()) {
			accountMap = accountCommonService.getAccountMapByAccountIds(accountIds);
		} else {
			accountMap = Collections.emptyMap();
		}

		List<AccountLoginLog> contents = mongodbList.stream()
			.map(x -> accountLoginLog(x, appMap, clientMap, accountMap))
			.collect(Collectors.toList());

		return new Page<>(args, contents, total);
	}

	public AccountLoginLog accountLoginLog(AccountLoginLogMongodb source, Map<String, App> appMap, Map<String, BasicClient> clientMap, Map<String, Account> accountMap) {
		return AccountLoginLog.builder()
			.logId(source.getLogId())
			.loginTime(source.getLoginTime())
			.appId(source.getAppId())
			.appName(Optional.ofNullable(appMap.get(source.getAppId())).map(App::getAppName).orElse(source.getAppId()))
			.appIcon(Optional.ofNullable(appMap.get(source.getApp())).map(App::getIcon).orElse(null))
			.clientId(source.getClientId())
			.clientName(Optional.ofNullable(clientMap.get(source.getClientId())).map(BasicClient::getClientName).orElse(source.getClientId()))
			.account(Optional.ofNullable(accountMap.get(source.getAccountId())).orElse(Account.builder()
				.accountId(source.getAccountId())
				.nickname(source.getAccountTokenId())
				.build())
			)
			.accountTokenId(source.getAccountTokenId())
			.authType(source.getAuthType())
			.loginType(source.getLoginType())
			.snsType(source.getSnsType())
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
