package io.github.lijiajia3515.cairo.auth.api.open.tenant;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.GetTenantByTenantAliasNameArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.GetTenantByTenantNameArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.open.tenant.OpenTenant;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


/**
 * [open/api] tenant service
 */
@Slf4j
@Validated
@Component
public class TenantOpenApiService {
	private final MongoTemplate readMongoTemplate;

	public TenantOpenApiService(
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 根据企业名称获取企业信息
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant:get_tenant_by_tenant_name",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenTenant getTenantByTenantName(GetTenantByTenantNameArgs args) {
		Criteria criteria = Criteria.where(TenantMongodb.FIELD.TENANT_NAME).is(args.getTenantName());
		TenantMongodb tenant = readMongoTemplate.findOne(Query.query(criteria), TenantMongodb.class, MongodbConstants.Collection.TENANT);
		if (tenant==null) {
			throw new ConflictBusinessException("企业不存在");
		}
		return OpenTenant.builder()
			.tenantId(tenant.getTenantId())
			.tenantName(tenant.getTenantName())
			.aliasName(tenant.getAliasName())
			.icon(tenant.getIcon())
			.build();
	}

	/**
	 * 根据企业别名获取企业信息
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant:get_tenant_by_tenant_alias_name",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public OpenTenant getTenantByTenantAliasName(GetTenantByTenantAliasNameArgs args) {
		Criteria criteria = Criteria.where(TenantMongodb.FIELD.ALIAS_NAME).is(args.getTenantAliasName());
		TenantMongodb tenant = readMongoTemplate.findOne(Query.query(criteria), TenantMongodb.class, MongodbConstants.Collection.TENANT);
		if (tenant==null) {
			throw new ConflictBusinessException("企业不存在");
		}
		return OpenTenant.builder()
			.tenantId(tenant.getTenantId())
			.tenantName(tenant.getTenantName())
			.aliasName(tenant.getAliasName())
			.icon(tenant.getIcon())
			.build();
	}
}
