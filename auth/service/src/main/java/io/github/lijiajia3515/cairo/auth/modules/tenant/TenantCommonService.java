package io.github.lijiajia3515.cairo.auth.modules.tenant;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.core.CoreConstants;
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
public class TenantCommonService {
	private final MongoTemplate readMongoTemplate;

	public TenantCommonService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	public static String getNewTenantId() {
		return "t" + CoreConstants.nextIdStr();
	}

	/**
	 * get tenant list by tenant ids
	 *
	 * @param tenantIds tenantIds
	 * @return tenant list
	 */
	@NewSpan
	public List<Tenant> getBasicTenantListByTenantId(Collection<String> tenantIds) {
		if (tenantIds == null || tenantIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria.where(TenantMongodb.FIELD.TENANT_ID).in(tenantIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(TenantMongodb.FIELD.TENANT_ID)));

		List<TenantMongodb> tenantMongodbList = readMongoTemplate.find(query, TenantMongodb.class, MongodbConstants.Collection.TENANT);

		return tenantMongodbList.stream().map(TenantConverter::convertBasicTenant).collect(Collectors.toList());
	}

	@NewSpan
	public List<Tenant> getTenantListByTenantId(Collection<String> tenantIds) {
		if (tenantIds == null || tenantIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(TenantMongodb.FIELD.TENANT_ID).in(tenantIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(TenantMongodb.FIELD.TENANT_ID)));

		List<TenantMongodb> tenantMongodbList = readMongoTemplate.find(query, TenantMongodb.class, MongodbConstants.Collection.TENANT);

		return tenantMongodbList.stream().map(TenantConverter::convertTenant).collect(Collectors.toList());
	}

	/**
	 * get tenant map by tenantIds
	 *
	 * @param tenantIds tenantIds
	 * @return app map
	 */
	@NewSpan
	public Map<String, Tenant> getBasicTenantMapByTenantIds(Collection<String> tenantIds) {
		return getBasicTenantListByTenantId(tenantIds).stream()
			.collect(Collectors.toMap(Tenant::getTenantId, x -> x, (x1, x2) -> x1));
	}

	/**
	 * get tenant map by tenantIds
	 *
	 * @param tenantIds tenantIds
	 * @return app map
	 */
	@NewSpan
	public Map<String, Tenant> getTenantMapByTenantIds(Collection<String> tenantIds) {
		return getTenantListByTenantId(tenantIds).stream()
			.collect(Collectors.toMap(Tenant::getTenantId, x -> x, (x1, x2) -> x1));
	}

	public void checkTenantId(MongoTemplate mongoTemplate, String tenantId) {
		Criteria criteria = Criteria
			.where(TenantMongodb.FIELD.TENANT_ID).is(tenantId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, TenantMongodb.class, MongodbConstants.Collection.TENANT)) {
			throw new ConflictBusinessException("tenantId错误");
		}
	}
}
