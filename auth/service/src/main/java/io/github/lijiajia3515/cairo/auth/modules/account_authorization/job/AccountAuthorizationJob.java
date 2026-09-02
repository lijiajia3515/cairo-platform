package io.github.lijiajia3515.cairo.auth.modules.account_authorization.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新账号令牌过期状态
 */
@Slf4j
@Component
public class AccountAuthorizationJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AccountAuthorizationCommonService accountAuthorizationCommonService;


	public AccountAuthorizationJob(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate, AccountAuthorizationCommonService accountAuthorizationCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.accountAuthorizationCommonService = accountAuthorizationCommonService;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * 修改过期会话状态任务
	 */
	@XxlJob("modifyAccountAuthorizationStatusJob")
	public void modifyAccountAuthorizationStatusJob() {
		XxlJobHelper.log("modifyAccountAuthorizationStatusJob start");
		try {
			Criteria accountauthorizationCriteria = Criteria.where(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue()).and(AccountAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now());
			Query accountauthorizationQuery = Query.query(accountauthorizationCriteria);

			List<AccountAuthorizationMongodb> list = mongoTemplate.find(accountauthorizationQuery, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
			list.forEach(accountAuthorizationMongodb -> {
				try {
					transactionTemplate.executeWithoutResult(status -> {
						try {
							//修改状态为已过期
							Query authorizationQuery = Query.query(Criteria.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(accountAuthorizationMongodb.getTokenId()));

							Update update = new Update();
							update.set(AccountAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.EXPIRED.getStatusValue());
							update.currentDate(AccountAuthorizationMongodb.FIELD.LOGOUT_TIME);
							update.currentDate(AccountAuthorizationMongodb.FIELD.UPDATE_TIME);

							mongoTemplate.findAndModify(authorizationQuery, update, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
						} catch (Exception e) {
							log.info("修改账号会话状态失败：", e);
						}
					});

					String accessToken = accountAuthorizationMongodb.getAccessToken().getTokenValue();
					accountAuthorizationCommonService.removeAuthorizationCache(accessToken);
				} catch (Exception e) {
					log.warn("modifyAccountAuthorizationStatusJob fail", e);
					XxlJobHelper.log(e.getMessage());
				}
			});
		} catch (Exception e) {
			log.warn("modifyAccountAuthorizationStatusJob", e);
			XxlJobHelper.log("modifyAccountAuthorizationStatusJob error", e.getMessage());
			XxlJobHelper.handleFail("modifyAccountAuthorizationStatusJob fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("modifyAccountAuthorizationStatusJob end");
	}

	/**
	 * 删除过期会话任务
	 */
	@XxlJob("deleteAccountAuthorizationJob")
	public void deleteAccountAuthorizationJob() {
		XxlJobHelper.log("deleteAccountAuthorizationJob start");
		try {
			Criteria accountauthorizationCriteria = Criteria.where(AccountAuthorizationMongodb.FIELD.STATUS).ne(AccountAuthorizationStatus.OK.getStatusValue()).and(AccountAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now().minusDays(7));
			Query accountauthorizationQuery = Query.query(accountauthorizationCriteria);

			List<AccountAuthorizationMongodb> accountAuthorizationMongodbs = mongoTemplate.findAllAndRemove(accountauthorizationQuery, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
			XxlJobHelper.log("删除过期会话：{}条", accountAuthorizationMongodbs.size());
			if (!accountAuthorizationMongodbs.isEmpty()) {
				mongoTemplate.insert(accountAuthorizationMongodbs, MongodbConstants.DeletedCollection.ACCOUNT_AUTHORIZATION);
			}
		} catch (Exception e) {
			log.warn("deleteAccountAuthorizationJob error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("deleteAccountAuthorizationJob fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("deleteAccountAuthorizationJob end");
	}
}
