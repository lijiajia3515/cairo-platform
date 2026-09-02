package io.github.lijiajia3515.cairo.auth.api.subapp.account;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.account.GetAccountPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.account.SearchAccountArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [subapp_user/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;

	public AccountSubappApiService(
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 查询账号列表
	 *
	 * @param args 参数
	 * @return 账号列表
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<Account> getAccountList(@Validated GetAccountPageListArgs args) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(args.getKeyword()).ifPresent(x -> criteria.orOperator(
			Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).regex(x),
			Criteria.where(AccountMongodb.FIELD.NICKNAME).regex(x),
			Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).regex(x),
			Criteria.where(AccountMongodb.FIELD.EMAIL).regex(x),
			Criteria.where(AccountMongodb.FIELD.USERNAME).regex(x)
		));
		Query query = Query.query(criteria);

		List<AccountMongodb> records = readMongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		return records.stream()
			.map(x -> Account.builder()
				.accountId(x.getAccountId())
				.nickname(x.getNickname())
				.avatarUrl(x.getAvatarUrl())
				.username(x.getUsername())
				.phoneNumber(x.getPhoneNumber())
				.email(x.getEmail())
				.enabled(x.isEnabled())
				.locked(x.isLocked())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(AccountLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime())
				.build())
			.collect(Collectors.toList());
	}

	/**
	 * 获取账号 分页模式
	 *
	 * @param args 分页参数
	 * @return 账号信息 分页模型
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<Account> getAccountPageList(@Validated GetAccountPageListArgs args) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(args.getKeyword()).ifPresent(x -> criteria.orOperator(
			Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).regex(x),
			Criteria.where(AccountMongodb.FIELD.NICKNAME).regex(x),
			Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).regex(x),
			Criteria.where(AccountMongodb.FIELD.EMAIL).regex(x),
			Criteria.where(AccountMongodb.FIELD.USERNAME).regex(x)
		));
		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

		query.with(args.pageable()).with(Sort.by(Sort.Order.desc(AccountMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<AccountMongodb> records = mongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		List<Account> list = records.stream()
			.map(x -> Account.builder()
				.accountId(x.getAccountId())
				.nickname(x.getNickname())
				.avatarUrl(x.getAvatarUrl())
				.username(x.getUsername())
				.phoneNumber(x.getPhoneNumber())
				.email(x.getEmail())
				.enabled(x.isEnabled())
				.locked(x.isLocked())
				.logoffStatus(Optional.ofNullable(x.getLogoffStatus()).orElse(AccountLogoffStatus.NO.getLogoffStatusValue()))
				.logoffPendingTime(x.getLogoffPendingTime())
				.logoffSuccessTime(x.getLogoffSuccessTime())
				.build())
			.collect(Collectors.toList());
		return new Page<>(args, list, total);
	}

	/**
	 * 搜索账号
	 *
	 * @param args 分页参数
	 * @return 账号信息
	 */
	@NewSpan
	@BizLog(
		bizId = "account:search_account",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Account searchAccountInfo(@Validated SearchAccountArgs args) {
		Criteria criteria = new Criteria();
		SearchAccountArgs.Type type = SearchAccountArgs.Type.ofTypeValue(args.getType()).orElse(SearchAccountArgs.Type.PHONE_NUMBER);
		if (type.equals(SearchAccountArgs.Type.ACCOUNT_ID)) {
			if (args.getAccountId() == null || args.getAccountId().isBlank()) {
				throw new ParamsErrorBusinessException("accountId不能为空");
			}
			criteria.and(AccountMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId().trim());
		} else if (type.equals(SearchAccountArgs.Type.PHONE_NUMBER)) {
			if (args.getPhoneNumber() == null || args.getPhoneNumber().isBlank()) {
				throw new ParamsErrorBusinessException("phoneNumber不能为空");
			}
			criteria.and(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber().trim());
		} else if (type.equals(SearchAccountArgs.Type.USERNAME)) {
			if (args.getUsername() == null || args.getUsername().isBlank()) {
				throw new ParamsErrorBusinessException("username不能为空");
			}
			criteria.and(AccountMongodb.FIELD.USERNAME).is(args.getUsername().trim());
		} else if (type.equals(SearchAccountArgs.Type.EMAIL)) {
			if (args.getEmail() == null || args.getEmail().isBlank()) {
				throw new ParamsErrorBusinessException("email不能为空");
			}
			criteria.and(AccountMongodb.FIELD.EMAIL).is(args.getEmail().trim());
		} else {
			throw new ConflictBusinessException("搜索类型不支持");
		}
		Query query = Query.query(criteria);
		query.fields().include(AccountMongodb.FIELD.ACCOUNT_ID, AccountMongodb.FIELD.AVATAR_URL, AccountMongodb.FIELD.NICKNAME, AccountMongodb.FIELD.PHONE_NUMBER, AccountMongodb.FIELD.USERNAME, AccountMongodb.FIELD.EMAIL, AccountMongodb.FIELD.JOIN_TIME);
		AccountMongodb accountMongodb = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (accountMongodb == null) {
			throw new ConflictBusinessException("账号不存在");
		}
		return Account.builder()
			.accountId(accountMongodb.getAccountId())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.nickname(accountMongodb.getNickname())
			.phoneNumber(accountMongodb.getPhoneNumber())
			.username(accountMongodb.getUsername())
			.email(accountMongodb.getEmail())
			.joinTime(accountMongodb.getJoinTime())
			.logoffStatus(accountMongodb.getLogoffStatus())
			.logoffPendingTime(accountMongodb.getLogoffPendingTime())
			.logoffSuccessTime(accountMongodb.getLogoffSuccessTime())
			.build();
	}

	/**
	 * 获取账号信息根据账号id
	 *
	 * @param accountId accountId
	 * @return 账号信息
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_account_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
		}
	)
	public Account getAccountInfo(@Valid @NotNull String accountId) {
		Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query query = Query.query(criteria);

		AccountMongodb account = readMongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		if (account == null) return null;
		return Account.builder()
			.accountId(account.getAccountId())
			.nickname(account.getNickname())
			.avatarUrl(account.getAvatarUrl())
			.username(account.getUsername())
			.phoneNumber(account.getPhoneNumber())
			.email(account.getEmail())
			.nickname(account.getNickname())
			.avatarUrl(account.getAvatarUrl())
			.enabled(account.isEnabled())
			.locked(account.isLocked())
			.logoffStatus(account.getLogoffStatus())
			.logoffPendingTime(account.getLogoffPendingTime())
			.logoffSuccessTime(account.getLogoffSuccessTime())
			.build();
	}
}
