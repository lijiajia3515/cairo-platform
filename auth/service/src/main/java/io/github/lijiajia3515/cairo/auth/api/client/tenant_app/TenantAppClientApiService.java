package io.github.lijiajia3515.cairo.auth.api.client.tenant_app;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app.TenantAppConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
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

/**
 * [client/api] tenant service
 */
@Slf4j
@Validated
@Component
public class TenantAppClientApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final AccountCommonService accountCommonService;

	public TenantAppClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									 TenantCommonService tenantCommonService,
									 AppCommonService appCommonService,
									 AccountCommonService accountCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.accountCommonService = accountCommonService;
	}


	/**
	 * 获取企业应用列表
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app:get_tenant_app_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<TenantApp> getTenantAppList(@Validated GetTenantAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppMongodb> tas = readMongoTemplate.find(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
		return getTenantAppList(tas);
	}

	/**
	 * 获取企业应用分页列表
	 *
	 * @return 企业查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app:get_tenant_app_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<TenantApp> getTenantAppPageList(@Validated GetTenantAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantMongodb.class, MongodbConstants.Collection.TENANT_APP);
		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(TenantAppMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppMongodb> tenantAppMongodbList = readMongoTemplate.find(query, TenantAppMongodb.class, MongodbConstants.Collection.TENANT_APP);
		List<TenantApp> tenantAppList = getTenantAppList(tenantAppMongodbList);

		return new Page<>(args, tenantAppList, total);
	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetTenantAppArgs args) {
		Criteria criteria = new Criteria();

		if (args.getTenantId() != null) {
			criteria.and(TenantAppMongodb.FIELD.TENANT_ID).is(args.getTenantId());
		}

		if (args.getAppId() != null) {
			criteria.and(TenantAppMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getEnabled() != null) {
			criteria.and(TenantAppMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		return criteria;
	}

	List<TenantApp> getTenantAppList(List<TenantAppMongodb> tas) {
		List<String> appIds = tas.stream().map(TenantAppMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> tenantIds = tas.stream().map(TenantAppMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());


		List<String> adminAccountIds = tas.stream().flatMap(x -> Optional.ofNullable(x.getAdminAccountIds()).orElse(Collections.emptyList()).stream()).collect(Collectors.toList());
		Map<String, Account> adminAccountMap = accountCommonService.getAccountMapByAccountIds(adminAccountIds);

		return tas.stream().map(x -> TenantAppConverter.convertTenantApp(x, tenantMap, appMap, adminAccountMap)).collect(Collectors.toList());
	}

}
