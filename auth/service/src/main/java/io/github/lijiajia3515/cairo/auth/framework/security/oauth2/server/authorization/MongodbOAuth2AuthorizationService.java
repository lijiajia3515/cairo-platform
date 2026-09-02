package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization;


import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.OAuth2AuthorizationMongodb;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.util.Assert;

import java.util.Optional;

public class MongodbOAuth2AuthorizationService implements OAuth2AuthorizationService {
	private final MongoTemplate mongoTemplate;

	private final MongoTemplate readMongoTemplate;
	private final OAuth2AuthorizationMapper oAuth2AuthorizationMapper;
	private final OAuth2AuthorizationMongodbMapper oAuth2AuthorizationMongodbMapper;

	public MongodbOAuth2AuthorizationService(MongoTemplate mongoTemplate, MongoTemplate readMongoTemplate, OAuth2AuthorizationMapper oAuth2AuthorizationMapper, OAuth2AuthorizationMongodbMapper oAuth2AuthorizationMongodbMapper) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.oAuth2AuthorizationMapper = oAuth2AuthorizationMapper;
		this.oAuth2AuthorizationMongodbMapper = oAuth2AuthorizationMongodbMapper;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		OAuth2Authorization existingAuthorization = findById(authorization.getId());
		OAuth2AuthorizationMongodb mongodb = oAuth2AuthorizationMongodbMapper.apply(authorization);
		if (existingAuthorization == null) {
			mongoTemplate.insert(mongodb, MongodbConstants.Collection.OAUTH2_AUTHORIZATION);
		} else {
			mongoTemplate.save(mongodb, MongodbConstants.Collection.OAUTH2_AUTHORIZATION);
		}
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");
		Criteria criteria = Criteria.where("_id").is(authorization.getId());
		mongoTemplate.remove(criteria, MongodbConstants.Collection.OAUTH2_AUTHORIZATION);
	}

	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return Optional.ofNullable(readMongoTemplate.findById(id, OAuth2AuthorizationMongodb.class, MongodbConstants.Collection.OAUTH2_AUTHORIZATION))
			.map(oAuth2AuthorizationMapper)
			.orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");
		Criteria criteria = null;
		if (tokenType == null) {
			criteria = new Criteria().andOperator(
				// state
				Criteria.where("State").is(token),
				// authorization code
				Criteria.where("AuthorizationCode.TokenValue").is(token),
				// default
				Criteria.where("AccessToken.TokenValue").is(token),
				Criteria.where("RefreshToken.TokenValue").is(token)
			);
		}
		// state
		else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			criteria = Criteria.where("State").is(token);
		}
		// authorization code
		else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			criteria = Criteria.where("AuthorizationCode.TokenValue").is(token);
		}
		// default
		else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
			criteria = Criteria.where("AccessToken.TokenValue").is(token);
		} else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
			criteria = Criteria.where("RefreshToken.TokenValue").is(token);
		}

		return Optional.ofNullable(criteria)
			.map(Query::query)
			.map(x -> readMongoTemplate.findOne(x, OAuth2AuthorizationMongodb.class, MongodbConstants.Collection.OAUTH2_AUTHORIZATION))
			.map(oAuth2AuthorizationMapper)
			.orElse(null);
	}
}
