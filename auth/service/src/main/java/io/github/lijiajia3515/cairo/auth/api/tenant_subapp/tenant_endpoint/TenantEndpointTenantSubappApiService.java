package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_endpoint;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.endpoint.GetCurrentEndpointArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant.Tenant;
import io.github.lijiajia3515.cairo.auth.modules.tenant.TenantCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_endpoint.TenantEndpoint;
import io.github.lijiajia3515.cairo.auth.modules.tenant_endpoint.TenantEndpointConverter;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * [tenant_subapp_user/api] tenant app endpoint service
 */
@Slf4j
@Validated
@Component
public class TenantEndpointTenantSubappApiService {
	private final MongoTemplate readMongoTemplate;
	private final TenantCommonService tenantCommonService;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;

	public TenantEndpointTenantSubappApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
													 TenantCommonService tenantCommonService,
													 AppCommonService appCommonService,
													 EndpointCommonService endpointCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantCommonService = tenantCommonService;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
	}

	/**
	 * 端点查询
	 *
	 * @return 端点查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_endpoint:get_current_endpoint_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<TenantEndpoint> getCurrentTenantEndpointList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetCurrentEndpointArgs args) {
		Criteria endpointCriteria = Criteria
			.where(EndpointMongodb.FIELD.APP_ID).is(appId);
		if (args.getTypeIds() != null) {
			endpointCriteria.and(EndpointMongodb.FIELD.TYPE).in(args.getTypeIds());
		}

		if (args.getScopeIds() != null) {
			endpointCriteria.and(EndpointMongodb.FIELD.SCOPE).in(args.getScopeIds());
		}

		Query endpointQuery = Query.query(endpointCriteria);
		endpointQuery.fields().include(EndpointMongodb.FIELD.ENDPOINT_ID);
		List<EndpointMongodb> mongodbList = readMongoTemplate.find(endpointQuery, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
		List<String> endpointIds = mongodbList.stream().map(EndpointMongodb::getEndpointId).collect(Collectors.toList());
		if (endpointIds.isEmpty()) return Collections.emptyList();

		Criteria tenantEndpointCriteria = Criteria
			.where(TenantEndpointMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantEndpointMongodb.FIELD.APP_ID).is(appId)
			.and(TenantEndpointMongodb.FIELD.ENDPOINT_ID).in(endpointIds)
			;

		Query tenantTenantEndpointQuery = Query.query(tenantEndpointCriteria);

		List<TenantEndpointMongodb> tenantEndpointMongodbList = readMongoTemplate.find(tenantTenantEndpointQuery, TenantEndpointMongodb.class, MongodbConstants.Collection.TENANT_ENDPOINT);
		return getTenantEndpointList(tenantEndpointMongodbList);
	}

	List<TenantEndpoint> getTenantEndpointList(List<TenantEndpointMongodb> tas) {
		List<String> tenantIds = tas.stream().map(TenantEndpointMongodb::getTenantId).distinct().collect(Collectors.toList());
		Map<String, Tenant> tenantMap = Optional.of(tenantIds)
			.filter(innerTenantIds -> !innerTenantIds.isEmpty())
			.map(tenantCommonService::getBasicTenantMapByTenantIds)
			.orElse(Collections.emptyMap());

		List<String> appIds = tas.stream().map(TenantEndpointMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());


		List<String> endpointIds = tas.stream().map(TenantEndpointMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());


		return tas.stream().map(x -> TenantEndpointConverter.convertTenantEndpoint(x, tenantMap, appMap, endpointMap)).collect(Collectors.toList());
	}

}
