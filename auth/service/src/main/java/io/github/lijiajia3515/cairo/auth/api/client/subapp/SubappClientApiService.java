package io.github.lijiajia3515.cairo.auth.api.client.subapp;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp.GetSubappClientArgs;
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

/**
 * [client/api] subapp service
 */
@Slf4j
@Validated
@Component
public class SubappClientApiService {

	private final MongoTemplate readMongoTemplate;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final AppUserCommonService appUserCommonService;

	public SubappClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								   CairoSecurityProperties cairoSecurityProperties,
								   AppCommonService appCommonService,
								   EndpointCommonService endpointCommonService,
								   AppUserCommonService appUserCommonService) {
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.appUserCommonService = appUserCommonService;
	}


	/**
	 * 获取子应用集合
	 *
	 * @param args 参数
	 * @return 端点集合
	 */
	@NewSpan
	@BizLog(
		bizId = "subapp:get_subapp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Subapp> getSubappList(GetSubappClientArgs args) {
		return getSubappList(args.getAppId(), args);
	}

	List<Subapp> getSubappList(@Valid @NotNull String appId, @Validated GetSubappClientArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(SubappMongodb.FIELD.APP_ID),
					Sort.Order.asc(SubappMongodb.FIELD.ENDPOINT_ID),
					Sort.Order.asc(SubappMongodb.FIELD.SORT)
				)
			);

		List<SubappMongodb> tms = readMongoTemplate.find(query, SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
		return getSubappList(tms);
	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(@Valid @NotNull String appId, GetSubappClientArgs args) {
		Criteria criteria = new Criteria();
		criteria.and(SubappMongodb.FIELD.APP_ID).is(args.getAppId());

		if (args.getEndpointId() != null && !args.getEndpointId().isEmpty()) {
			criteria.and(SubappMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SubappMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null) {
			criteria.and(SubappMongodb.FIELD.SUBAPP_NAME).regex(args.getKeyword());
		}

		return criteria;
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return subapp list
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
