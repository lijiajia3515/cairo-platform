package io.github.lijiajia3515.cairo.auth.api.client.app;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * [client/api] app service
 */
@Slf4j
@Validated
@Component
public class AppClientApiService {


	private final MongoTemplate readMongoTemplate;


	public AppClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 应用列表
	 *
	 * @param args 参数
	 * @return 应用列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app:get_app_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<App> getAppList(@Validated GetAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<AppMongodb> tms = readMongoTemplate.find(query, AppMongodb.class, MongodbConstants.Collection.APP);
		return getAppList(tms);
	}

	/**
	 * 应用分页列表
	 *
	 * @return 应用 page
	 */
	@NewSpan
	@BizLog(
		bizId = "app:get_app_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<App> getAppPageList(@Validated GetAppArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = readMongoTemplate.count(query, AppMongodb.class, MongodbConstants.Collection.APP);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.desc(AppMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<App> ds = getAppList(readMongoTemplate.find(query, AppMongodb.class, MongodbConstants.Collection.APP));

		return new Page<>(args, ds, total);
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetAppArgs args) {
		Criteria criteria = new Criteria();

		Optional.ofNullable(args.getAppIds()).filter(appId -> !appId.isEmpty()).ifPresent(appIds -> criteria.and(AppMongodb.FIELD.APP_ID).in(appIds));
		Optional.ofNullable(args.getKeyword()).ifPresent(name -> criteria.and(AppMongodb.FIELD.APP_NAME).regex(name));
		Optional.ofNullable(args.getEnabled()).ifPresent(enabled -> criteria.and(AppMongodb.FIELD.ENABLED).is(enabled));

		return criteria;
	}

	List<App> getAppList(List<AppMongodb> ms) {
		return ms.stream().map(AppConverter::convertApp).collect(Collectors.toList());
	}

}
