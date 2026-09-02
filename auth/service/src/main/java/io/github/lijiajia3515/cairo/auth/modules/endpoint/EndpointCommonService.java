package io.github.lijiajia3515.cairo.auth.modules.endpoint;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class EndpointCommonService {
	private final MongoTemplate readMongoTemplate;

	public EndpointCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * get app endpoint list by app endpoint ids
	 *
	 * @param endpointIds endpointIds
	 * @return app list
	 */
	@NewSpan
	public List<Endpoint> getEndpointListByEndpointIds(Collection<String> endpointIds) {
		if (endpointIds == null || endpointIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria.where(EndpointMongodb.FIELD.ENDPOINT_ID).in(endpointIds);

		Query query = Query.query(criteria);
		query.with(
			Sort.by(
				Sort.Order.asc(EndpointMongodb.FIELD.APP_ID),
				Sort.Order.asc(EndpointMongodb.FIELD.ENDPOINT_ID)
			)
		);

		List<EndpointMongodb> mongodbList = readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);

		return mongodbList.stream().map(EndpointConverter::convertBasicEndpoint).collect(Collectors.toList());
	}

	/**
	 * get app endpoint map by app endpoint ids
	 *
	 * @param endpointIds endpointIds
	 * @return app map
	 */
	@NewSpan
	public Map<String, Endpoint> getEndpointMapByEndpointIds(Collection<String> endpointIds) {
		return getEndpointListByEndpointIds(endpointIds).stream()
			.collect(Collectors.toMap(Endpoint::getEndpointId, x -> x, (x1, x2) -> x1));
	}

	public void checkEndpointId(MongoTemplate mongoTemplate, String appId, String endpointId) {
		Criteria criteria = Criteria
			.where(EndpointMongodb.FIELD.APP_ID).is(appId)
			.and(EndpointMongodb.FIELD.ENDPOINT_ID).is(endpointId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT)) {
			throw new ConflictBusinessException("endpointId错误");
		}
	}
}
