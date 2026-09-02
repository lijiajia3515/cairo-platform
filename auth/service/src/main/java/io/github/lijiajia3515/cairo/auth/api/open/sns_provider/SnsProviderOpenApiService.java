package io.github.lijiajia3515.cairo.auth.api.open.sns_provider;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderPartnerProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderTypeProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProviderProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
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
 * [open/api] sns provider service
 */
@Slf4j
@Validated
@Component
public class SnsProviderOpenApiService {

	private final MongoTemplate readMongoTemplate;
	private final SnsProviderProperties snsProviderProperties;
	private final AppCommonService appCommonService;


	public SnsProviderOpenApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									 SnsProviderProperties snsProviderProperties,
									 AppCommonService appCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.snsProviderProperties = snsProviderProperties;
		this.appCommonService = appCommonService;
	}

	/**
	 * 获取元素第三方认证提供方 集合模式
	 *
	 * @param args 参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "sns_provider:get_sns_provider_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<SnsProvider> getSnsProviderList(@Validated GetSnsProviderArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query.query(criteria)
			.with(Sort.by(Sort.Order.desc(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME)));
		List<SnsProviderMongodb> list = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		return getSnsProviderList(list);
	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetSnsProviderArgs args) {
		Criteria criteria = new Criteria();

		if (args.getAppId() != null && !args.getAppId().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getSnsProviderIds() != null && !args.getSnsProviderIds().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).in(args.getSnsProviderIds());
		}

		if (args.getSnsTypes() != null&& !args.getSnsTypes().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).in(args.getSnsTypes());
		}

		if (args.getSnsPartners() != null&& !args.getSnsPartners().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER).in(args.getSnsPartners());
		}

		if (args.getEnabled() != null) {
			criteria.and(SnsProviderMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_NAME).regex(args.getKeyword());
		}

		return criteria;
	}


	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return cairo snsProvider list
	 */
	List<SnsProvider> getSnsProviderList(List<SnsProviderMongodb> ms) {
		List<String> appIds = ms.stream().map(SnsProviderMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		Map<String, ProviderTypeProperties> snsTypeMap = snsProviderProperties.getProviderTypes().stream()
			.collect(Collectors.toMap(ProviderTypeProperties::getId, x -> x, (x1, x2) -> x1));

		Map<String, ProviderPartnerProperties> snsPartnerMap = snsProviderProperties.getProviderPartners().stream()
			.collect(Collectors.toMap(ProviderPartnerProperties::getId, x -> x, (x1, x2) -> x1));

		return ms.stream().map(x -> SnsProviderConverter.convertSnsProvider(x,appMap,snsTypeMap, snsPartnerMap)).collect(Collectors.toList());
	}

}
