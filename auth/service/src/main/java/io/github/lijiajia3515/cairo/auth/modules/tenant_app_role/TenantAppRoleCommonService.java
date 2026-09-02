package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserConverter;
import lombok.extern.slf4j.Slf4j;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class TenantAppRoleCommonService {
	private final MongoTemplate readMongoTemplate;

	public TenantAppRoleCommonService(MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	public List<TenantAppRole> getRoleList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) return Collections.emptyList();

		Criteria criteria = Criteria
			.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleMongodb.FIELD.ROLE_ID).in(roleIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));

		return readMongoTemplate.find(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE).stream()
			.map(TenantAppRoleConverter::convert).collect(Collectors.toList());
	}

	@NewSpan
	public List<BasicTenantAppUser> existsUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> roleIds) {
		final Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.ROLE_IDS).in(roleIds);
		Query query = Query.query(criteria);
		query.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.NICKNAME);
		query.limit(10);
		return readMongoTemplate.find(Query.query(criteria), TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER).stream()
			.map(TenantAppUserConverter::convertMetadataUser).collect(Collectors.toList());
	}
}
