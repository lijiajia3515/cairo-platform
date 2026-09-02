package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.account_authorization;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account_authorization.GetAccountAuthorizationListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account_authorization.OfflineAccountAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.account_authorization.AccountAuthorization;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationConverter;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientCommonService;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * [cairo_web_manage/api] account authorization service
 */
@Slf4j
@Service
@Validated
public class AccountAuthorizationCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ClientCommonService clientCommonService;
	private final AccountAuthorizationCommonService accountAuthorizationCommonService;

	public AccountAuthorizationCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														TransactionTemplate transactionTemplate,
														ClientCommonService clientCommonService,
														AccountAuthorizationCommonService accountAuthorizationCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.clientCommonService = clientCommonService;
		this.accountAuthorizationCommonService = accountAuthorizationCommonService;
	}

	/**
	 * 获取账号会话 集合模式
	 *
	 * @param args 参数
	 * @return 账号会话 list
	 */
	@NewSpan
	@BizLog(
		bizId = "account_authorization:get_account_authorization_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<AccountAuthorization> getAccountAuthorizationList(GetAccountAuthorizationListArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(AccountAuthorizationMongodb.FIELD.UPDATE_TIME),
					Sort.Order.desc(AccountAuthorizationMongodb.FIELD.TOKEN_ID)
				)
			);

		List<AccountAuthorizationMongodb> accountAuthorizationMongodbs = readMongoTemplate.find(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		return getAccountAuthorizationList(accountAuthorizationMongodbs);
	}


	/**
	 * 获取账号会话 分页模式
	 *
	 * @param args 参数
	 * @return 账号会话 page
	 */
	@NewSpan
	@BizLog(
		bizId = "account_authorization:get_account_authorization_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<AccountAuthorization> getAccountAuthorizationPageList(GetAccountAuthorizationListArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(AccountAuthorizationMongodb.FIELD.UPDATE_TIME),
					Sort.Order.desc(AccountAuthorizationMongodb.FIELD.TOKEN_ID)
				)
			);
		long total = readMongoTemplate.count(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);

		query.with(args.pageable());
		List<AccountAuthorizationMongodb> accountAuthorizationMongodbs = readMongoTemplate.find(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		List<AccountAuthorization> accountAuthorizationList = getAccountAuthorizationList(accountAuthorizationMongodbs);
		return new Page<>(args, accountAuthorizationList, total);
	}

	/**
	 * 下线账号会话
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "offline_account_authorization", keys = {"#args.tokenId"})
	@BizLog(
		bizId = "account_authorization:offline_account_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineAccountAuthorization(OfflineAccountAuthorizationArgs args) {
		Query query = Query.query(
			Criteria
				.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(args.getTokenId())
		);
		AccountAuthorizationMongodb authorizationMongodb = mongoTemplate.findOne(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		if (authorizationMongodb == null) {
			throw new ConflictBusinessException("账号会话不存在");
		}
		if (!AccountAuthorizationStatus.OK.getStatusValue().equals(authorizationMongodb.getStatus())) {
			throw new ConflictBusinessException("登录已失效");
		}

		AccountAuthorizationMongodb accountAuthorizationMongodb = transactionTemplate.execute(status -> {
			try {
				Update update = new Update();
				update.set(AccountAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(AccountAuthorizationMongodb.FIELD.LOGOUT_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
				return mongoTemplate.findAndModify(query, update, options, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineAccountAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线账号会话失败");
			}
		});


		if (accountAuthorizationMongodb == null) {
			throw new ConflictBusinessException("登录已失效");
		}

		// 删除cache
		String accessToken = accountAuthorizationMongodb.getAccessToken().getTokenValue();
		accountAuthorizationCommonService.removeAuthorizationCache(accessToken);

	}

	/**
	 * 下线所有账号会话
	 */
	@NewSpan
	@Lock4j(name = "offline_all_account_authorization")
	@BizLog(
		bizId = "account_authorization:offline_all_account_authorization",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void offlineAllAccountAuthorization() {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(
					Criteria
						.where(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue())
				);
				List<AccountAuthorizationMongodb> accountAuthorizationMongodbs = mongoTemplate.find(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
				if (!accountAuthorizationMongodbs.isEmpty()) {
					Query tokenIdQuery = Query.query(
						Criteria
							.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).in(accountAuthorizationMongodbs.stream().map(AccountAuthorizationMongodb::getTokenId).collect(Collectors.toSet()))
					);
					Update update = new Update();
					update.set(AccountAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
					update.currentDate(AccountAuthorizationMongodb.FIELD.LOGOUT_TIME);
					update.set(AccountAuthorizationMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
					mongoTemplate.updateMulti(tokenIdQuery, update, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
				}

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("offlineAccountAuthorization", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("下线账号会话失败");
			}
		});

		accountAuthorizationCommonService.removeAllAuthorizationCache();
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetAccountAuthorizationListArgs args) {
		Criteria criteria = new Criteria();

		if (args.getAccountId() != null && !args.getAccountId().isBlank()) {
			criteria.and(AccountAuthorizationMongodb.FIELD.ACCOUNT_ID).is(args.getAccountId());
		}

		if (args.getClientId() != null && !args.getClientId().isBlank()) {
			criteria.and(AccountAuthorizationMongodb.FIELD.CLIENT_ID).is(args.getClientId());
		}

		if (args.getStatus() != null && !args.getStatus().isBlank()) {
			criteria.and(AccountAuthorizationMongodb.FIELD.STATUS).is(args.getStatus());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).regex(args.getKeyword()),
				Criteria.where(AccountAuthorizationMongodb.FIELD.SNS_TYPE).regex(args.getKeyword()),
				Criteria.where(AccountAuthorizationMongodb.FIELD.LOGIN_TYPE).regex(args.getKeyword())
			);
		}
		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return AccountAuthorization list
	 */
	List<AccountAuthorization> getAccountAuthorizationList(List<AccountAuthorizationMongodb> ms) {

		List<String> clientIds = ms.stream().map(AccountAuthorizationMongodb::getClientId).distinct().collect(Collectors.toList());
		Map<String, BasicClient> clientMap = Optional.of(clientIds)
			.filter(innerClientIds -> !innerClientIds.isEmpty())
			.map(clientCommonService::getClientMapByClientIds)
			.orElse(Collections.emptyMap());
		return ms.stream().map(x -> AccountAuthorizationConverter.convertAccountAuthorization(clientMap, x)).collect(Collectors.toList());
	}
}
