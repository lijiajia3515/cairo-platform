package io.github.lijiajia3515.cairo.auth.modules.account_authorization;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
public class AccountAuthorizationCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RedisTemplate<String, Object> redisTemplate;

	public AccountAuthorizationCommonService(
		MongoTemplate mongoTemplate,
		TransactionTemplate transactionTemplate,
		RedisTemplate<String, Object> redisTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.redisTemplate = redisTemplate;
	}

	public void removeAuthorizationCache(String accessToken) {
		redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, accessToken));
	}

	public void removeAllAuthorizationCache() {
		redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, "*"));
	}


//	public void offlineAccountAuthorization(String accountId) {
//		transactionTemplate.executeWithoutResult(status -> {
//			try {
//				Query query = Query.query(
//					Criteria
//						.where(AccountAuthorizationMongodb.FIELD.ACCOUNT_ID).is(accountId)
//						.and(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue())
//				);
//				List<AccountAuthorizationMongodb> list = mongoTemplate.find(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
//				list.forEach(authorization -> {
//					try {
//						Query tokenQuery = Query.query(
//							Criteria
//								.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).in(authorization.getTokenId())
//						);
//						Update update = new Update();
//						update.set(AccountAuthorizationMongodb.FIELD.STATUS, AccountAuthorizationStatus.BLACKLIST.getStatusValue());
//						update.currentDate(AccountAuthorizationMongodb.FIELD.LOGOUT_TIME);
//						update.set(AccountAuthorizationMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getSubappAccountId());
//						mongoTemplate.updateFirst(tokenQuery, update, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
//						removeAuthorizationCache(authorization.getAccessToken().getTokenValue());
//					} catch (Exception e) {
//						log.warn("offlineAccountAuthorization error", e);
//					}
//				});
//
//
//			} catch (BusinessException e) {
//				status.setRollbackOnly();
//				throw e;
//			} catch (Exception e) {
//				log.debug("offlineAccountAuthorization", e);
//				status.setRollbackOnly();
//				throw new ConflictBusinessException(String.format("下线账号[%s]会话失败", accountId));
//			}
//		});
//	}
}
