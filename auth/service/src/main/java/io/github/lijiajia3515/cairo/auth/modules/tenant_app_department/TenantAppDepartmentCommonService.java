package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserConverter;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

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

@Slf4j
@Validated
@Component
public class TenantAppDepartmentCommonService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;

	public TenantAppDepartmentCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											TransactionTemplate transactionTemplate,
											@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 根据部门id查询部门信息
	 *
	 * @param tenantId      tenantId
	 * @param appId         appId
	 * @param departmentIds departmentIds
	 * @return 部门集合
	 */
	@NewSpan
	public List<TenantAppDepartment> getDepartmentList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(departmentIds);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		return dms.stream().map(x -> TenantAppDepartmentConverter.convert(x, TenantAppDepartmentExtension.BASIC)).collect(Collectors.toList());
	}

	@NewSpan
	public List<PathTenantAppDepartment> getPathDepartmentList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(departmentIds);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.DEPTH),
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.LEFT_NO)
				)
			);

		List<TenantAppDepartmentMongodb> lastDepartmentList = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		// List<String> departmentIds = lastDepartmentList.stream().map(DepartmentMongodb::getDepartmentId).distinct().collect(Collectors.toList());
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

	public Map<String, PathTenantAppDepartment> getPathDepartmentMap(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> departmentIds) {
		return getPathDepartmentList(tenantId, appId, departmentIds).stream()
			.collect(Collectors.toMap(x -> x.getDepartmentIds().get(x.getDepartmentIds().size() - 1), x -> x));
	}

	/**
	 * 判断是否存在用户使用departmentIds
	 *
	 * @param tenantId      tenantId
	 * @param appId         appId
	 * @param departmentIds departmentIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicTenantAppUser> existsUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull @NotEmpty String... departmentIds) {
		return existsUserList(tenantId, appId, Set.of(departmentIds));
	}

	@NewSpan
	public List<BasicTenantAppUser> existsUserList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> departmentIds) {
		Criteria criteria = Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.DEPARTMENT_IDS).in(departmentIds);
		Query query = Query.query(criteria);
		query.fields().include(TenantAppUserMongodb.FIELD.USER_ID, TenantAppUserMongodb.FIELD.NICKNAME);
		query.limit(10);
		return readMongoTemplate.find(Query.query(criteria), TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER).stream().map(TenantAppUserConverter::convertMetadataUser).collect(Collectors.toList());
	}

	public TenantAppDepartment getRootDepartment(@Valid @NotNull String tenantId, @Valid @NotNull String appId) {
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.ROOT).is(true);
		Query query = Query.query(criteria);
		TenantAppDepartmentMongodb root = readMongoTemplate.findOne(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		if (root == null) {
			root = TenantAppDepartmentMongodb.builder()
				.tenantId(tenantId)
				.appId(appId)
				.parentId(null)
				.root(true)
				.departmentId(CoreConstants.SNOWFLAKE.nextIdStr())
				.departmentName(tenantId)
				.remark(String.format("%s的%s的组织结构", tenantId, appId))
				.leftNo(1)
				.rightNo(2)
				.depth(0)
				.metadata(TenantAppUserMetadataMongodb.builder()
					.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
					.build())
				.build();
			mongoTemplate.insert(root, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		}
		return TenantAppDepartment.builder()
			.departmentId(root.getDepartmentId())
			.departmentName(root.getDepartmentName())
			.build();
	}

	public Map<String, PathTenantAppDepartment> getSubDepartmentMap(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> departmentIds) {
		return getSubDepartmentList(tenantId, appId, departmentIds).stream()
			.collect(Collectors.toMap(x -> x.getDepartmentIds().get(0), x -> x));
	}


	@NewSpan
	public List<PathTenantAppDepartment> getSubDepartmentList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, Collection<String> departmentIds) {

		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(departmentIds);

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
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).ne(CairoAuthConstants.ROOT_ID)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gt(x.getLeftNo())
							.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).lt(x.getRightNo()))
						.collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentDepartmentCriteria);
				return readMongoTemplate.find(noParentMenuQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
			}).orElse(Collections.emptyList());

		List<TenantAppDepartmentMongodb> allDepartmentList = Stream.of(subDepartmentMongodbList, lastDepartmentList)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingInt(TenantAppDepartmentMongodb::getLeftNo))
			.collect(Collectors.toList());

		return lastDepartmentList.stream()
			.map(d -> {
				List<TenantAppDepartmentMongodb> departmentMongodbList = new ArrayList<>();
				allDepartmentList.forEach(department -> {
					if (department.getLeftNo() >= d.getLeftNo() && department.getRightNo() <= d.getRightNo()) {
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
}
