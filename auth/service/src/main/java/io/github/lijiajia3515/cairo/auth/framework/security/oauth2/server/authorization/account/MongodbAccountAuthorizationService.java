package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.account;


import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AccountAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.modules.account_authorization.AccountAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MongodbAccountAuthorizationService implements AccountAuthorizationService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final AccountAuthorizationMapper accountAuthorizationMapper;
	private final AccountAuthorizationMongodbMapper accountAuthorizationMongodbMapper;

	public MongodbAccountAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate,
											  @Qualifier("readTemplate") MongoTemplate readMongoTemplate,
											  RedisTemplate<String, Object> redisTemplate,
											  AccountAuthorizationMapper accountAuthorizationMapper,
											  AccountAuthorizationMongodbMapper accountAuthorizationMongodbMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.accountAuthorizationMapper = accountAuthorizationMapper;
		this.accountAuthorizationMongodbMapper = accountAuthorizationMongodbMapper;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		Query query = Query.query(Criteria.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId()));
		AccountAuthorizationMongodb existsAuthorization = mongoTemplate.findOne(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);

		AccountAuthorizationMongodb mongodb = accountAuthorizationMongodbMapper.apply(authorization);
		if (existsAuthorization == null) {
			mongodb.setStatus(AccountAuthorizationStatus.OK.getStatusValue());
			mongodb.setLoginTime(LocalDateTime.now());
			mongodb.setCreateTime(LocalDateTime.now());
			mongodb.setUpdateTime(LocalDateTime.now());
			mongoTemplate.insert(mongodb, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		} else {
			Update update = new Update();
			update.set(AccountAuthorizationMongodb.FIELD.ACCOUNT_ID, mongodb.getAccountId());
			update.set(AccountAuthorizationMongodb.FIELD.ACCOUNT_NAME, mongodb.getAccountName());
			update.set(AccountAuthorizationMongodb.FIELD.APP_ID, mongodb.getAppId());
			update.set(AccountAuthorizationMongodb.FIELD.CLIENT_ID, mongodb.getClientId());
			update.set(AccountAuthorizationMongodb.FIELD.REGISTERED_CLIENT_ID, mongodb.getRegisteredClientId());
			update.set(AccountAuthorizationMongodb.FIELD.AUTHORIZATION_GRANT_TYPE, mongodb.getAuthorizationGrantType());
			update.set(AccountAuthorizationMongodb.FIELD.AUTHORIZED_SCOPES, mongodb.getAuthorizedScopes());
			update.set(AccountAuthorizationMongodb.FIELD.ACCESS_TOKEN.SELF, mongodb.getAccessToken());
			update.set(AccountAuthorizationMongodb.FIELD.REFRESH_TOKEN.SELF, mongodb.getRefreshToken());
			update.set(AccountAuthorizationMongodb.FIELD.ATTRIBUTES, mongodb.getAttributes());
			update.currentDate(AccountAuthorizationMongodb.FIELD.UPDATE_TIME);

			if (existsAuthorization.getAccessToken() != null && existsAuthorization.getAccessToken().getTokenValue() != null) {
				redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, existsAuthorization.getAccessToken().getTokenValue()));
			}
			mongoTemplate.updateFirst(query, update, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		}
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");

		AccountAuthorizationMongodb removedAuthorization = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId());
				Query query = Query.query(criteria);
				AccountAuthorizationMongodb removed = mongoTemplate.findAndRemove(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
				if (removed != null) {
					mongoTemplate.insert(removed, MongodbConstants.DeletedCollection.ACCOUNT_AUTHORIZATION);
				}
				return removed;
			} catch (Exception e) {
				log.info("remove fail", e);
				status.setRollbackOnly();
				return null;
			}
		});

		if (removedAuthorization != null && removedAuthorization.getAccessToken() != null && removedAuthorization.getAccessToken().getTokenValue() != null) {
			redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, removedAuthorization.getAccessToken().getTokenValue()));
		}


	}

	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		Query query = Query.query(Criteria
			.where(AccountAuthorizationMongodb.FIELD.TOKEN_ID).is(id)
			.is(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue())
		);
		AccountAuthorizationMongodb mongodb = readMongoTemplate.findOne(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		return Optional.ofNullable(mongodb)
			.map(accountAuthorizationMapper)
			.orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");
		AccountAuthorizationMongodb authorization = null;
		// find redis
		if (CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN.equals(tokenType)) {
			try {
				authorization = (AccountAuthorizationMongodb) redisTemplate.opsForValue().get(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, token));
			} catch (Exception e) {
				log.warn("redis get ", e);
				redisTemplate.delete(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, token));
			}
		}

		if (authorization != null) {
			return accountAuthorizationMapper.apply(authorization);
		}

		// find db
		Criteria criteria = null;
		if (tokenType == null) {
			criteria = new Criteria().andOperator(
				Criteria.where(AccountAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token),
				Criteria.where(AccountAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token)
			);
			criteria.and(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue());
		} else if (CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN.equals(tokenType)) {
			criteria = Criteria
				.where(AccountAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token)
				.and(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue());
		} else if (CairoOAuthTokenTypeConstants.ACCOUNT_REFRESH_TOKEN.equals(tokenType)) {
			criteria = Criteria
				.where(AccountAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token)
				.and(AccountAuthorizationMongodb.FIELD.STATUS).is(AccountAuthorizationStatus.OK.getStatusValue());
		}
		if (criteria != null) {
			Query query = Query.query(criteria);
			authorization = readMongoTemplate.findOne(query, AccountAuthorizationMongodb.class, MongodbConstants.Collection.ACCOUNT_AUTHORIZATION);
		}

		// save cache（token 已过期时剩余时长为负，Redis 拒绝 PX 负值；跳过缓存，交由调用方 isExpired 走过期路径）
		if (authorization != null && CairoOAuthTokenTypeConstants.ACCOUNT_ACCESS_TOKEN.equals(tokenType)) {
			Duration timeout = Duration.between(Instant.now(), authorization.getAccessToken().getExpiresAt());
			if (!timeout.isNegative() && !timeout.isZero()) {
				redisTemplate.opsForValue().set(String.format("%s:%s", CairoAuthRedisConstants.Keys.ACCOUNT_ACCESS_TOKEN, token), authorization, timeout);
			}
		}


		return Optional.ofNullable(authorization).map(accountAuthorizationMapper).orElse(null);
	}
}
