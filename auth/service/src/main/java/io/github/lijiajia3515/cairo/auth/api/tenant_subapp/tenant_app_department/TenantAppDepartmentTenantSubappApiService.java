package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_department;

import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.MetadataTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.PathTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentField;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TreeNodeTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.CreateDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.DeleteDepartmentUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.GetDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.GetDepartmentTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.ModifyDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_department.MoveDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * [tenant_subapp_user/api] tenant app department service
 */
@Slf4j
@Validated
@Component
public class TenantAppDepartmentTenantSubappApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final TenantAppDepartmentCommonService tenantAppDepartmentCommonService;
	private final TenantAppUserCommonService tenantAppUserCommonService;

	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_department";

	private final SerialService serialService;

	public TenantAppDepartmentTenantSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														 TransactionTemplate transactionTemplate,
														 TenantAppDepartmentCommonService tenantAppDepartmentCommonService,
														 TenantAppUserCommonService tenantAppUserCommonService, SerialService serialService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.tenantAppDepartmentCommonService = tenantAppDepartmentCommonService;
		this.tenantAppUserCommonService = tenantAppUserCommonService;
		this.serialService = serialService;
	}


	/**
	 * 部门查询
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return 部门列表
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_tenant_app_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MetadataTenantAppDepartment> getTenantAppDepartmentList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		return getTenantAppDepartmentList(tenantId, appId, dms, args.getExtension());
	}

	/**
	 * 查找
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return 部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_path_tenant_app_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<PathTenantAppDepartment> getPathTenantAppDepartmentList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);
		final List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		return getPathTenantAppDepartmentList(tenantId, appId, dms);
	}

	/**
	 * 查找
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return 部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_path_tenant_app_department_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<PathTenantAppDepartment> getPathTenantAppDepartmentPageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = mongoTemplate.count(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);

		final List<TenantAppDepartmentMongodb> dms = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		List<PathTenantAppDepartment> pathDepartmentList = getPathTenantAppDepartmentList(tenantId, appId, dms);
		return new Page<>(args, pathDepartmentList, total);
	}

	/**
	 * 获取部门分页列表
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return 部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_tenant_app_department_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<MetadataTenantAppDepartment> getTenantDepartmentPageList(@Valid @NotNull String tenantId, @Valid String appId, @Valid @Validated GetDepartmentArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = mongoTemplate.count(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<TenantAppDepartmentMongodb> departmentMongodbList = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		List<MetadataTenantAppDepartment> ds = getTenantAppDepartmentList(tenantId, appId, departmentMongodbList, args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 获取部门树形列表
	 *
	 * @param tenantId tenantId
	 * @param appId    appI
	 * @return 部门 list
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:get_tenant_app_department_tree",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	TreeNodeTenantAppDepartment getTenantAppDepartmentTree(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetDepartmentTreeArgs args) {
		String parentId = Optional.ofNullable(args).map(GetDepartmentTreeArgs::getParentId).orElse(tenantAppDepartmentCommonService.getRootDepartment(tenantId, appId).getDepartmentId());
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId);

		Query parentQuery = Query.query(Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(parentId));
		TenantAppDepartmentMongodb parentDepartment = readMongoTemplate.findOne(parentQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
		if (parentDepartment == null) {
			throw new ConflictBusinessException(String.format("父节点错误(%s)", parentId));
		}
		criteria
			.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(parentDepartment.getLeftNo())
			.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).lte(parentDepartment.getRightNo());

		Query query = Query.query(criteria);
		query.with(
			Sort.by(
				Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.DEPTH),
				Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.LEFT_NO)
			)
		);
		List<TenantAppDepartmentMongodb> departmentMongodbList = readMongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

		List<TreeNodeTenantAppDepartment> nodes = departmentMongodbList.stream().map(TenantAppDepartmentConverter::treeConvert).collect(Collectors.toList());
		List<TreeNodeTenantAppDepartment> subNodes = Tree2Converter.build(nodes, parentId);
		TreeNodeTenantAppDepartment rootNode = TenantAppDepartmentConverter.treeConvert(parentDepartment);
		rootNode.subs(subNodes);
		return rootNode;
	}

	Optional<PathTenantAppDepartment> getTenantAppDepartmentByDepartmentId(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String departmentId) {
		Criteria lastCriteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(departmentId);

		Query lastQuery = Query.query(lastCriteria);
		return Optional.ofNullable(readMongoTemplate.findOne(lastQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT))
			.flatMap(last -> getPathTenantAppDepartmentList(tenantId, appId, Collections.singletonList(last)).stream().findFirst());
	}

	/**
	 * 创建部门
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:create_tenant_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createTenantAppDepartment(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated CreateDepartmentArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				String parentId = Optional.ofNullable(args.getParentId()).orElse(tenantAppDepartmentCommonService.getRootDepartment(tenantId, appId).getDepartmentId());
				Query parentQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(parentId)
				);
				TenantAppDepartmentMongodb parentDepartment = mongoTemplate.findOne(parentQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

				if (parentDepartment == null) {
					throw new ConflictBusinessException("创建部门失败（父级部门不存在）");
				}

				// find brother
				Query brotherNodeQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.PARENT_ID).is(parentId)
				);
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.LEFT_NO)));

				List<TenantAppDepartmentMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				Optional<TenantAppDepartmentMongodb> afterNode = brotherNodes.stream().filter(a -> a.getDepartmentId().equals(args.getBeforeId())).findFirst();
				int position;
				int left;
				int right;
				if (brotherNodes.isEmpty() || afterNode.isEmpty()) {
					position = parentDepartment.getRightNo();
					left = parentDepartment.getRightNo();
					right = parentDepartment.getRightNo() + 1;
				} else {
					position = afterNode.get().getLeftNo();
					left = afterNode.get().getLeftNo();
					right = afterNode.get().getLeftNo() + 1;
				}


				// 左值扩容
				Query leftParentQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(position)
				);

				Update leftParentUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.LEFT_NO, 2)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult leftParentUpdateResult = mongoTemplate.updateMulti(leftParentQuery, leftParentUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.debug("leftParentUpdateResult: {}", leftParentUpdateResult);


				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).gte(position)
				);
				Update update = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.RIGHT_NO, 2)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult rightParentUpdateResult = mongoTemplate.updateMulti(rightParentQuery, update, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.debug("rightParentUpdateResult: {}", rightParentUpdateResult);

				// 插入菜单
				TenantAppDepartmentMongodb insertDepartmentMongodb = TenantAppDepartmentMongodb.builder()
					.tenantId(tenantId)
					.appId(appId)
					.parentId(parentDepartment.getDepartmentId())
					.root(false)
					.departmentId(serialService.nextStr(SERIAL_NAMESPACE, SERIAL_KEY,1,2001))
					.departmentName(args.getDepartmentName())
					.remark(args.getRemark())
					.leftNo(left)
					.rightNo(right)
					.depth(parentDepartment.getDepth() + 1)
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.build()
					)
					.build();
				TenantAppDepartmentMongodb insertedDepartmentMongodb = mongoTemplate.insert(insertDepartmentMongodb, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.info("inserted tenant app department: {}", insertedDepartmentMongodb.getDepartmentId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("createTenantAppDepartment", e);
				throw new ConflictBusinessException("创建部门失败");
			}
		});
	}

	/**
	 * 修改部门信息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:modify_tenant_app_department_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifyTenantAppDepartmentInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyDepartmentArgs args) {
		transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(args.getDepartmentId());
				Query query = Query.query(criteria);
				TenantAppDepartmentMongodb departmentMongodb = mongoTemplate.findOne(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);

				if (departmentMongodb == null) {
					throw new ConflictBusinessException("修改部门失败（部门不存在）");
				}

				Optional.ofNullable(args.getDepartmentName()).ifPresent(departmentMongodb::setDepartmentName);
				Optional.ofNullable(args.getRemark()).ifPresent(departmentMongodb::setRemark);
				departmentMongodb.getMetadata().setUpdateUserId(CairoSecurityContextHolder.getTenantAppUserId());
				departmentMongodb.getMetadata().setUpdateTime(LocalDateTime.now());
				mongoTemplate.save(departmentMongodb, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				return departmentMongodb;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyTenantAppDepartmentInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改部门失败");
			}
		});
	}

	/**
	 * 菜单移动
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:move_tenant_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void moveTenantAppDepartment(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated MoveDepartmentArgs args) {
		// 	1. 删除移动的节点，
		// 	2. 缩容
		//  3. 扩容
		//  4. 插入删移动的节点（更新leftNo,rightNo)
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 先查询三个节点信息
				Criteria criteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(args.getMoveId(), args.getBeforeId(), args.getParentId());
				Query query = Query.query(criteria);
				Map<String, TenantAppDepartmentMongodb> departmentIdMap = mongoTemplate.find(query, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT).stream().collect(Collectors.toMap(TenantAppDepartmentMongodb::getDepartmentId, x -> x));
				// 移动的节点
				TenantAppDepartmentMongodb moveDepartment = departmentIdMap.get(args.getMoveId());
				// 移动后的父节点
				TenantAppDepartmentMongodb parentDepartment = departmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentMongodb beforeDepartment = departmentIdMap.get(args.getBeforeId());
				if (moveDepartment == null || parentDepartment == null) {
					throw new ParamsErrorBusinessException("移动部门失败（moveId is null or parentId 错误）");
				}

				if (parentDepartment.getLeftNo() >= moveDepartment.getLeftNo() && parentDepartment.getRightNo() <= moveDepartment.getRightNo()) {
					throw new ParamsErrorBusinessException("移动部门失败（parentId 不能设置为移动节点的子节点）");
				}

				// 容错beforeId错误的问题，默认移动到最后
				if (beforeDepartment != null && !beforeDepartment.getParentId().equals(parentDepartment.getDepartmentId())) {
					beforeDepartment = null;
				}

				// 查询后删除移动的节点
				Criteria moveNodeCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(moveDepartment.getLeftNo())
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).lte(moveDepartment.getRightNo());
				Query moveNodeQuery = Query.query(moveNodeCriteria);
				moveNodeQuery.with(Sort.by(Sort.Order.asc(TenantAppDepartmentMongodb.FIELD.LEFT_NO)));
				List<TenantAppDepartmentMongodb> moveNodes = mongoTemplate.findAllAndRemove(moveNodeQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				// 移动的数值
				int moveNum = moveNodes.size() * 2;

				// 缩容左值
				Criteria subNodeLeftCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(moveDepartment.getRightNo() + 1);
				Query subNodeLeftQuery = Query.query(subNodeLeftCriteria);
				Update subNodeLeftUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.LEFT_NO, -moveNum)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeLeftUpdateResult = mongoTemplate.updateMulti(subNodeLeftQuery, subNodeLeftUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.info("subNodeLeftUpdateResult: {}", subNodeLeftUpdateResult);

				// 缩容右值
				Criteria subNodeRightCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).gte(moveDepartment.getRightNo() + 1);
				Query subNodeRightQuery = Query.query(subNodeRightCriteria);
				Update subNodeRightUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.RIGHT_NO, -moveNum)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeRightUpdateResult = mongoTemplate.updateMulti(subNodeRightQuery, subNodeRightUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.info("subNodeRightUpdateResult: {}", subNodeRightUpdateResult);

				Criteria newNodeCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(args.getBeforeId(), args.getParentId());
				Query newNodequery = Query.query(newNodeCriteria);

				Map<String, TenantAppDepartmentMongodb> newDepartmentIdMap = mongoTemplate.find(newNodequery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT).stream().collect(Collectors.toMap(TenantAppDepartmentMongodb::getDepartmentId, x -> x));

				// 移动后的父节点
				TenantAppDepartmentMongodb newParentDepartment = newDepartmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentMongodb newBeforeDepartment = beforeDepartment == null ? null : newDepartmentIdMap.get(args.getBeforeId());

				// 扩容
				int startAddNum = Optional.ofNullable(newBeforeDepartment).map(TenantAppDepartmentMongodb::getLeftNo).orElse(newParentDepartment.getRightNo());
				// 扩容左值
				Criteria addNodeLeftCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(startAddNum);
				Query addNodeLeftQuery = Query.query(addNodeLeftCriteria);

				Update addNodeLeftUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.LEFT_NO, moveNum)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateMulti(addNodeLeftQuery, addNodeLeftUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);

				// 扩容右值
				Criteria addNodeRightCriteria = Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).gte(startAddNum);
				Query addNodeRightQuery = Query.query(addNodeRightCriteria);
				Update addNodeRightUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.RIGHT_NO, moveNum)
					.currentDate(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult addNodeRightUpdateResult = mongoTemplate.updateMulti(addNodeRightQuery, addNodeRightUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.info("addNodeRightUpdateResult: {}", addNodeRightUpdateResult);

				newDepartmentIdMap = mongoTemplate.find(newNodequery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT).stream().collect(Collectors.toMap(TenantAppDepartmentMongodb::getDepartmentId, x -> x));

				// 移动后的父节点
				TenantAppDepartmentMongodb newParentDepartment2 = newDepartmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				TenantAppDepartmentMongodb newBeforeDepartment2 = beforeDepartment == null ? null : newDepartmentIdMap.get(args.getBeforeId());


				int leftNo = moveDepartment.getLeftNo();
				moveNodes.forEach(x -> {
					if (newBeforeDepartment2 == null) {
						// 使用右基点
						x.setLeftNo(x.getLeftNo() - leftNo + newParentDepartment2.getRightNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newParentDepartment2.getRightNo() - moveNum);
					} else {
						// 使用左基点
						x.setLeftNo(x.getLeftNo() - leftNo + newBeforeDepartment2.getLeftNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newBeforeDepartment2.getLeftNo() - moveNum);
					}
					if (x.getDepartmentId().equals(args.getMoveId())) {
						x.setParentId(args.getParentId());
					}
					x.setDepth(x.getDepth() + parentDepartment.getDepth() + 1 - moveDepartment.getDepth());

					x.getMetadata().setUpdateTime(LocalDateTime.now());
				});
				mongoTemplate.insert(moveNodes, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("moveDepartment", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动部门失败");
			}
		});
	}

	/**
	 * 部门删除，包含删除子级部门
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_department:delete_tenant_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deleteTenantAppDepartment(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated DeleteDepartmentUserArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 判断部门合法
				Query departmentQuery = Query.query(
					Criteria
						.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
						.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(args.getDepartmentId())
				);

				TenantAppDepartmentMongodb deleteDepartment = mongoTemplate.findOne(departmentQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				if (deleteDepartment == null) {
					throw new ConflictBusinessException(String.format("删除部门失败（部门不存在 %s）", args.getDepartmentId()));
				}
				if (deleteDepartment.isRoot()) {
					throw new ConflictBusinessException("删除部门失败（根节点不能删除）");
				}

				// 判断不存在子部门
				Query deleteDepartmentQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gte(deleteDepartment.getLeftNo())
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).lte(deleteDepartment.getRightNo())
				);
				int inc = -(deleteDepartment.getRightNo() - deleteDepartment.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("删除部门失败（部门含有子级部门，请先删除子级部门后在操作）");
				}

				// 判断不存在用户
				List<BasicTenantAppUser> existsUserList = tenantAppDepartmentCommonService.existsUserList(tenantId, appId, args.getDepartmentId());
				if (!existsUserList.isEmpty()) {
					String userNames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException(String.format("删除部门失败（部门已被用户[%s]使用，请先移出所有人员）", userNames));
				}


				// 更新左值
				Query otherMenuLeftQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.LEFT_NO).gt(deleteDepartment.getLeftNo())
				);
				Update otherMenuLeftUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.LEFT_NO, inc)
					.set(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 更新右值
				Query otherMenuRightQuery = Query.query(Criteria
					.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppDepartmentMongodb.FIELD.RIGHT_NO).gt(deleteDepartment.getRightNo())
				);
				Update otherMenuRightUpdate = new Update()
					.inc(TenantAppDepartmentMongodb.FIELD.RIGHT_NO, inc)
					.set(TenantAppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 删除目标
				List<TenantAppDepartmentMongodb> deleteDepartmentList = mongoTemplate.findAllAndRemove(deleteDepartmentQuery, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				// 移动到删除影子表
				if (!deleteDepartmentList.isEmpty()) {
					mongoTemplate.insert(deleteDepartmentList, MongodbConstants.DeletedCollection.TENANT_APP_DEPARTMENT);
				}

				// 移动其他菜单左右值

				UpdateResult otherDepartmentLeftUpdateResult = mongoTemplate.updateMulti(otherMenuLeftQuery, otherMenuLeftUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.debug("OtherTenantAppDepartmentLeftUpdateResult: {}", otherDepartmentLeftUpdateResult);
				UpdateResult otherMenuLeftUpdateResult = mongoTemplate.updateMulti(otherMenuRightQuery, otherMenuRightUpdate, TenantAppDepartmentMongodb.class, MongodbConstants.Collection.TENANT_APP_DEPARTMENT);
				log.debug("OtherTenantAppDepartmentRightUpdateResult: {}", otherMenuLeftUpdateResult);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeTenantAppDepartment", e);
				throw new ConflictBusinessException("删除部门失败");
			}

		});
	}

	/**
	 * 构建查询条件
	 *
	 * @param tenantId 企业ID
	 * @param appId    应用ID
	 * @param args     查询参数
	 * @return criteria
	 */
	protected Criteria buildCriteria(String tenantId, String appId, @NotNull GetDepartmentArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppDepartmentMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppDepartmentMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getDepartmentIds()).filter(x -> !x.isEmpty()).ifPresent(ids -> criteria.and(TenantAppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(ids));
		Optional.ofNullable(args.getParentId()).filter(x -> !x.isBlank()).ifPresent(parent -> criteria.and(TenantAppDepartmentMongodb.FIELD.PARENT_ID).is(parent));

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


	public List<MetadataTenantAppDepartment> getTenantAppDepartmentList(String tenantId, String appId, List<TenantAppDepartmentMongodb> ms, Map<String, String> extensionMap) {
		final TenantAppDepartmentExtension extension = Optional.of(extensionMap.getOrDefault(CairoAuthExtensionConstants.DEPARTMENT, TenantAppDepartmentExtension.ALL.name()))
			.map(TenantAppDepartmentExtension::valueOf)
			.orElse(TenantAppDepartmentExtension.ALL);

		Map<String, TenantAppUser> metadataUserMap = new HashMap<>();
		if (extension.fields().contains(TenantAppDepartmentField.METADATA)) {
			Set<String> userIds = CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(ms.stream().map(TenantAppDepartmentMongodb::getMetadata).collect(Collectors.toList()));
			if (!userIds.isEmpty()) {
				metadataUserMap.putAll(tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds));
			}
		}
		return ms.stream().map(x -> TenantAppDepartmentConverter.convert(x, metadataUserMap, extension)).collect(Collectors.toList());
	}

}
