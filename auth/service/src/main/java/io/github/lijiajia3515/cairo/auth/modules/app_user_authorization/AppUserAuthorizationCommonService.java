package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AppUserAuthorizationCommonService {
	private final MongoTemplate mongoTemplate;
	private final RedisTemplate<String, Object> redisTemplate;

	public AppUserAuthorizationCommonService(
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		RedisTemplate<String, Object> redisTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.redisTemplate = redisTemplate;
	}

	public void removeAuthorizationCache(String appId, String endpointId, String accessToken) {
		redisTemplate.delete(String.format("%s:%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, appId, endpointId, accessToken));
	}

	public void removeAllAuthorizationCache(String appId) {
		redisTemplate.delete(String.format("%s:%s:%s", CairoAuthRedisConstants.Keys.APP_USER_ACCESS_TOKEN, appId, "*"));
	}

	public void offlineEndpointAuthorization(String appId, String userId) {
		Query query = Query.query(
			Criteria
				.where(AppUserAuthorizationMongodb.FIELD.APP_ID).is(appId)
				.and(AppUserAuthorizationMongodb.FIELD.USER_ID).is(userId)
				.and(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue())
		);
		List<AppUserAuthorizationMongodb> list = mongoTemplate.find(query, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
		list.forEach(authorization -> {
			try {
				Query tokenQuery = Query.query(
					Criteria
						.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).in(authorization.getTokenId())
				);
				Update update = new Update();
				update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AppUserAuthorizationStatus.BLACKLIST.getStatusValue());
				update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
				update.set(AppUserAuthorizationMongodb.FIELD.METADATA.UPDATE_USER_ID, null);
				mongoTemplate.updateFirst(tokenQuery, update, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
				removeAuthorizationCache(appId, userId, authorization.getAccessToken().getTokenValue());
			} catch (Exception e) {
				log.warn("offlineAccountAuthorization appId: {} userId: {} accessToken: {} error", appId, userId, authorization.getAccessToken().getTokenValue(), e);
			}
		});
	}
}
