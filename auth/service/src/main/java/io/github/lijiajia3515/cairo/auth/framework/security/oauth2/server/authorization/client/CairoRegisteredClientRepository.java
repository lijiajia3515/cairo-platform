package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientConverter;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CairoRegisteredClientRepository implements RegisteredClientRepository {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;

	public CairoRegisteredClientRepository(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	@Override
	@Caching(evict = {
		@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.AUTH_CLIENT, key = " #registeredClient.id", condition = "#registeredClient.id != null"),
		@CacheEvict(cacheNames = CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, key = "#registeredClient.clientId", condition = "#registeredClient.clientId != null")
	})
	@NewSpan
	public void save(RegisteredClient registeredClient) {

	}

	@Override
	@Cacheable(cacheNames = CairoAuthRedisConstants.Keys.AUTH_CLIENT, key = "#id", condition = "#id != null", sync = true)
	@NewSpan
	public CairoRegisteredClient findById(String id) {
		Query query = Query.query(Criteria
			.where(ClientMongodb.FIELD.ID).is(id)
			.and(ClientMongodb.FIELD.ENABLED).is(true)
		);

		ClientMongodb client = readMongoTemplate.findOne(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

		return Optional.ofNullable(client).map(ClientConverter::convert).orElse(null);
	}

	@Override
	@Cacheable(cacheNames = CairoAuthRedisConstants.Keys.AUTH_CLIENT_ID, key = "#clientId", condition = "#clientId != null", sync = true)
	@NewSpan
	public CairoRegisteredClient findByClientId(String clientId) {
		Query query = Query.query(Criteria
			.where(ClientMongodb.FIELD.CLIENT_ID).is(clientId)
			.and(ClientMongodb.FIELD.ENABLED).is(true)
		);

		ClientMongodb client = readMongoTemplate.findOne(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

		return Optional.ofNullable(client).map(ClientConverter::convert).orElse(null);
	}

}
