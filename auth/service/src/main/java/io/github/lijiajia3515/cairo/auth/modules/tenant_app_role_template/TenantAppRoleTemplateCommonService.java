package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role_template;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleTemplateMongodb;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class TenantAppRoleTemplateCommonService {
	private final MongoTemplate readMongoTemplate;

	public TenantAppRoleTemplateCommonService(MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	public List<TenantAppRoleTemplate> getTenantAppRoleTemplateList(String appId, Collection<String> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) return Collections.emptyList();

		Criteria criteria = Criteria
			.where(TenantAppRoleTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_ID).in(roleIds)
			.and(TenantAppRoleTemplateMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(TenantAppRoleTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		return readMongoTemplate.find(query, TenantAppRoleTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_TEMPLATE).stream()
			.map(TenantAppRoleTemplateConverter::convert).collect(Collectors.toList());
	}
}
