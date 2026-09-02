package io.github.lijiajia3515.cairo.auth.api.subapp.tenant_app_department_template;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.MetadataTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.PathTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template.TenantAppDepartmentTemplateCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template.TenantAppDepartmentTemplateConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TreeNodeTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.CreateTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.DeleteTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.GetTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.GetTenantAppDepartmentTemplateTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.ModifyTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.ModifyTenantAppDepartmentTemplateStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.tenant_app_department_template.MoveTenantAppDepartmentTemplateArgs;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
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
 * [subapp_user/api] tenant_app_department_template service
 */
@Slf4j
@Validated
@Component
public class TenantAppDepartmentTemplateSubappApiService {
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final TenantAppDepartmentTemplateCommonService departmentTemplateCommonService;
	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_department_template";

	private final SerialService serialService;

	public TenantAppDepartmentTemplateSubappApiService(TransactionTemplate transactionTemplate,
														   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														   AppUserCommonService appUserCommonService,
														   TenantAppDepartmentTemplateCommonService departmentTemplateCommonService, SerialService serialService) {
		this.transactionTemplate = transactionTemplate;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.appUserCommonService = appUserCommonService;
		this.departmentTemplateCommonService = departmentTemplateCommonService;
		this.serialService = serialService;
	}

	/**
	 * 企业部门模板查询
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 企业部门模板列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:get_tenant_app_department_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MetadataTenantAppDepartmentTemplate> getTenantAppDepartmentTemplateList(@Valid @NotNull String appId, @Validated GetTenantAppDepartmentTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppDepartmentTemplateMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		return getTenantAppDepartmentTemplateList(appId,dms, args.getExtension());
	}

	/**
	 * 查找
	 *
	 * @param args args
	 * @return 企业部门模板查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:get_path_tenant_app_department_template_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplateList(@Valid String appId, @Validated GetTenantAppDepartmentTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);
		final List<TenantAppDepartmentTemplateMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		return getPathTenantAppDepartmentTemplateList(appId, dms);
	}

	/**
	 * 查找
	 *
	 * @param args args
	 * @return 企业部门模板查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:get_path_tenant_app_department_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplatePageList(String appId, @Validated GetTenantAppDepartmentTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);
		long total = mongoTemplate.count(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		final List<TenantAppDepartmentTemplateMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		List<PathTenantAppDepartmentTemplate> pathTenantAppDepartmentTemplateList = getPathTenantAppDepartmentTemplateList(appId,dms);
		return new Page<>(args, pathTenantAppDepartmentTemplateList, total);
	}

	/**
	 * 获取企业部门模板分页列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 企业部门模板查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:get_tenant_app_department_template_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<MetadataTenantAppDepartmentTemplate> getTenantAppDepartmentTemplatePageList(@Valid String appId, @Valid @Validated GetTenantAppDepartmentTemplateArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = mongoTemplate.count(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<TenantAppDepartmentTemplateMongodb> tenant_app_department_templateMongodbList = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		List<MetadataTenantAppDepartmentTemplate> ds = getTenantAppDepartmentTemplateList(appId,tenant_app_department_templateMongodbList, args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 获取企业部门模板树形列表
	 *
	 * @param appId appI
	 * @return 企业部门模板 list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:get_tenant_app_department_template_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	TreeNodeTenantAppDepartmentTemplate getTenantAppDepartmentTemplateTree(@NotNull String appId, @Validated GetTenantAppDepartmentTemplateTreeArgs args) {
		// 确认查询节点，默认根节点
		String parentId = Optional.ofNullable(args).map(GetTenantAppDepartmentTemplateTreeArgs::getParentId)
			.orElse(departmentTemplateCommonService.getRootTenantAppDepartmentTemplate(appId).getTenantAppDepartmentTemplateId());

		Criteria criteria = new Criteria();
		Query parentQuery = Query.query(Criteria.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).is(parentId));
		TenantAppDepartmentTemplateMongodb parentTenantAppDepartmentTemplate = readMongoTemplate.findOne(parentQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
		if (parentTenantAppDepartmentTemplate != null) {
			criteria
				.and(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(parentTenantAppDepartmentTemplate.getLeftNo())
				.and(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).lte(parentTenantAppDepartmentTemplate.getRightNo());
		}

		Query query = Query.query(criteria);
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.DEPTH),
				Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO)
			)
		);
		List<TenantAppDepartmentTemplateMongodb> appDepartmentMongodbList = readMongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

		TreeNodeTenantAppDepartmentTemplate rootNode = TenantAppDepartmentTemplateConverter.treeConvert(parentTenantAppDepartmentTemplate);
		List<TreeNodeTenantAppDepartmentTemplate> nodes = appDepartmentMongodbList.stream().map(TenantAppDepartmentTemplateConverter::treeConvert).collect(Collectors.toList());
		List<TreeNodeTenantAppDepartmentTemplate> subNodes = Tree2Converter.build(nodes, parentId);
		rootNode.subs(subNodes);
		return rootNode;
	}

	Optional<PathTenantAppDepartmentTemplate> getTenantAppDepartmentTemplateByTenantAppDepartmentTemplateId(String appId, @Valid @NotNull String departmentId) {
		Criteria lastCriteria = Criteria
			.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).is(departmentId);

		Query lastQuery = Query.query(lastCriteria);
		return Optional.ofNullable(readMongoTemplate.findOne(lastQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE))
			.flatMap(last -> getPathTenantAppDepartmentTemplateList(appId,Collections.singletonList(last)).stream().findFirst());
	}

	/**
	 * 创建企业部门模板
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:create_tenant_app_department_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenantAppDepartmentTemplate(@Valid @NotNull String appId, @Validated CreateTenantAppDepartmentTemplateArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				String parentId = Optional.ofNullable(args.getParentId()).orElse(departmentTemplateCommonService.getRootTenantAppDepartmentTemplate(appId).getTenantAppDepartmentTemplateId());
				Query parentQuery = Query.query(Criteria.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).is(parentId));
				TenantAppDepartmentTemplateMongodb parentTenantAppDepartmentTemplate = mongoTemplate.findOne(parentQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

				if (parentTenantAppDepartmentTemplate == null) {
					throw new ConflictBusinessException("父节点不存在");
				}

				// find brother
				Query brotherNodeQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.PARENT_ID).is(parentId));
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO)));

				List<TenantAppDepartmentTemplateMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				Optional<TenantAppDepartmentTemplateMongodb> afterNode = brotherNodes.stream().filter(a -> a.getTenantAppDepartmentTemplateId().equals(args.getBeforeId())).findFirst();
				int position;
				int left;
				int right;
				if (brotherNodes.isEmpty() || afterNode.isEmpty()) {
					position = parentTenantAppDepartmentTemplate.getRightNo();
					left = parentTenantAppDepartmentTemplate.getRightNo();
					right = parentTenantAppDepartmentTemplate.getRightNo() + 1;
				} else {
					position = afterNode.get().getLeftNo();
					left = afterNode.get().getLeftNo();
					right = afterNode.get().getLeftNo() + 1;
				}


				// 左值扩容
				Query leftParentQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(position)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
				);

				Update leftParentUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO, 2)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult leftParentUpdateResult = mongoTemplate.updateMulti(leftParentQuery, leftParentUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.debug("leftParentUpdateResult: {}", leftParentUpdateResult);


				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).gte(position)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
				);
				Update update = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO, 2)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult rightParentUpdateResult = mongoTemplate.updateMulti(rightParentQuery, update, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.debug("rightParentUpdateResult: {}", rightParentUpdateResult);

				// 插入企业部门
				TenantAppDepartmentTemplateMongodb insertTenantAppDepartmentTemplateMongodb = TenantAppDepartmentTemplateMongodb.builder()
					.appId(appId)
					.parentId(parentTenantAppDepartmentTemplate.getTenantAppDepartmentTemplateId())
					.root(false)
					.tenantAppDepartmentTemplateId(serialService.nextStr(SERIAL_NAMESPACE, SERIAL_KEY,1,1001))
					.tenantAppDepartmentTemplateName(args.getTenantAppDepartmentTemplateName())
					.remark(args.getRemark())
					.leftNo(left)
					.rightNo(right)
					.depth(parentTenantAppDepartmentTemplate.getDepth() + 1)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				TenantAppDepartmentTemplateMongodb insertedTenantAppDepartmentTemplateMongodb = mongoTemplate.insert(insertTenantAppDepartmentTemplateMongodb, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.info("inserted  department: {}", insertedTenantAppDepartmentTemplateMongodb.getTenantAppDepartmentTemplateId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("createTenantAppDepartmentTemplate", e);
				throw new ConflictBusinessException("创建企业部门模板失败");
			}
		});
	}

	/**
	 * 修改企业部门模板信息
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:modify_tenant_app_department_template_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "modify_tenant_app_department_template_info", keys = {"#args.tenantAppDepartmentTemplateId"})
	public void modifyTenantAppDepartmentTemplateInfo(@Valid String appId, @Validated ModifyTenantAppDepartmentTemplateArgs args) {
		transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).is(args.getTenantAppDepartmentTemplateId());
				Query query = Query.query(criteria);
				TenantAppDepartmentTemplateMongodb appDepartmentMongodb = mongoTemplate.findOne(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);

				if (appDepartmentMongodb == null) {
					throw new ConflictBusinessException("更新企业部门模板失败(企业部门模板不存在)");
				}

				Optional.ofNullable(args.getTenantAppDepartmentTemplateName()).ifPresent(appDepartmentMongodb::setTenantAppDepartmentTemplateName);
				Optional.ofNullable(args.getRemark()).ifPresent(appDepartmentMongodb::setRemark);
				appDepartmentMongodb.getMetadata().setUpdateUserId(CairoSecurityContextHolder.getSubappUserId());
				appDepartmentMongodb.getMetadata().setUpdateTime(LocalDateTime.now());
				mongoTemplate.save(appDepartmentMongodb, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				return appDepartmentMongodb;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantAppDepartmentTemplateInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业部门模板失败");
			}
		});
	}

	/**
	 * 企业部门移动
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:move_tenant_app_department_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "move_tenant_app_department_template", keys = {"#appId"})
	public void moveTenantAppDepartmentTemplate(String appId, @Validated MoveTenantAppDepartmentTemplateArgs args) {
		// 	1. 删除移动的节点，
		// 	2. 缩容
		//  3. 扩容
		//  4. 插入删移动的节点（更新leftNo,rightNo)
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 先查询三个节点信息
				Criteria criteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).in(args.getMoveId(), args.getBeforeId(), args.getParentId());
				Query query = Query.query(criteria);
				Map<String, TenantAppDepartmentTemplateMongodb> tenantAppDepartmentTemplateMongodbMap = mongoTemplate.find(query, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE).stream().collect(Collectors.toMap(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateId, x -> x));
				// 移动的节点
				TenantAppDepartmentTemplateMongodb moveTenantAppDepartmentTemplate = tenantAppDepartmentTemplateMongodbMap.get(args.getMoveId());
				// 移动后的父节点
				TenantAppDepartmentTemplateMongodb parentTenantAppDepartmentTemplate = tenantAppDepartmentTemplateMongodbMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentTemplateMongodb beforeTenantAppDepartmentTemplate = tenantAppDepartmentTemplateMongodbMap.get(args.getBeforeId());
				if (moveTenantAppDepartmentTemplate == null || parentTenantAppDepartmentTemplate == null) {
					throw new ParamsErrorBusinessException("moveId is null or parentId 错误");
				}

				if (parentTenantAppDepartmentTemplate.getLeftNo() >= moveTenantAppDepartmentTemplate.getLeftNo() && parentTenantAppDepartmentTemplate.getRightNo() <= moveTenantAppDepartmentTemplate.getRightNo()) {
					throw new ParamsErrorBusinessException("parentId 不能设置为移动节点的子节点");
				}

				// 容错beforeId错误的问题，默认移动到最后
				if (beforeTenantAppDepartmentTemplate != null && !beforeTenantAppDepartmentTemplate.getParentId().equals(parentTenantAppDepartmentTemplate.getTenantAppDepartmentTemplateId())) {
					beforeTenantAppDepartmentTemplate = null;
				}

				// 查询后删除移动的节点
				Criteria moveNodeCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(moveTenantAppDepartmentTemplate.getLeftNo())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).lte(moveTenantAppDepartmentTemplate.getRightNo());
				Query moveNodeQuery = Query.query(moveNodeCriteria);
				moveNodeQuery.with(Sort.by(Sort.Order.asc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO)));
				List<TenantAppDepartmentTemplateMongodb> moveNodes = mongoTemplate.findAllAndRemove(moveNodeQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				// 移动的数值
				int moveNum = moveNodes.size() * 2;

				// 缩容左值
				Criteria subNodeLeftCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(moveTenantAppDepartmentTemplate.getRightNo() + 1)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);
				Query subNodeLeftQuery = Query.query(subNodeLeftCriteria);
				Update subNodeLeftUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO, -moveNum)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeLeftUpdateResult = mongoTemplate.updateMulti(subNodeLeftQuery, subNodeLeftUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.info("subNodeLeftUpdateResult: {}", subNodeLeftUpdateResult);

				// 缩容右值
				Criteria subNodeRightCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).gte(moveTenantAppDepartmentTemplate.getRightNo() + 1)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);
				Query subNodeRightQuery = Query.query(subNodeRightCriteria);
				Update subNodeRightUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO, -moveNum)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeRightUpdateResult = mongoTemplate.updateMulti(subNodeRightQuery, subNodeRightUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.info("subNodeRightUpdateResult: {}", subNodeRightUpdateResult);

				Criteria newNodeCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).in(args.getBeforeId(), args.getParentId())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);
				Query newNodequery = Query.query(newNodeCriteria);

				Map<String, TenantAppDepartmentTemplateMongodb> newTenantAppDepartmentTemplateIdMap = mongoTemplate.find(newNodequery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE).stream().collect(Collectors.toMap(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateId, x -> x));

				// 移动后的父节点
				TenantAppDepartmentTemplateMongodb newParentTenantAppDepartmentTemplate = newTenantAppDepartmentTemplateIdMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentTemplateMongodb newBeforeTenantAppDepartmentTemplate = beforeTenantAppDepartmentTemplate == null ? null : newTenantAppDepartmentTemplateIdMap.get(args.getBeforeId());

				// 扩容
				int startAddNum = Optional.ofNullable(newBeforeTenantAppDepartmentTemplate).map(TenantAppDepartmentTemplateMongodb::getLeftNo).orElse(newParentTenantAppDepartmentTemplate.getRightNo());
				// 扩容左值
				Criteria addNodeLeftCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(startAddNum)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);
				Query addNodeLeftQuery = Query.query(addNodeLeftCriteria);

				Update addNodeLeftUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO, moveNum)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateMulti(addNodeLeftQuery, addNodeLeftUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);

				// 扩容右值
				Criteria addNodeRightCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).gte(startAddNum)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);
				Query addNodeRightQuery = Query.query(addNodeRightCriteria);
				Update addNodeRightUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO, moveNum)
					.currentDate(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult addNodeRightUpdateResult = mongoTemplate.updateMulti(addNodeRightQuery, addNodeRightUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.info("addNodeRightUpdateResult: {}", addNodeRightUpdateResult);

				newTenantAppDepartmentTemplateIdMap = mongoTemplate.find(newNodequery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE).stream().collect(Collectors.toMap(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateId, x -> x));

				// 移动后的父节点
				TenantAppDepartmentTemplateMongodb newParentTenantAppDepartmentTemplate2 = newTenantAppDepartmentTemplateIdMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentTemplateMongodb newBeforeTenantAppDepartmentTemplate2 = beforeTenantAppDepartmentTemplate == null ? null : newTenantAppDepartmentTemplateIdMap.get(args.getBeforeId());


				int leftNo = moveTenantAppDepartmentTemplate.getLeftNo();
				moveNodes.forEach(x -> {
					if (newBeforeTenantAppDepartmentTemplate2 == null) {
						// 使用右基点
						x.setLeftNo(x.getLeftNo() - leftNo + newParentTenantAppDepartmentTemplate2.getRightNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newParentTenantAppDepartmentTemplate2.getRightNo() - moveNum);
					} else {
						// 使用左基点
						x.setLeftNo(x.getLeftNo() - leftNo + newBeforeTenantAppDepartmentTemplate2.getLeftNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newBeforeTenantAppDepartmentTemplate2.getLeftNo() - moveNum);
					}
					if (x.getTenantAppDepartmentTemplateId().equals(args.getMoveId())) {
						x.setParentId(args.getParentId());
					}
					x.setDepth(x.getDepth() + parentTenantAppDepartmentTemplate.getDepth() + 1 - moveTenantAppDepartmentTemplate.getDepth());

					x.getMetadata().setUpdateTime(LocalDateTime.now());
				});
				mongoTemplate.insert(moveNodes, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("moveTenantAppDepartmentTemplate", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动企业部门模板失败");
			}
		});
	}

	/**
	 * 企业部门模板删除，包含删除子级企业部门模板
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:delete_tenant_app_department_template",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "delete_tenant_app_department_template", keys = {"#args.tenantAppDepartmentTemplateId"})
	public void deleteTenantAppDepartmentTemplate(@Valid String appId, @Validated DeleteTenantAppDepartmentTemplateArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query appDepartmentQuery = Query.query(
					Criteria
						.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).is(args.getTenantAppDepartmentTemplateId())
				);

				TenantAppDepartmentTemplateMongodb deleteTenantAppDepartmentTemplate = mongoTemplate.findOne(appDepartmentQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				if (deleteTenantAppDepartmentTemplate == null) {
					throw new ConflictBusinessException("企业部门模板不存在，删除失败");
				}
				if (deleteTenantAppDepartmentTemplate.isRoot()) {
					throw new ConflictBusinessException("根节点不能删除");
				}

				Query deleteMenuQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gte(deleteTenantAppDepartmentTemplate.getLeftNo())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).lte(deleteTenantAppDepartmentTemplate.getRightNo())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
				);
				int inc = -(deleteTenantAppDepartmentTemplate.getRightNo() - deleteTenantAppDepartmentTemplate.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("企业部门模板含有子级企业部门模板，请先删除子级企业部门模板后在操作");
				}

				// 更新左值
				Query otherMenuLeftQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).gt(deleteTenantAppDepartmentTemplate.getLeftNo())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
				);
				Update otherMenuLeftUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO, inc)
					.set(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 更新右值
				Query otherMenuRightQuery = Query.query(Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO).gt(deleteTenantAppDepartmentTemplate.getRightNo())
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
				);
				Update otherMenuRightUpdate = new Update()
					.inc(TenantAppDepartmentTemplateMongodb.FIELD.RIGHT_NO, inc)
					.set(TenantAppDepartmentTemplateMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 删除目标
				List<TenantAppDepartmentTemplateMongodb> deleteTenantAppDepartmentTemplateList = mongoTemplate.findAllAndRemove(deleteMenuQuery, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				// 移动到删除影子表
				if (!deleteTenantAppDepartmentTemplateList.isEmpty()) {
					mongoTemplate.insert(deleteTenantAppDepartmentTemplateList, MongodbConstants.DeletedCollection.TENANT_APP_DEPARTMENT_TEMPLATE);
				}

				// 移动其他菜单左右值
				UpdateResult otherTenantAppDepartmentTemplateLeftUpdateResult = mongoTemplate.updateMulti(otherMenuLeftQuery, otherMenuLeftUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.debug("OtherTenantAppDepartmentTemplateLeftUpdateResult: {}", otherTenantAppDepartmentTemplateLeftUpdateResult);
				UpdateResult otherMenuLeftUpdateResult = mongoTemplate.updateMulti(otherMenuRightQuery, otherMenuRightUpdate, TenantAppDepartmentTemplateMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT_TEMPLATE);
				log.debug("OtherTenantAppDepartmentTemplateRightUpdateResult: {}", otherMenuLeftUpdateResult);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeTenantAppDepartmentTemplate", e);
				throw new ConflictBusinessException("删除企业部门模板失败");
			}

		});
	}

	/**
	 * 修改企业部门模板状态
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:modify_tenant_app_department_template_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "modify_tenant_app_department_template_status", keys = {"#appId"})
	public void modifyTenantAppDepartmentTemplateStatus(@Valid String appId, @Validated ModifyTenantAppDepartmentTemplateStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppMongodb.FIELD.APP_ID).is(appId);
				Query query = Query.query(criteria);
				AppMongodb appMongodb = mongoTemplate.findOne(query, AppMongodb.class, MongodbConstants.Collection.APP);

				if (appMongodb == null) {
					throw new ConflictBusinessException("应用不存在");
				}

				Update update = Update.update(AppMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_STATUS, args.getEnabled());

				mongoTemplate.updateFirst(query,update,AppMongodb.class, MongodbConstants.Collection.APP);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantAppDepartmentTemplateInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改企业部门模板状态失败");
			}
		});

	}

	/**
	 * 获取企业部门模板状态
	 *
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department_template:modify_tenant_app_department_template_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId")
		}
	)
	Optional<Boolean> getTenantAppDepartmentDepartmentStatus(String appId) {
		Criteria criteria = Criteria
			.where(AppMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		AppMongodb appMongodb = mongoTemplate.findOne(query, AppMongodb.class, MongodbConstants.Collection.APP);

		if (appMongodb == null) {
			throw new ConflictBusinessException("应用不存在");
		}
		return Optional.of(Optional.ofNullable(appMongodb.getTenantAppDepartmentTemplateStatus()).orElse(false));
	}


	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	protected Criteria buildCriteria(String appId, @NotNull GetTenantAppDepartmentTemplateArgs args) {
		Criteria criteria = Criteria.where(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getTenantAppDepartmentTemplateIds()).filter(x -> !x.isEmpty()).ifPresent(ids -> criteria.and(TenantAppDepartmentTemplateMongodb.FIELD.TENANT_APP_DEPARTMENT_TEMPLATE_ID).in(ids));
		Optional.ofNullable(args.getParentId()).filter(x -> !x.isBlank()).ifPresent(parent -> criteria.and(TenantAppDepartmentTemplateMongodb.FIELD.PARENT_ID).is(parent));

		return criteria;
	}

	/**
	 * 查询
	 *
	 * @param lastTenantAppDepartmentTemplateList 企业部门模板列表
	 * @return 1
	 */
	@NewSpan
	protected List<PathTenantAppDepartmentTemplate> getPathTenantAppDepartmentTemplateList(@Valid String appId, List<TenantAppDepartmentTemplateMongodb> lastTenantAppDepartmentTemplateList) {
		List<String> tenantAppDepartmentTemplateIds = lastTenantAppDepartmentTemplateList.stream().map(TenantAppDepartmentTemplateMongodb::getTenantAppDepartmentTemplateId).distinct().toList();
		// 获取企业部门模板列表中的父级企业部门模板列表并从已有的数据中去除重复
		Set<TenantAppDepartmentTemplateMongodb> noSelectParentTenantAppDepartmentTemplateList = lastTenantAppDepartmentTemplateList.stream().filter(x -> !tenantAppDepartmentTemplateIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<TenantAppDepartmentTemplateMongodb> parentTenantAppDepartmentTemplateMongodbList = Optional.of(noSelectParentTenantAppDepartmentTemplateList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentTenantAppDepartmentTemplateCriteria = Criteria
					.where(TenantAppDepartmentTemplateMongodb.FIELD.ROOT).is(false)
					.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
					.orOperator(noParentMenus.stream()
						.map(x -> Criteria
							.where(TenantAppDepartmentTemplateMongodb.FIELD.LEFT_NO).lt(x.getLeftNo())
							.and(TenantAppDepartmentTemplateMongodb.FIELD.APP_ID).is(appId)
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
				allTenantAppDepartmentTemplateList.forEach(tenant_app_department_template -> {
					if (tenant_app_department_template.getLeftNo() <= d.getLeftNo() && tenant_app_department_template.getRightNo() >= d.getRightNo()) {
						departmentMongodbList.add(tenant_app_department_template);
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


	public List<MetadataTenantAppDepartmentTemplate> getTenantAppDepartmentTemplateList(String appId,List<TenantAppDepartmentTemplateMongodb> ms, Map<String, String> extensionMap) {
		final TenantAppDepartmentTemplateExtension extension = Optional.of(extensionMap.getOrDefault(CairoAuthExtensionConstants.TENANT_APP_DEPARTMENT_TEMPLATE, TenantAppDepartmentTemplateExtension.ALL.name()))
			.map(TenantAppDepartmentTemplateExtension::valueOf)
			.orElse(TenantAppDepartmentTemplateExtension.ALL);

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(TenantAppDepartmentTemplateMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(appId, userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> TenantAppDepartmentTemplateConverter.convert(x, metadataUserMap, extension)).collect(Collectors.toList());
	}
}
