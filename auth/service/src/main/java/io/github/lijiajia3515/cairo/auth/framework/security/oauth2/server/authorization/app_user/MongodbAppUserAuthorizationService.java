package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.app_user;


import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
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
import java.util.Optional;

@Slf4j
public class MongodbAppUserAuthorizationService implements AppUserAuthorizationService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;

	private final AppUserAuthorizationMapper appUserAuthorizationMapper;

	private final AppUserAuthorizationMongodbMapper appUserAuthorizationMongodbMapper;

	public MongodbAppUserAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
													  TransactionTemplate transactionTemplate,
													  @Qualifier("readTemplate") MongoTemplate readMongoTemplate,
													  RedisTemplate<String, Object> redisTemplate,
													  AppUserAuthorizationMapper appUserAuthorizationMapper,
													  AppUserAuthorizationMongodbMapper appUserAuthorizationMongodbMapper) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.appUserAuthorizationMapper = appUserAuthorizationMapper;
		this.appUserAuthorizationMongodbMapper = appUserAuthorizationMongodbMapper;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		Query query = Query.query(Criteria.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId()));
		AppUserAuthorizationMongodb existsAuthorization = mongoTemplate.findOne(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		AppUserAuthorizationMongodb mongodb = appUserAuthorizationMongodbMapper.apply(authorization);
		if (existsAuthorization == null) {
			mongodb.setStatus(AppUserAuthorizationStatus.OK.getStatusValue());
			mongodb.setLoginTime(LocalDateTime.now());
			mongodb.setCreateTime(LocalDateTime.now());
			mongodb.setUpdateTime(LocalDateTime.now());
			mongoTemplate.insert(mongodb, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		} else {
			Update update = new Update();
			update.set(AppUserAuthorizationMongodb.FIELD.APP_ID, mongodb.getAppId());
			update.set(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID, mongodb.getEndpointId());
			update.set(AppUserAuthorizationMongodb.FIELD.USER_ID, mongodb.getUserId());
			update.set(AppUserAuthorizationMongodb.FIELD.USER_NAME, mongodb.getUserName());
			update.set(AppUserAuthorizationMongodb.FIELD.CLIENT_ID, mongodb.getClientId());
			update.set(AppUserAuthorizationMongodb.FIELD.REGISTERED_CLIENT_ID, mongodb.getRegisteredClientId());
			update.set(AppUserAuthorizationMongodb.FIELD.AUTHORIZATION_GRANT_TYPE, mongodb.getAuthorizationGrantType());
			update.set(AppUserAuthorizationMongodb.FIELD.AUTHORIZED_SCOPES, mongodb.getAuthorizedScopes());
			update.set(AppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.SELF, mongodb.getAccessToken());
			update.set(AppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.SELF, mongodb.getRefreshToken());
			update.set(AppUserAuthorizationMongodb.FIELD.ATTRIBUTES, mongodb.getAttributes());
			update.currentDate(AppUserAuthorizationMongodb.FIELD.UPDATE_TIME);

			// remove cache
			if (existsAuthorization.getAccessToken() != null && existsAuthorization.getAccessToken().getTokenValue() != null) {
				redisTemplate.delete(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, existsAuthorization.getAppId(), existsAuthorization.getEndpointId(), existsAuthorization.getAccessToken().getTokenValue()));
			}
			mongoTemplate.updateFirst(query, update, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		}
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		AppUserAuthorizationMongodb removedAuthorization = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId());
				Query query = Query.query(criteria);
				AppUserAuthorizationMongodb removed = mongoTemplate.findAndRemove(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
				if (removed != null) {
					mongoTemplate.insert(removed, MongodbConstants.DeletedCollection.APP_USER_AUTHORIZATION);
				}
				return removed;
			} catch (Exception e) {
				log.info("remove fail", e);
				status.setRollbackOnly();
				return null;
			}
		});

		// remove cache
		if (removedAuthorization != null && removedAuthorization.getAccessToken() != null && removedAuthorization.getAccessToken().getTokenValue() != null) {
			redisTemplate.delete(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, removedAuthorization.getAppId(), removedAuthorization.getEndpointId(), removedAuthorization.getAccessToken().getTokenValue()));
		}

	}

	@Override
	public OAuth2Authorization findById(String appId, String endpointId, String id) {
		Assert.hasText(appId, "appId cannot be empty");
		Assert.hasText(endpointId, "endpointId cannot be empty");
		Assert.hasText(id, "id cannot be empty");
		Query query = Query.query(Criteria
			.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(id)
			.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue())
		);
		AppUserAuthorizationMongodb mongodb = readMongoTemplate.findOne(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		return Optional.ofNullable(mongodb)
			.map(appUserAuthorizationMapper)
			.orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String appId, String endpointId, String token, OAuth2TokenType tokenType) {
		Assert.hasText(appId, "appId cannot be empty");
		Assert.hasText(endpointId, "endpointId cannot be empty");
		Assert.hasText(token, "token cannot be empty");

		AppUserAuthorizationMongodb authorization = null;
		// find redis
		if (CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			try {
				authorization = (AppUserAuthorizationMongodb) redisTemplate.opsForValue().get(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, appId, endpointId, token));
			} catch (Exception e) {
				log.warn("redis get ", e);
				redisTemplate.delete(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, appId, endpointId, token));
			}
		}

		if (authorization != null) {
			return appUserAuthorizationMapper.apply(authorization);
		}

		// find db
		Criteria criteria = null;
		if (tokenType == null) {
			criteria = new Criteria().andOperator(
				Criteria.where(AppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token),
				Criteria.where(AppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token)
			);
			criteria.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue());
		}
		// app endpoint user
		else if (CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			criteria = Criteria
				.where(AppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token)
				.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue());
		} else if (CairoOAuthTokenTypeConstants.APP_USER_REFRESH_TOKEN.equals(tokenType)) {
			criteria = Criteria
				.where(AppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token)
				.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue());
		}

		if (criteria != null) {
			Query query = Query.query(criteria);
			authorization = readMongoTemplate.findOne(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		}

		// save cache（token 已过期时剩余时长为负，Redis 拒绝 PX 负值；跳过缓存，交由调用方 isExpired 走过期路径）
		if (authorization != null && CairoOAuthTokenTypeConstants.APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			Duration timeout = Duration.between(Instant.now(), authorization.getAccessToken().getExpiresAt());
			if (!timeout.isNegative() && !timeout.isZero()) {
				redisTemplate.opsForValue().set(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, appId, endpointId, token), authorization, timeout);
			}
		}

		return Optional.ofNullable(authorization).map(appUserAuthorizationMapper).orElse(null);
	}
}
