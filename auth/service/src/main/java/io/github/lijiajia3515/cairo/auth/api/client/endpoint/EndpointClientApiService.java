package io.github.lijiajia3515.cairo.auth.api.client.endpoint;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointClientArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import io.micrometer.tracing.annotation.NewSpan;
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
 * [client/api] app endpoint service
 */
@Slf4j
@Validated
@Component
public class EndpointClientApiService {

	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;

	public EndpointClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, AppCommonService appCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
	}

	/**
	 * 终端列表
	 *
	 * @param args 参数
	 * @return 终端列表
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:get_endpoint_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Endpoint> getEndpointList(@Validated GetEndpointClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<EndpointMongodb> tms = readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
		return getEndpointList(tms);
	}

	/**
	 * 终端分页列表
	 *
	 * @return 终端分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "endpoint:get_endpoint_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<Endpoint> getEndpointPageList(@Validated GetEndpointClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = readMongoTemplate.count(query, EndpointMongodb.class, MongodbConstants.Collection.APP);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.desc(EndpointMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<Endpoint> ds = getEndpointList(readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT));

		return new Page<>(args, ds, total);
	}

	public List<Endpoint> getEndpointByAppList(GetEndpointByAppClientArgs args) {
		if (args.getEndpointInfos() == null || args.getEndpointInfos().isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = new Criteria();
		List<Criteria> criteriaList = args.getEndpointInfos().stream().map(x -> {
			return Criteria.where(EndpointMongodb.FIELD.APP_ID).is(x.getAppId()).and(EndpointMongodb.FIELD.ENDPOINT_ID).is(x.getEndpointId());
		}).collect(Collectors.toList());

		criteria.orOperator(criteriaList);
		Query query = Query.query(criteria);
		List<EndpointMongodb> mongodbList = readMongoTemplate.find(query, EndpointMongodb.class, MongodbConstants.Collection.ENDPOINT);
		return getEndpointList(mongodbList);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetEndpointClientArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getKeyword()).ifPresent(name -> criteria.and(EndpointMongodb.FIELD.ENDPOINT_NAME).regex(name));
		Optional.ofNullable(args.getEnabled()).ifPresent(enabled -> criteria.and(EndpointMongodb.FIELD.ENABLED).is(enabled));

		return criteria;
	}

	List<Endpoint> getEndpointList(List<EndpointMongodb> ms) {
		List<String> appIds = ms.stream().map(EndpointMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> EndpointConverter.convertEndpoint(x, appMap)).collect(Collectors.toList());
	}

}
