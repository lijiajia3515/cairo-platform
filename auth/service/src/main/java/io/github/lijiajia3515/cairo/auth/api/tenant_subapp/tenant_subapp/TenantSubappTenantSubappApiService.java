package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_subapp;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.CairoAccountTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_subapp.MetadataTenantSubapp;
import io.github.lijiajia3515.cairo.auth.modules.tenant_subapp.TenantSubappConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_subapp.GetTenantSubappArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [tenant_subapp_user/api] tenant app subapp service
 */
@Slf4j
@Validated
@Component
public class TenantSubappTenantSubappApiService {

	private final MongoTemplate readMongoTemplate;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final AccountCommonService accountCommonService;
	private final EndpointCommonService endpointCommonService;
	private final SubappCommonService subappCommonService;

	public TenantSubappTenantSubappApiService(
												@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												TenantCommonService tenantCommonService,
												AppCommonService appCommonService,
												AccountCommonService accountCommonService,
												EndpointCommonService endpointCommonService,
												SubappCommonService subappCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.accountCommonService = accountCommonService;
		this.endpointCommonService = endpointCommonService;
		this.subappCommonService = subappCommonService;
	}


	/**
	 * 获取企业子应用列表
	 *
	 * @param appId appId
	 * @param args  1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_subapp:get_tenant_subapp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<MetadataTenantSubapp> getTenantSubappList(String tenantId,String appId, @Validated GetTenantSubappArgs args) {
		Criteria criteria = buildCriteria(tenantId,appId,args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(TenantSubappMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantSubappMongodb> tas = readMongoTemplate.find(query, TenantSubappMongodb.class, MongodbConstants.Collection.TENANT_SUBAPP);
		return getTenantSubappList(tas);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(String tenantId,String appId,GetTenantSubappArgs args) {

		Criteria subappCriteria = Criteria
			.where(SubappMongodb.FIELD.APP_ID).is(appId);
		if (args.getEndpointId() != null) {
			subappCriteria.and(SubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getEnabled() != null) {
			subappCriteria.and(SubappMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		Query subappQuery = Query.query(subappCriteria);
		subappQuery.fields().include(SubappMongodb.FIELD.SUBAPP_ID);
		List<SubappMongodb> mongodbList = readMongoTemplate.find(subappQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
		List<String> subappIds = mongodbList.stream().map(SubappMongodb::getSubappId).toList();

		Criteria criteria = Criteria.where(TenantSubappMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantSubappMongodb.FIELD.APP_ID).is(appId)
			.and(TenantSubappMongodb.FIELD.SUBAPP_ID).in(subappIds);
		Optional.ofNullable(args.getEndpointId()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.ENDPOINT_ID).is(x));
		Optional.ofNullable(args.getEnabled()).ifPresent(x -> criteria.and(TenantSubappMongodb.FIELD.ENABLED).is(x));
		return criteria;
	}

	List<MetadataTenantSubapp> getTenantSubappList(List<TenantSubappMongodb> tas) {
		List<String> tenantIds = tas.stream().map(TenantSubappMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());

		List<String> appIds = tas.stream().map(TenantSubappMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());


		List<String> endpointIds = tas.stream().map(TenantSubappMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());


		List<String> subappIds = tas.stream().map(TenantSubappMongodb::getSubappId).distinct().collect(Collectors.toList());
		Map<String, Subapp> subappMap = Optional.of(subappIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(subappCommonService::getSubappMapBySubappIds)
			.orElse(Collections.emptyMap());


		Set<String> metadataAccountIds = CairoAccountTool.getAccountMetadataAccountIds(tas.stream().map(TenantSubappMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, Account> metadataAccountMap = accountCommonService.getAccountMapByAccountIds(metadataAccountIds);

		return tas.stream().map(x -> TenantSubappConverter.convertTenantSubapp(x, tenantMap, appMap, endpointMap, subappMap, metadataAccountMap)).collect(Collectors.toList());
	}

}
