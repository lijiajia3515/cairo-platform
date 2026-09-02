package io.github.lijiajia3515.cairo.auth.api.client.client;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.Client;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.client.GetClientArgs;
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
import java.util.stream.Collectors;

/**
 * [client/api] client service
 */
@Slf4j
@Validated
@Component
public class ClientClientApiService {


	private final MongoTemplate readMongoTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;

	public ClientClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								  AppCommonService appCommonService,
								  EndpointCommonService endpointCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
	}


	/**
	 * 客户端列表
	 *
	 * @param args 参数
	 * @return 应用列表
	 */
	@NewSpan
	@BizLog(
		bizId = "client:get_basic_client_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<BasicClient> getBasicClientList(@Validated GetClientArgs args) {
		if (args.getClientIds().isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria.where(ClientMongodb.FIELD.CLIENT_ID).in(args.getClientIds());

		if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHENTICATION_TYPES).in(args.getAuthenticationTypes());
		}

		if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.ACCOUNT_SNS_PROVIDER_IDS).in(args.getAccountSnsProviderIds());
		}


		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(ClientMongodb.FIELD.CLIENT_ID)
				)
			);

		List<ClientMongodb> mongodbList = readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return mongodbList.stream().map(ClientConverter::convertBasicClient).collect(Collectors.toList());
	}

	/**
	 * 客户端查询
	 *
	 * @param args 1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "client:get_client_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Client> getClientList(GetClientArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<ClientMongodb> tms = readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return getClientList(tms);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetClientArgs args) {
		Criteria criteria = new Criteria();
		if (args.getAppId() != null) {
			criteria.and(ClientMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getClientIds() != null&& !args.getClientIds().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.CLIENT_ID).in(args.getClientIds());
		}

		if (args.getEndpointId() != null) {
			criteria.and(ClientMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getAuthorizationGrantTypes() != null && !args.getAuthorizationGrantTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHORIZATION_GRANT_TYPES).in(args.getAuthorizationGrantTypes());
		}

		if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHENTICATION_TYPES).in(args.getAuthenticationTypes());
		}

		if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.ACCOUNT_SNS_PROVIDER_IDS).in(args.getAccountSnsProviderIds());
		}


		if (args.getKeyword() != null) {
			criteria.and(ClientMongodb.FIELD.CLIENT_NAME).regex(args.getKeyword());
		}

		if (args.getEnabled() != null) {
			criteria.and(ClientMongodb.FIELD.ENABLED).is(args.getEnabled());
		}
		return criteria;
	}

	List<Client> getClientList(List<ClientMongodb> ms) {

		List<String> appIds = ms.stream().map(ClientMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = appCommonService.getAppMapByAppIds(appIds);

		List<String> endpointIds = ms.stream().map(ClientMongodb::getEndpointId).distinct().collect(Collectors.toList());
		Map<String, Endpoint> endpointMap = endpointCommonService.getEndpointMapByEndpointIds(endpointIds);

		return ms.stream().map(x -> ClientConverter.convertClient(x, appMap, endpointMap)).collect(Collectors.toList());
	}
}
