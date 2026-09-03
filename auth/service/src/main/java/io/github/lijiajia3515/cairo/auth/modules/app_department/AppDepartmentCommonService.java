package io.github.lijiajia3515.cairo.auth.modules.app_department;

import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
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
public class AppDepartmentCommonService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;

	public AppDepartmentCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 根据应用部门id查询应用部门信息
	 *
	 * @param appId         appId
	 * @param departmentIds departmentIds
	 * @return 应用部门集合
	 */
	@NewSpan
	public List<AppDepartment> getDepartmentList(@Valid @NotNull String appId, Collection<String> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(departmentIds);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<AppDepartmentMongodb> dms = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		return dms.stream().map(x -> AppDepartmentConverter.convert(x, AppDepartmentExtension.BASIC)).collect(Collectors.toList());
	}

	@NewSpan
	public List<PathAppDepartment> getPathAppDepartmentList(@Valid @NotNull String appId, Collection<String> app_departmentIds) {
		if (app_departmentIds == null || app_departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(app_departmentIds);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.DEPTH),
					Sort.Order.asc(AppDepartmentMongodb.FIELD.LEFT_NO)
				)
			);

		List<AppDepartmentMongodb> lastAppDepartmentList = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		// List<String> app_departmentIds = lastAppDepartmentList.stream().map(AppDepartmentMongodb::getDepartmentId).distinct().collect(Collectors.toList());
		// 获取应用部门列表中的父级应用部门列表并从已有的数据中去除重复
		Set<AppDepartmentMongodb> noSelectParentAppDepartmentList = lastAppDepartmentList.stream().filter(x -> !app_departmentIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<AppDepartmentMongodb> parentAppDepartmentMongodbList = Optional.of(noSelectParentAppDepartmentList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentAppDepartmentCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).ne(CairoAuthConstants.ROOT_ID)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(AppDepartmentMongodb.FIELD.LEFT_NO).lt(x.getLeftNo())
							.and(AppDepartmentMongodb.FIELD.RIGHT_NO).gt(x.getRightNo()))
						.collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentAppDepartmentCriteria);
				return readMongoTemplate.find(noParentMenuQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
			}).orElse(Collections.emptyList());

		List<AppDepartmentMongodb> allAppDepartmentList = Stream.of(parentAppDepartmentMongodbList, lastAppDepartmentList)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingInt(AppDepartmentMongodb::getLeftNo))
			.collect(Collectors.toList());

		return lastAppDepartmentList.stream()
			.map(d -> {
				List<AppDepartmentMongodb> app_departmentMongodbList = new ArrayList<>();
				allAppDepartmentList.forEach(app_department -> {
					if (app_department.getLeftNo() <= d.getLeftNo() && app_department.getRightNo() >= d.getRightNo()) {
						app_departmentMongodbList.add(app_department);
					}
				});

				return PathAppDepartment.builder()
					.departmentIds(app_departmentMongodbList.stream().map(AppDepartmentMongodb::getDepartmentId).collect(Collectors.toList()))
					.departmentNames(app_departmentMongodbList.stream().map(AppDepartmentMongodb::getDepartmentName).collect(Collectors.toList()))
					.depth(app_departmentMongodbList.size())
					.build();
			})
			.collect(Collectors.toList());
	}

	public Map<String, PathAppDepartment> getPathAppDepartmentMap(@Valid @NotNull String appId, Collection<String> departmentIds) {
		return getPathAppDepartmentList(appId, departmentIds).stream()
			.collect(Collectors.toMap(x -> x.getDepartmentIds().get(x.getDepartmentIds().size() - 1), x -> x));
	}

	/**
	 * 判断是否存在用户使用app_departmentIds
	 *
	 * @param appId         appId
	 * @param departmentIds departmentIds
	 * @return 是否存在用户
	 */
	@NewSpan
	public List<BasicAppUser> existsUserList(@Valid @NotNull String appId, @Valid @NotNull @NotEmpty String... departmentIds) {
		return existsUserList(appId, Set.of(departmentIds));
	}

	@NewSpan
	public List<BasicAppUser> existsUserList(@Valid @NotNull String appId, @Valid @NotNull @NotEmpty Collection<String> appDepartmentIds) {
		Criteria criteria = Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.DEPARTMENT_IDS).in(appDepartmentIds);
		Query query = Query.query(criteria);
		query.fields().include(AppUserMongodb.FIELD.USER_ID, AppUserMongodb.FIELD.NICKNAME);
		query.limit(10);
		return readMongoTemplate.find(Query.query(criteria), AppUserMongodb.class, MongodbConstants.Collection.APP_USER).stream()
			.map(AppUserConverter::convertBasicAppUser)
			.collect(Collectors.toList());
	}

	public AppDepartment getRootAppDepartment(String appId) {
		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(AppDepartmentMongodb.FIELD.ROOT).is(true);
		Query query = Query.query(criteria);
		AppDepartmentMongodb root = readMongoTemplate.findOne(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		if (root == null) {
			root = AppDepartmentMongodb.builder()
				.appId(appId)
				.departmentId(CoreConstants.nextIdStr())
				.departmentName("组织结构")
				.parentId(null)
				.root(true)
				.remark(String.format("%s的根节点", appId))
				.leftNo(1)
				.rightNo(2)
				.depth(0)
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(CairoSecurityContextHolder.getSubappUserId())
					.updateUserId(CairoSecurityContextHolder.getSubappUserId())
					.build())
				.build();
			mongoTemplate.insert(root, MongodbConstants.Collection.APP_DEPARTMENT);
			log.info("create app department root node: {} {} {}", root.getDepartmentId(), root.getDepartmentName(), root.getRemark());
		}
		return AppDepartment
			.builder()
			.departmentId(root.getDepartmentId())
			.departmentName(root.getDepartmentName())
			.build();
	}


}
