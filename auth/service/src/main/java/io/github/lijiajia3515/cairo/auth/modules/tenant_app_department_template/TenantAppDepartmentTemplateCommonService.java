package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.micrometer.tracing.annotation.NewSpan;
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

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

@Slf4j
@Validated
@Component
public class TenantAppDepartmentTemplateCommonService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;

	public TenantAppDepartmentTemplateCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}

	/**
	 * 根据部门模板id查询部门模板信息
	 *
	 * @param departmentIds departmentIds
	 * @return 部门模板集合
	 */
	@NewSpan
	public List<TenantAppDepartmentTemplate> getDepartmentList(Collection<String> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).in(departmentIds);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		List<TenantAppDepartmentTemplateMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

		return dms.stream().map(x -> TenantAppDepartmentTemplateConverter.convert(x, TenantAppDepartmentTemplateExtension.BASIC)).collect(Collectors.toList());
	}

	@NewSpan
	public List<PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplateList(String appId, Collection<String> departmentIds) {
		if (departmentIds == null || departmentIds.isEmpty()) return Collections.emptyList();
		Criteria criteria = Criteria
			.where(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).in(departmentIds)
			.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.DEPTH),
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO)
				)
			);

		List<TenantAppDepartmentTemplateMongodb> lastTenantAppDepartmentTemplateList = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

		// List<String> app_departmentIds = lastTenantAppDepartmentTemplateList.stream().map(TenantAppDepartmentTemplateMongodb::getDepartmentId).distinct().collect(Collectors.toList());
		// 获取部门模板列表中的父级部门模板列表并从已有的数据中去除重复
		Set<TenantAppDepartmentTemplateMongodb> noSelectParentTenantAppDepartmentTemplateList = lastTenantAppDepartmentTemplateList.stream().filter(x -> !departmentIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<TenantAppDepartmentTemplateMongodb> parentTenantAppDepartmentTemplateMongodbList = Optional.of(noSelectParentTenantAppDepartmentTemplateList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentTenantAppDepartmentTemplateCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).ne(ROOT_ID)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).lt(x.getLeftNo())
							.and(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).gt(x.getRightNo()))
						.collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentTenantAppDepartmentTemplateCriteria);
				return readMongoTemplate.find(noParentMenuQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
			}).orElse(Collections.emptyList());

		List<TenantAppDepartmentTemplateMongodb> allTenantAppDepartmentTemplateList = Stream.of(parentTenantAppDepartmentTemplateMongodbList, lastTenantAppDepartmentTemplateList)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingInt(TenantAppDepartmentTemplateMongodb::getLeftNo))
			.toList();

		return lastTenantAppDepartmentTemplateList.stream()
			.map(d -> {
				List<TenantAppDepartmentTemplateMongodb> departmentMongodbList = new ArrayList<>();
				allTenantAppDepartmentTemplateList.forEach(app_department -> {
					if (app_department.getLeftNo() <= d.getLeftNo() && app_department.getRightNo() >= d.getRightNo()) {
						departmentMongodbList.add(app_department);
					}
				});

				return PathTenantAppDepartmentTemplate.builder()
					.tenantAppDepartmentTemplateIds(departmentMongodbList.stream().map(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateId).collect(Collectors.toList()))
					.tenantAppDepartmentTemplateNames(departmentMongodbList.stream().map(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateName).collect(Collectors.toList()))
					.depth(departmentMongodbList.size())
					.build();
			})
			.collect(Collectors.toList());
	}

	public Map<String, PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplateMap(String appId,Collection<String> departmentIds) {
		return getPathTenantAppDepartmentTemplateList(appId,departmentIds).stream()
			.collect(Collectors.toMap(x -> x.getTenantAppDepartmentTemplateIds().get(x.getTenantAppDepartmentTemplateIds().size() - 1), x -> x));
	}


	public TenantAppDepartmentTemplate getRootTenantAppDepartmentTemplate(String appId) {
		Criteria criteria = Criteria
			.where(TenantAppDepartmentTemplateMongodb.FIELD.ROOT).is(true);
		Query query = Query.query(criteria);
		TenantAppDepartmentTemplateMongodb root = readMongoTemplate.findOne(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		if (root == null) {
			root = TenantAppDepartmentTemplateMongodb.builder()
				.tenantAppDepartmentTemplateId(CoreConstants.SNOWFLAKE.nextIdStr())
				.tenantAppDepartmentTemplateName("组织结构")
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
			mongoTemplate.insert(root, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
			log.info("create department root node: {} {} {}", root.getTenantAppDepartmentTemplateId(), root.getTenantAppDepartmentTemplateName(), root.getRemark());
		}
		return TenantAppDepartmentTemplate
			.builder()
			.tenantAppDepartmentTemplateId(root.getTenantAppDepartmentTemplateId())
			.tenantAppDepartmentTemplateName(root.getTenantAppDepartmentTemplateName())
			.build();
	}


}
