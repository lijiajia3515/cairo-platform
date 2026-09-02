package io.github.lijiajia3515.cairo.auth.api.endpoint.subapp;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappConverter;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.subapp.GetSubappArgs;
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
 * [subapp_user/api] app endpoint service
 */
@Slf4j
@Validated
@Component
public class SubappEndpointApiService {

	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;

	public SubappEndpointApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										AppCommonService appCommonService,
										EndpointCommonService endpointCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
	}

	/**
	 * 子应用查询
	 *
	 * @return 子应用查询
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp:get_subapp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Subapp> getSubappList(@Valid @NotNull String appId, @Validated GetSubappArgs args) {
		Criteria criteria = new Criteria();

		if (appId != null && !appId.isBlank()) {
			criteria.and(SubappMongodb.FIELD.APP_ID).is(appId);
		}

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(SubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(SubappMongodb.FIELD.SUBAPP_NAME).regex(args.getKeyword());
		}

		Query subappQuery = Query.query(criteria);
		List<SubappMongodb> mongodbList = readMongoTemplate.find(subappQuery, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
		return getSubappList(mongodbList);
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return  subapp list
	 */
	List<Subapp> getSubappList(List<SubappMongodb> ms) {
		List<String> appIds = ms.stream().map(SubappMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		List<String> endpointIds = ms.stream().map(SubappMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = Optional.of(endpointIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(endpointCommonService::getEndpointMapByEndpointIds)
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> SubappConverter.convertSubapp(x, appMap, endpointMap)).collect(Collectors.toList());
	}
}
