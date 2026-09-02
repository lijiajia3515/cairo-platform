package io.github.lijiajia3515.cairo.auth.api.account.login_log.account_login_log;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.login_log.AccountLoginLogMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.account.login_log.account_login_log.AccountLoginLog;
import io.github.lijiajia3515.cairo.auth.domain.api.account.login_log.account_login_log.GetAccountLoginLogArgs;
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
import java.util.List;
import java.util.stream.Collectors;


/**
 * [account/api] account login log service
 */
@Slf4j
@Validated
@Component
public class AccountLoginLogAccountApiService {
	private final MongoTemplate readMongoTemplate;

	public AccountLoginLogAccountApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "account_login_log:get_my_account_login_log_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<AccountLoginLog> getMyAccountLoginLogList(@Valid @NotNull String accountId, @Valid @NotNull String appId, @Validated GetAccountLoginLogArgs args) {
		Query query = Query.query(buildCriteria(accountId, appId, args));
		query.with(Sort.by(Sort.Order.desc("login_time")));
		query.limit(100);
		return readMongoTemplate.find(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG).stream()
			.map(this::accountLoginLog)
			.collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "account_login_log:get_my_account_login_log_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<AccountLoginLog> getMyAccountLoginLogPageList(@Valid @NotNull String accountId, @Valid @NotNull String appId, @Validated GetAccountLoginLogArgs args) {
		Query query = Query.query(buildCriteria(accountId, appId, args));

		long total = readMongoTemplate.count(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc("login_time")));

		List<AccountLoginLog> contents = readMongoTemplate.find(query, AccountLoginLogMongodb.class, MongodbConstants.Collection.ACCOUNT_LOGIN_LOG).stream()
			.map(this::accountLoginLog)
			.collect(Collectors.toList());
		return new Page<>(args, contents, total);
	}

	protected Criteria buildCriteria(@Valid @NotNull String accountId, @Valid @NotNull String appId,@Validated GetAccountLoginLogArgs args) {
		Criteria criteria = Criteria
			.where(AccountLoginLogMongodb.FIELD.ACCOUNT_ID).is(accountId);

		if (args.getStartTime() != null || args.getEndTime() != null) {
			Criteria loginTime = criteria.and("login_time");
			if (args.getStartTime() != null) {
				loginTime.gte(args.getStartTime());
			}
			if (args.getEndTime() != null) {
				loginTime.lte(args.getEndTime());
			}
		}

		if (args.getAuthType() != null && !args.getAuthType().isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.AUTH_TYPE).is(args.getAuthType());
		}

		if (args.getAppId() != null && !args.getAppId().isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getLoginType() != null && !args.getLoginType().isBlank()) {
			criteria.and(AccountLoginLogMongodb.FIELD.LOGIN_TYPE).is(args.getLoginType());
		}

		if (args.getSuccess() != null) {
			criteria.and(AccountLoginLogMongodb.FIELD.SUCCESS).is(args.getSuccess());
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

		return criteria;
	}

	public AccountLoginLog accountLoginLog(AccountLoginLogMongodb source) {
		return AccountLoginLog.builder()
			.logId(source.getLogId())
			.authType(source.getAuthType())
			.appId(source.getAppId())
			.clientId(source.getClientId())
			.loginTime(source.getLoginTime())
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
