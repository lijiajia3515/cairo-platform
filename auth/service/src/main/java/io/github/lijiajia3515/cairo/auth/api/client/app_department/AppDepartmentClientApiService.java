package io.github.lijiajia3515.cairo.auth.api.client.app_department;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.page.Page;
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


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * [client/api] app_department service
 */
@Slf4j
@Validated
@Component
public class AppDepartmentClientApiService {

	private final MongoTemplate readMongoTemplate;

	public AppDepartmentClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 部门查询
	 *
	 * @param appId 1
	 * @param args  1
	 * @return 1
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:get_app_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<AppDepartment> getAppDepartmentList(@Valid @NotNull String appId, @Valid @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<AppDepartmentMongodb> dms = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		return getAppDepartmentList(appId, dms, args.getExtension());
	}

	/**
	 * 查找
	 *
	 * @param appId 1
	 * @param args  1
	 * @return 部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:get_app_department_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<AppDepartment> getAppDepartmentPageList(@Valid @NotNull String appId, @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<AppDepartment> ds = getAppDepartmentList(appId, readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT), args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 构建查询条件
	 *
	 * @param appId 应用id
	 * @param args  查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(@Valid @NotNull String appId, @Validated GetAppDepartmentArgs args) {
		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getDepartmentIds()).ifPresent(ids -> criteria.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(ids));
		Optional.ofNullable(args.getParentId()).ifPresent(parent -> criteria.and(AppDepartmentMongodb.FIELD.PARENT_ID).is(parent));

		return criteria;
	}

	public List<AppDepartment> getAppDepartmentList(String appId, List<AppDepartmentMongodb> ms, Map<String, String> extensionMap) {
		final AppDepartmentExtension extension = Optional.of(extensionMap.getOrDefault(CairoAuthExtensionConstants.DEPARTMENT, AppDepartmentExtension.ALL.name()))
			.map(AppDepartmentExtension::valueOf)
			.orElse(AppDepartmentExtension.ALL);


		return ms.stream().map(x -> AppDepartmentConverter.convert(x, extension)).collect(Collectors.toList());
	}

}
