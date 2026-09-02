package io.github.lijiajia3515.cairo.auth.modules.tenant_app;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Slf4j
@Validated
@Component
public class TenantAppCommonService {

	public TenantAppCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
								  TransactionTemplate transactionTemplate,
								  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
	}

	public void checkTenantApp(MongoTemplate mongoTemplate, @Valid @NotNull String tenantId, @Valid @NotNull String appId) {
		Criteria criteria = Criteria
			.where(TenantAppMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		if (mongoTemplate.exists(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP)) {
			throw new ConflictBusinessException("tenantId错误");
		}
	}
}
