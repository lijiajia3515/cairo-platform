package io.github.lijiajia3515.cairo.auth.modules.app;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Validated
@Component
public class AppCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	public AppCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
							TransactionTemplate transactionTemplate,
							@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	public void checkAppId(MongoTemplate mongoTemplate, String appId) {
		Criteria criteria = Criteria.where(AppMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		if (!mongoTemplate.exists(query, AppMongodb.class, MongodbConstants.Collection.APP)) {
			throw new ConflictBusinessException("appId错误");
		}
	}

	/**
	 * 检查 范围是否在应用允许范围内
	 *
	 * @param mongoTemplate mongoTemplate
	 * @param appId         应用ID
	 * @param scope         范围
	 */
	public void checkAppScope(MongoTemplate mongoTemplate, String appId, String scope) {
		Query query = Query.query(Criteria.where(AppMongodb.FIELD.APP_ID).is(appId));
		query.fields().include(AppMongodb.FIELD.SCOPES);
		AppMongodb app = mongoTemplate.findOne(query, AppMongodb.class, MongodbConstants.Collection.APP);
		if (app == null || app.getScopes() == null || !app.getScopes().contains(scope)) {
			throw new ParamsErrorBusinessException(String.format("参数: 范围：%s 超出应用允许范围", scope));
		}
	}

	/**
	 * get app list by app ids
	 *
	 * @param appIds appIds
	 * @return app list
	 */
	@NewSpan
	public List<App> getAppListByAppIds(Collection<String> appIds) {
		if (appIds == null || appIds.isEmpty()) {
			return Collections.emptyList();
		}
		Criteria criteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).in(appIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AppMongodb.FIELD.APP_ID)));

		List<AppMongodb> appList = readMongoTemplate.find(query, AppMongodb.class, MongodbConstants.Collection.APP);


		return appList.stream().map(this::convert).collect(Collectors.toList());
	}

	/**
	 * get app map by appIds
	 *
	 * @param appIds appIds
	 * @return app map
	 */
	@NewSpan
	public Map<String, App> getAppMapByAppIds(Collection<String> appIds) {
		return getAppListByAppIds(appIds).stream()
			.collect(Collectors.toMap(App::getAppId, x -> x, (x1, x2) -> x1));
	}

	public App convert(AppMongodb appMongodb) {
		return App.builder()
			.appId(appMongodb.getAppId())
			.appName(appMongodb.getAppName())
			.icon(appMongodb.getIcon())
			.build();
	}
}
