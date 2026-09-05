package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.client;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.client.BasicClient;
import io.github.lijiajia3515.cairo.auth.modules.client.ClientConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.client.GetCurrentAppClientArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [tenant_app_user/api] client service
 */
@Slf4j
@Validated
@Component
public class ClientTenantAppUserApiService {

	private final MongoTemplate readMongoTemplate;

	public ClientTenantAppUserApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 获取当前登录系统中的客户端列表
	 *
	 * @param appId         应用ID
	 * @param args          参数
	 * @return 客户端列表
	 */
	@NewSpan
	@BizLog(
		bizId = "client:get_current_client_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<BasicClient> getCurrentAppClientList(@Valid @NotNull String appId, @Validated GetCurrentAppClientArgs args) {
		Criteria criteria = Criteria
			.where(ClientMongodb.FIELD.APP_ID).is(appId);

		if (args.getEndpointId() != null && !args.getEndpointId().isBlank()) {
			criteria.and(ClientMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId());
		}

		if (args.getAuthenticationTypes() != null && !args.getAuthenticationTypes().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.AUTHENTICATION_TYPES).in(args.getAuthenticationTypes());
		}

		if (args.getAccountSnsProviderIds() != null && !args.getAccountSnsProviderIds().isEmpty()) {
			criteria.and(ClientMongodb.FIELD.ACCOUNT_SNS_PROVIDER_IDS).in(args.getAccountSnsProviderIds());
		}

		criteria.and(ClientMongodb.FIELD.ENABLED).is(true);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.desc(ClientMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<ClientMongodb> tms = readMongoTemplate.find(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
		return tms.stream().map(ClientConverter::convertBasicClient).collect(Collectors.toList());
	}

}
