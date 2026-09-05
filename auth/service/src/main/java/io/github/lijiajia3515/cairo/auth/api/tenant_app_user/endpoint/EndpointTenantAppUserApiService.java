package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.endpoint;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.endpoint.GetEndpointArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [tenant_endpoint/api] app endpoint service
 */
@Slf4j
@Validated
@Component
public class EndpointTenantAppUserApiService {

	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;

	public EndpointTenantAppUserApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, TransactionTemplate transactionTemplate,
												  AppCommonService appCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
	}

	/**
	 * 端点查询
	 *
	 * @return 端点查询
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:get_current_endpoint_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Endpoint> getEndpointList(@Valid @NotNull String appId, @Validated GetEndpointArgs args) {
		Criteria endpointCriteria = Criteria.where(EndpointMongodb.FIELD.APP_ID).is(appId);
		if (args.getTypeIds() != null) {
			endpointCriteria.and(EndpointMongodb.FIELD.TYPE).in(args.getTypeIds());
		}

		if (args.getScopeIds() != null) {
			endpointCriteria.and(EndpointMongodb.FIELD.SCOPE).in(args.getScopeIds());
		}

		Query endpointQuery = Query.query(endpointCriteria);
		List<EndpointMongodb> mongodbList = readMongoTemplate.find(endpointQuery, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
		return getEndpointList(mongodbList);
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return cairo user endpoint list
	 */
	List<Endpoint> getEndpointList(List<EndpointMongodb> ms) {
		List<String> appIds = ms.stream().map(EndpointMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> EndpointConverter.convertEndpoint(x, appMap)).collect(Collectors.toList());
	}
}
