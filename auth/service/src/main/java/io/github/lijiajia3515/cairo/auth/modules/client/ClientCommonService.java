package io.github.lijiajia3515.cairo.auth.modules.client;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class ClientCommonService {
	private final MongoTemplate readMongoTemplate;

	public ClientCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * get client list by client ids
	 *
	 * @param clientIds clientIds
	 * @return app list
	 */
	@NewSpan
	public List<BasicClient> getClientListByClientIds(Collection<String> clientIds) {
		if (clientIds == null || clientIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(ClientMongodb.FIELD.CLIENT_ID).in(clientIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(ClientMongodb.FIELD.CLIENT_ID)));

		List<ClientMongodb> mongodbList = readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		query.fields().include(
			ClientMongodb.FIELD.ID,
			ClientMongodb.FIELD.CLIENT_ID,
			ClientMongodb.FIELD.CLIENT_NAME);
		query.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.CLIENT_ID)));

		List<BasicClient> basicClientList = mongodbList.stream().map(x ->
				BasicClient.builder()
					.id(x.getId())
					.clientId(x.getClientId())
					.clientName(x.getClientName())
					.build())
			.collect(Collectors.toList());

		return basicClientList;
	}

	/**
	 * get client map by appIds
	 *
	 * @param clientIds clientIds
	 * @return client map
	 */
	@NewSpan
	public Map<String, BasicClient> getClientMapByClientIds(Collection<String> clientIds) {
		return getClientListByClientIds(clientIds).stream()
			.collect(Collectors.toMap(BasicClient::getClientId, x -> x, (x1, x2) -> x1));
	}

	/**
	 * 查询是否存在client
	 *
	 * @param appId appId
	 * @return client集合（仅展示最近10条）
	 */
	public List<BasicClient> existsClient(MongoTemplate mongoTemplate, @Valid @NotNull String appId) {

		Criteria criteria = Criteria.where(ClientMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		query.fields().include(
			ClientMongodb.FIELD.ID,
			ClientMongodb.FIELD.APP_ID,
			ClientMongodb.FIELD.ENDPOINT_ID,
			ClientMongodb.FIELD.CLIENT_ID,
			ClientMongodb.FIELD.CLIENT_NAME);
		query.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.LOGIN_TIME)));
		query.limit(10);

		List<ClientMongodb> clientMongodbList = mongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return clientMongodbList.stream().map(ClientConverter::convertBasicClient).collect(Collectors.toList());
	}

	/**
	 * 查询是否存在client
	 *
	 * @param appId         appId
	 * @param endpointId endpointId
	 * @return client集合（仅展示最近10条）
	 */
	public List<BasicClient> existsClient(MongoTemplate mongoTemplate, @Valid @NotNull String appId, @Valid @NotNull String endpointId) {

		Criteria criteria = Criteria
			.where(ClientMongodb.FIELD.APP_ID).is(appId)
			.and(ClientMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		Query query = Query.query(criteria);
		query.fields().include(
			ClientMongodb.FIELD.ID,
			ClientMongodb.FIELD.APP_ID,
			ClientMongodb.FIELD.ENDPOINT_ID,
			ClientMongodb.FIELD.CLIENT_ID,
			ClientMongodb.FIELD.CLIENT_NAME);
		query.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.LOGIN_TIME)));
		query.limit(10);

		List<ClientMongodb> clientMongodbList = mongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return clientMongodbList.stream().map(ClientConverter::convertBasicClient).collect(Collectors.toList());
	}

	public void checkClientId(MongoTemplate mongoTemplate, @Valid @NotNull String clientId) {
		Criteria criteria = Criteria
			.where(ClientMongodb.FIELD.CLIENT_ID).is(clientId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT)) {
			throw new ConflictBusinessException("clientId错误");
		}
	}

	public void checkClientId(MongoTemplate mongoTemplate, @Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String clientId) {
		Criteria criteria = Criteria
			.where(ClientMongodb.FIELD.APP_ID).is(appId)
			.and(ClientMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(ClientMongodb.FIELD.CLIENT_ID).is(clientId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT)) {
			throw new ConflictBusinessException("clientId错误");
		}
	}
}
