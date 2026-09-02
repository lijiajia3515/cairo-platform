package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.tenant_app_user;


import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.CairoOAuthTokenTypeConstants;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class MongodbTenantAppUserAuthorizationService implements TenantAppUserAuthorizationService {
	private final MongoTemplate mongoTemplate;

	private final MongoTemplate readMongoTemplate;

	private final RedisTemplate<String, Object> redisTemplate;

	private final TenantAppUserAuthorizationMapper tenantAppUserAuthorizationMapper;

	private final TenantAppUserAuthorizationMongodbMapper tenantAppUserAuthorizationMongodbMapper;

	public MongodbTenantAppUserAuthorizationService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
																@Qualifier("readTemplate") MongoTemplate readMongoTemplate,
																RedisTemplate<String, Object> redisTemplate,
																TenantAppUserAuthorizationMapper tenantAppUserAuthorizationMapper,
																TenantAppUserAuthorizationMongodbMapper tenantAppUserAuthorizationMongodbMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.redisTemplate = redisTemplate;
		this.tenantAppUserAuthorizationMapper = tenantAppUserAuthorizationMapper;
		this.tenantAppUserAuthorizationMongodbMapper = tenantAppUserAuthorizationMongodbMapper;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		Query query = Query.query(Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId()));
		TenantAppUserAuthorizationMongodb existsAuthorization = mongoTemplate.findOne(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		TenantAppUserAuthorizationMongodb mongodb = tenantAppUserAuthorizationMongodbMapper.apply(authorization);
		if (existsAuthorization == null) {
			mongodb.setStatus(TenantAppUserAuthorizationStatus.OK.getStatusValue());
			mongodb.setLoginTime(LocalDateTime.now());
			mongodb.setCreateTime(LocalDateTime.now());
			mongodb.setUpdateTime(LocalDateTime.now());
			mongoTemplate.insert(mongodb, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		} else {
			Update update = new Update();
			update.set(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID, mongodb.getTenantId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.APP_ID, mongodb.getAppId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID, mongodb.getEndpointId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.USER_ID, mongodb.getUserId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.USER_NAME, mongodb.getUserName());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.CLIENT_ID, mongodb.getClientId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.REGISTERED_CLIENT_ID, mongodb.getRegisteredClientId());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.AUTHORIZATION_GRANT_TYPE, mongodb.getAuthorizationGrantType());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.AUTHORIZED_SCOPES, mongodb.getAuthorizedScopes());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.SELF, mongodb.getAccessToken());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.SELF, mongodb.getRefreshToken());
			update.set(TenantAppUserAuthorizationMongodb.FIELD.ATTRIBUTES, mongodb.getAttributes());
			update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.UPDATE_TIME);

			// remove cache
			if (existsAuthorization.getAccessToken() != null && existsAuthorization.getAccessToken().getTokenValue() != null) {
				redisTemplate.delete(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN, existsAuthorization.getTenantId(), existsAuthorization.getAppId(), existsAuthorization.getEndpointId(), existsAuthorization.getAccessToken().getTokenValue()));
			}
			mongoTemplate.updateFirst(query, update, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		}
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		Criteria criteria = Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(authorization.getId());
		Query query = Query.query(criteria);
		TenantAppUserAuthorizationMongodb removed = mongoTemplate.findAndRemove(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		if (removed != null) {
			mongoTemplate.insert(removed, MongodbConstants.DeletedCollection.TENANT_APP_USER_AUTHORIZATION);

			// remove cache
			if (removed.getAccessToken() != null && removed.getAccessToken().getTokenValue() != null) {
				redisTemplate.delete(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN, removed.getTenantId(), removed.getAppId(), removed.getEndpointId(), removed.getAccessToken().getTokenValue()));
			}
		}

	}

	@Override
	public OAuth2Authorization findById(String tenantId, String appId, String endpointId, String id) {
		Assert.hasText(tenantId, "tenantId cannot be empty");
		Assert.hasText(appId, "appId cannot be empty");
		Assert.hasText(endpointId, "endpointId cannot be empty");
		Assert.hasText(id, "id cannot be empty");
		Query query = Query.query(Criteria
			.where(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(id)
			.and(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(TenantAppUserAuthorizationStatus.OK.getStatusValue())
		);
		TenantAppUserAuthorizationMongodb mongodb = readMongoTemplate.findOne(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
		return Optional.ofNullable(mongodb)
			.map(tenantAppUserAuthorizationMapper)
			.orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String tenantId, String appId, String endpointId, String token, OAuth2TokenType tokenType) {
		Assert.hasText(tenantId, "tenantId cannot be empty");
		Assert.hasText(appId, "appId cannot be empty");
		Assert.hasText(endpointId, "endpointId cannot be empty");
		Assert.hasText(token, "token cannot be empty");

		TenantAppUserAuthorizationMongodb authorization = null;
		// find redis
		if (CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			try {
				authorization = (TenantAppUserAuthorizationMongodb) redisTemplate.opsForValue().get(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN, tenantId, appId, endpointId, token));
			} catch (Exception e) {
				log.warn("redis get ", e);
				redisTemplate.delete(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN, tenantId, appId, endpointId, token));
			}
		}

		if (authorization != null) {
			return tenantAppUserAuthorizationMapper.apply(authorization);
		}

		// find db
		Criteria criteria = new Criteria();
		criteria.and(TenantAppUserAuthorizationMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserAuthorizationMongodb.FIELD.ENDPOINT_ID).is(endpointId);

		if (tokenType == null) {
			criteria.andOperator(
				Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token),
				Criteria.where(TenantAppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token)
			);
		}
		// tenant app user
		else if (CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.ACCESS_TOKEN.TOKEN_VALUE).is(token);
		} else if (CairoOAuthTokenTypeConstants.TENANT_APP_USER_REFRESH_TOKEN.equals(tokenType)) {
			criteria.and(TenantAppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.TOKEN_VALUE).is(token);
		} else {
			return null;
		}

		criteria.and(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(TenantAppUserAuthorizationStatus.OK.getStatusValue());

		Query query = Query.query(criteria);
		authorization = readMongoTemplate.findOne(query, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);

		// save cache（token 已过期时剩余时长为负，Redis 拒绝 PX 负值；跳过缓存，交由调用方 isExpired 走过期路径）
		if (authorization != null && CairoOAuthTokenTypeConstants.TENANT_APP_USER_ACCESS_TOKEN.equals(tokenType)) {
			Duration timeout = Duration.between(Instant.now(), authorization.getAccessToken().getExpiresAt());
			if (!timeout.isNegative() && !timeout.isZero()) {
				redisTemplate.opsForValue().set(String.format("%s:%s:%s:%s:%s", CairoAuthRedisConstants.Keys.TENANT_APP_USER_ACCESS_TOKEN, tenantId, appId, endpointId, token), authorization, timeout);
			}
		}

		return Optional.ofNullable(authorization).map(tenantAppUserAuthorizationMapper).orElse(null);
	}
}
