package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_user_template;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template.TenantAppDepartmentTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role_template.TenantAppRoleTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role_template.TenantAppRoleTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_template.TenantAppUserTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user_template.TenantAppUserTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_template.GetTenantAppUserTemplateListArgs;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


/**
 * [client/api] tenant_app_user_template service
 */
@Slf4j
@Validated
@Component
public class TenantAppUserTemplateClientApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantAppDepartmentTemplateCommonService tenantDepartmentTemplateCommonService;
	private final TenantAppRoleTemplateCommonService tenantAppRoleTemplateCommonService;
	private final AppUserCommonService appUserCommonService;

	public TenantAppUserTemplateClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
                                                 TenantAppDepartmentTemplateCommonService tenantDepartmentTemplateCommonService,
                                                 TenantAppRoleTemplateCommonService tenantAppRoleTemplateCommonService,
                                                 AppUserCommonService appUserCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantDepartmentTemplateCommonService = tenantDepartmentTemplateCommonService;
		this.tenantAppRoleTemplateCommonService = tenantAppRoleTemplateCommonService;
		this.appUserCommonService = appUserCommonService;
	}


	/**
	 * getUserList
	 *
	 * @param appId appId
	 * @param args  args
	 * @return user list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user_template:get_tenant_app_user_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	List<TenantAppUserTemplate> getTenantAppUserTemplateList(@Valid @NotNull String appId, @Validated GetTenantAppUserTemplateListArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(TenantAppUserTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		List<TenantAppUserTemplateMongodb> users = readMongoTemplate.find(query, TenantAppUserTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_TEMPLATE);

		return getTenantAppUserTemplateList(readMongoTemplate, appId, users, args.getExtension());
	}


	Criteria buildCriteria(String appId, GetTenantAppUserTemplateListArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppUserTemplateMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getKeyword()).filter(x -> !x.isEmpty())
			.map(x -> new Criteria[]{
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.NICKNAME).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.PHONE_NUMBER).regex(x),
				Criteria.where(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).regex(x),
			}).ifPresent(criteria::orOperator);

		if (args.getAccountIds() != null && !args.getAccountIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.ACCOUNT_ID).in(args.getAccountIds());
		}

		if (args.getTenantAppUserTemplateIds() != null && !args.getTenantAppUserTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_USER_TEMPLATE_ID).in(args.getTenantAppUserTemplateIds());
		}

		if (args.getTenantAppDepartmentTemplateIds() != null && !args.getTenantAppDepartmentTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_IDS).in(args.getTenantAppDepartmentTemplateIds());
		}

		if (args.getTenantAppRoleTemplateIds() != null && !args.getTenantAppRoleTemplateIds().isEmpty()) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.TENANT_APP_ROLE_TEMPLATE_IDS).in(args.getTenantAppRoleTemplateIds());
		}

		if (args.getEnabled() != null) {
			criteria.and(TenantAppUserTemplateMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		return criteria;
	}

	@NewSpan
	protected List<TenantAppUserTemplate> getTenantAppUserTemplateList(MongoTemplate template, String appId, List<TenantAppUserTemplateMongodb> ms, Map<String, String> extensionMap) {
		List<String> accountIds = ms.stream().map(TenantAppUserTemplateMongodb::getAccountId).distinct().collect(Collectors.toList());
		Map<String, AccountMongodb> accountMap = Optional.of(accountIds).filter(x -> !x.isEmpty())
			.map(ids -> {
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);
				Query accountQuery = Query.query(accountCriteria);
				return template.find(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT).stream().collect(toMap(AccountMongodb::getAccountId, x -> x));
			}).orElse(Collections.emptyMap());

		TenantAppUserTemplateExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.TENANT_APP_ROLE_TEMPLATE)).map(TenantAppUserTemplateExtension::valueOf).orElse(TenantAppUserTemplateExtension.ALL);

		Map<String, TenantAppRoleTemplate> roleMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserTemplateField.ROLE))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getTenantAppRoleTemplateIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantAppRoleTemplateCommonService.getTenantAppRoleTemplateList(appId, x).stream().collect(Collectors.toMap(TenantAppRoleTemplate::getTenantAppRoleTemplateId, z -> z)))
			.orElse(Collections.emptyMap());

		Map<String, PathTenantAppDepartmentTemplate> departmentMap = Optional.of(extension.fields())
			.filter(x -> x.contains(TenantAppUserTemplateField.DEPARTMENT))
			.map(x -> ms.stream().flatMap(m -> Optional.ofNullable(m.getTenantAppDepartmentTemplateIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toSet()))
			.map(x -> tenantDepartmentTemplateCommonService.getPathTenantAppDepartmentTemplateMap(appId, x))
			.orElse(Collections.emptyMap());


		return ms.stream().map(m -> TenantAppUserTemplateConverter.convertTenantAppUserTemplate(
				m,
				roleMap,
				departmentMap,
				accountMap,
				extension)
			)
			.collect(Collectors.toList());
	}

}
