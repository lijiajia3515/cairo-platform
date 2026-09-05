package io.github.lijiajia3515.cairo.auth.api.client.tenant;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthRedisConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant.GetTenantInfoArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [client/api] client service
 */
@Slf4j
@Service
@Validated
public class TenantClientApiService {

	private final MongoTemplate readMongoTemplate;

	public TenantClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 企业查询
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant:get_tenant_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<Tenant> getTenantList(@Validated GetTenantArgs args) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(args.getEnabled()).ifPresent(enabled -> criteria.and(TenantMongodb.FIELD.ENABLED).is(enabled));

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(TenantMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<TenantMongodb> tms = readMongoTemplate.find(query, TenantMongodb.class, MongodbConstants.Collection.TENANT);
		return getTenantList(tms);
	}

	@Caching(
		cacheable = {
			@Cacheable(cacheNames = CairoAuthRedisConstants.Keys.TENANT, sync = true)
		}
	)
	@BizLog(
		bizId = "tenant:get_tenant_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Tenant getTenantInfo(@Validated GetTenantInfoArgs args) {
		Criteria criteria = Criteria.where(TenantMongodb.FIELD.TENANT_ID).is(args.getTenantId());
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantMongodb.FIELD.METADATA.UPDATE_TIME))
			);
		return Optional.ofNullable(readMongoTemplate.findOne(query, TenantMongodb.class, MongodbConstants.Collection.TENANT))
			.flatMap(x -> getTenantList(Collections.singletonList(x)).stream().findFirst())
			.orElse(null);
	}

	List<Tenant> getTenantList(List<TenantMongodb> ms) {
		return ms.stream().map(TenantConverter::convertTenant).collect(Collectors.toList());
	}

}
