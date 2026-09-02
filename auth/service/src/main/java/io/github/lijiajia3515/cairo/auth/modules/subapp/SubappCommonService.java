package io.github.lijiajia3515.cairo.auth.modules.subapp;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class SubappCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	public SubappCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
								TransactionTemplate transactionTemplate,
								@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * get subapp list by app endpoint ids
	 *
	 * @param subappIds subappIds
	 * @return app list
	 */
	@NewSpan
	public List<Subapp> getSubappListBySubappIds(Collection<String> subappIds) {
		if (subappIds == null || subappIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria.where(SubappMongodb.FIELD.SUBAPP_ID).in(subappIds);

		Query query = Query.query(criteria);
		query.with(
			Sort.by(
				Sort.Order.asc(SubappMongodb.FIELD.APP_ID),
				Sort.Order.asc(SubappMongodb.FIELD.ENDPOINT_ID),
				Sort.Order.asc(SubappMongodb.FIELD.SUBAPP_ID)
			)
		);

		List<SubappMongodb> mongodbList = readMongoTemplate.find(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);

		return mongodbList.stream().map(SubappConverter::convertBasicSubapp).collect(Collectors.toList());
	}

	/**
	 * get subapp map by subapp ids
	 *
	 * @param subappIds subappIds
	 * @return app map
	 */
	@NewSpan
	public Map<String, Subapp> getSubappMapBySubappIds(Collection<String> subappIds) {
		return getSubappListBySubappIds(subappIds).stream()
			.collect(Collectors.toMap(Subapp::getSubappId, x -> x, (x1, x2) -> x1));
	}

	public void checkSubappId(MongoTemplate mongoTemplate,String appId, String endpointId, String subappId) {
		Criteria criteria = Criteria.where(SubappMongodb.FIELD.APP_ID).is(appId)
			.and(SubappMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(SubappMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP)) {
			throw new ConflictBusinessException("subappId错误");
		}
	}
}
