package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * [client/api] tenant department service
 */
@Slf4j
@Validated
@Component
public class TenantAppDepartmentClientApiService {

	private final MongoTemplate readMongoTemplate;

	private final TenantAppUserCommonService tenantAppUserCommonService;

	public TenantAppDepartmentClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											   TenantAppUserCommonService tenantAppUserCommonService) {
		this.readMongoTemplate = readMongoTemplate;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
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
		bizId = "department:get_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<TenantAppDepartment> getTenantAppDepartmentList(@Valid @NotNull String appId, @Valid @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		return getTenantAppDepartmentList(args.getTenantId(), appId, dms, args.getExtension());
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
		bizId = "department:get_department_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<TenantAppDepartment> getTenantAppDepartmentPageList(@Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<TenantAppDepartment> ds = getTenantAppDepartmentList(args.getTenantId(), appId, readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT), args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 查找
	 *
	 * @param appId    appId
	 * @param args     args
	 * @return 部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_path_tenant_app_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<PathTenantAppDepartment> getPathTenantAppDepartmentList( @Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);
		final List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		return getPathTenantAppDepartmentList(args.getTenantId(),appId, dms);
	}

	/**
	 * 构建查询条件
	 *
	 * @param appId 应用id
	 * @param args  查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(@Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(args.getTenantId())
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getDepartmentIds()).ifPresent(ids -> criteria.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(ids));
		Optional.ofNullable(args.getParentId()).ifPresent(parent -> criteria.and(TenantAppDepartmentMongodb.FIELD.PARENT_ID).is(parent));

		return criteria;
	}

	/**
	 * 查询
	 *
	 * @param tenantId           tenantId
	 * @param appId              appId
	 * @param lastDepartmentList 部门列表
	 * @return 1
	 */
	@NewSpan
	protected List<PathTenantAppDepartment> getPathTenantAppDepartmentList(String tenantId, String appId, List<TenantAppDepartmentMongodb> lastDepartmentList) {
		List<String> departmentIds = lastDepartmentList.stream().map(TenantAppDepartmentMongodb::getDepartmentId).distinct().collect(Collectors.toList());
		// 获取部门列表中的父级部门列表并从已有的数据中去除重复
		Set<TenantAppDepartmentMongodb> noSelectParentDepartmentList = lastDepartmentList.stream().filter(x -> !departmentIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<TenantAppDepartmentMongodb> parentDepartmentMongodbList = Optional.of(noSelectParentDepartmentList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentDepartmentCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).ne(CairoAuthConstants.ROOT_ID)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(TenantAppDepartmentMongodb.FIELD.LEFT_NO).lt(x.getLeftNo())
							.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).gt(x.getRightNo()))
						.collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentDepartmentCriteria);
				return readMongoTemplate.find(noParentMenuQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
			}).orElse(Collections.emptyList());

		List<TenantAppDepartmentMongodb> allDepartmentList = Stream.of(parentDepartmentMongodbList, lastDepartmentList)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingInt(TenantAppDepartmentMongodb::getLeftNo))
			.collect(Collectors.toList());

		return lastDepartmentList.stream()
			.map(d -> {
				List<TenantAppDepartmentMongodb> departmentMongodbList = new ArrayList<>();
				allDepartmentList.forEach(department -> {
					if (department.getLeftNo() <= d.getLeftNo() && department.getRightNo() >= d.getRightNo()) {
						departmentMongodbList.add(department);
					}
				});

				return PathTenantAppDepartment.builder()
					.departmentIds(departmentMongodbList.stream().map(TenantAppDepartmentMongodb::getDepartmentId).collect(Collectors.toList()))
					.departmentNames(departmentMongodbList.stream().map(TenantAppDepartmentMongodb::getDepartmentName).collect(Collectors.toList()))
					.depth(departmentMongodbList.size())
					.build();
			})
			.collect(Collectors.toList());
	}

	public List<TenantAppDepartment> getTenantAppDepartmentList(String tenantId, String appId, List<TenantAppDepartmentMongodb> ms, Map<String, String> extensionMap) {
		final TenantAppDepartmentExtension extension = Optional.of(extensionMap.getOrDefault(CairoAuthExtensionConstants.DEPARTMENT, TenantAppDepartmentExtension.ALL.name()))
			.map(TenantAppDepartmentExtension::valueOf)
			.orElse(TenantAppDepartmentExtension.ALL);


		return ms.stream().map(x -> TenantAppDepartmentConverter.convert(x, extension)).collect(Collectors.toList());
	}

	public List<TenantAppDepartment> getTenantAppSubDepartmentList(String appId, GetDepartmentArgs args) {
		if (args.getDepartmentIds() == null || args.getDepartmentIds().isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(args.getTenantId())
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(args.getDepartmentIds());

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.DEPTH),
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.LEFT_NO)
				)
			);

		List<TenantAppDepartmentMongodb> lastDepartmentList = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);


		// 利用左右值特性，查询出所有子节点
		List<TenantAppDepartmentMongodb> subDepartmentMongodbList = Optional.of(lastDepartmentList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentDepartmentCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(args.getTenantId())
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).ne(CairoAuthConstants.ROOT_ID)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(x.getLeftNo())
							.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).lte(x.getRightNo()))
						.collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentDepartmentCriteria);
				return readMongoTemplate.find(noParentMenuQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
			}).orElse(Collections.emptyList());



		return subDepartmentMongodbList.stream()
			.map(d -> {
				return TenantAppDepartment.builder()
					.tenantId(d.getTenantId())
					.appId(d.getAppId())
					.departmentId(d.getDepartmentId())
					.parentId(d.getParentId())
					.departmentName(d.getDepartmentName())
					.remark(d.getRemark())
					.build();
			})
			.collect(Collectors.toList());
	}
}
