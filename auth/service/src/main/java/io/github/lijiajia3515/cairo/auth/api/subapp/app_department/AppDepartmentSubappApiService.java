package io.github.lijiajia3515.cairo.auth.api.subapp.app_department;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_department.AppDepartmentConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.MetadataAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.PathAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.TreeNodeAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.CreateAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.DeleteAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.GetAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.GetAppDepartmentTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.ModifyAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_department.MoveAppDepartmentArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
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
 * [subapp_user/api] app department service
 */
@Slf4j
@Validated
@Component
public class AppDepartmentSubappApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppDepartmentCommonService appDepartmentCommonService;
	private final AppUserCommonService appUserCommonService;
	private final CairoAuthAppUserService cairoAuthAppUserService;

	public AppDepartmentSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											 TransactionTemplate transactionTemplate,
											 AppDepartmentCommonService appDepartmentCommonService,
											 AppUserCommonService appUserCommonService, CairoAuthAppUserService cairoAuthAppUserService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appDepartmentCommonService = appDepartmentCommonService;
		this.appUserCommonService = appUserCommonService;
		this.cairoAuthAppUserService = cairoAuthAppUserService;
	}


	/**
	 * 应用部门查询
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 应用部门列表
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
	public List<MetadataAppDepartment> getAppDepartmentList(@Valid @NotNull String appId, @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(Sort.by(Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<AppDepartmentMongodb> dms = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		return getAppDepartmentList(appId, dms, args.getExtension());
	}

	/**
	 * 查找
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 应用部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:get_path_app_department_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	List<PathAppDepartment> getPathAppDepartmentList(@Valid @NotNull String appId, @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);
		final List<AppDepartmentMongodb> dms = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		return getPathAppDepartmentList(appId, dms);
	}

	/**
	 * 查找
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 应用部门查询
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:get_path_app_department_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	Page<PathAppDepartment> getPathAppDepartmentPageList(@Valid @NotNull String appId, @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);

		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = mongoTemplate.count(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);

		final List<AppDepartmentMongodb> dms = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		List<PathAppDepartment> pathAppDepartmentList = getPathAppDepartmentList(appId, dms);
		return new Page<>(args, pathAppDepartmentList, total);
	}

	/**
	 * 获取应用部门分页列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return 应用部门查询
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
	Page<MetadataAppDepartment> getAppDepartmentPageList(@Valid String appId, @Valid @Validated GetAppDepartmentArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query
			.query(criteria)
			.with(
				Sort.by(
					Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
				)
			);

		long total = mongoTemplate.count(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		query.with(args.pageable());
		query.with(
			Sort.by(
				Sort.Order.asc(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME)
			)
		);
		List<AppDepartmentMongodb> app_departmentMongodbList = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		List<MetadataAppDepartment> ds = getAppDepartmentList(appId, app_departmentMongodbList, args.getExtension());

		return new Page<>(args, ds, total);
	}

	/**
	 * 获取应用部门树形列表
	 *
	 * @param appId appI
	 * @return 应用部门 list
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:get_app_department_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	TreeNodeAppDepartment getAppDepartmentTree(@NotNull String appId, @Validated GetAppDepartmentTreeArgs args) {
		// 确认查询节点，默认根节点
		String parentId = Optional.ofNullable(args).map(GetAppDepartmentTreeArgs::getParentId)
			.orElse(appDepartmentCommonService.getRootAppDepartment(appId).getDepartmentId());

		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId);
		Query parentQuery = Query.query(Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(parentId));
		AppDepartmentMongodb parentAppDepartment = readMongoTemplate.findOne(parentQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
		if (parentAppDepartment != null) {
			criteria
				.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(parentAppDepartment.getLeftNo())
				.and(AppDepartmentMongodb.FIELD.RIGHT_NO).lte(parentAppDepartment.getRightNo());
		}

		Query query = Query.query(criteria);
		query.with(
			Sort.by(
				Sort.Order.asc(AppDepartmentMongodb.FIELD.DEPTH),
				Sort.Order.asc(AppDepartmentMongodb.FIELD.LEFT_NO)
			)
		);
		List<AppDepartmentMongodb> appDepartmentMongodbList = readMongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

		TreeNodeAppDepartment rootNode = AppDepartmentConverter.treeConvert(parentAppDepartment);
		List<TreeNodeAppDepartment> nodes = appDepartmentMongodbList.stream().map(AppDepartmentConverter::treeConvert).collect(Collectors.toList());
		List<TreeNodeAppDepartment> subNodes = Tree2Converter.build(nodes, parentId);
		rootNode.subs(subNodes);
		return rootNode;
	}

	Optional<PathAppDepartment> getAppDepartmentByAppDepartmentId(@Valid @NotNull String appId, @Valid @NotNull String departmentId) {
		Criteria lastCriteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
			.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(departmentId);

		Query lastQuery = Query.query(lastCriteria);
		return Optional.ofNullable(readMongoTemplate.findOne(lastQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT))
			.flatMap(last -> getPathAppDepartmentList(appId, Collections.singletonList(last)).stream().findFirst());
	}

	/**
	 * 创建应用部门
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:create_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createAppDepartment(@Valid @NotNull String appId, @Validated CreateAppDepartmentArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				String parentId = Optional.ofNullable(args.getParentId()).orElse(appDepartmentCommonService.getRootAppDepartment(appId).getDepartmentId());
				Query parentQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(parentId)
				);
				AppDepartmentMongodb parentAppDepartment = mongoTemplate.findOne(parentQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

				if (parentAppDepartment == null) {
					throw new ConflictBusinessException("父节点不存在");
				}

				// find brother
				Query brotherNodeQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.PARENT_ID).is(parentId)
				);
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(AppDepartmentMongodb.FIELD.LEFT_NO)));

				List<AppDepartmentMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				Optional<AppDepartmentMongodb> afterNode = brotherNodes.stream().filter(a -> a.getDepartmentId().equals(args.getBeforeId())).findFirst();
				int position;
				int left;
				int right;
				if (brotherNodes.isEmpty() || afterNode.isEmpty()) {
					position = parentAppDepartment.getRightNo();
					left = parentAppDepartment.getRightNo();
					right = parentAppDepartment.getRightNo() + 1;
				} else {
					position = afterNode.get().getLeftNo();
					left = afterNode.get().getLeftNo();
					right = afterNode.get().getLeftNo() + 1;
				}


				// 左值扩容
				Query leftParentQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(position)
				);

				Update leftParentUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.LEFT_NO, 2)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult leftParentUpdateResult = mongoTemplate.updateMulti(leftParentQuery, leftParentUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.debug("leftParentUpdateResult: {}", leftParentUpdateResult);


				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).gte(position)
				);
				Update update = new Update()
					.inc(AppDepartmentMongodb.FIELD.RIGHT_NO, 2)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult rightParentUpdateResult = mongoTemplate.updateMulti(rightParentQuery, update, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.debug("rightParentUpdateResult: {}", rightParentUpdateResult);

				// 插入部门
				AppDepartmentMongodb insertAppDepartmentMongodb = AppDepartmentMongodb.builder()
					.appId(appId)
					.parentId(parentAppDepartment.getDepartmentId())
					.root(false)
					.departmentId(CoreConstants.SNOWFLAKE.nextIdStr())
					.departmentName(args.getDepartmentName())
					.remark(args.getRemark())
					.leftNo(left)
					.rightNo(right)
					.depth(parentAppDepartment.getDepth() + 1)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				AppDepartmentMongodb insertedAppDepartmentMongodb = mongoTemplate.insert(insertAppDepartmentMongodb, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("inserted app department: {}", insertedAppDepartmentMongodb.getDepartmentId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("createAppDepartment", e);
				throw new ConflictBusinessException("创建部门失败");
			}
		});
	}

	/**
	 * 修改应用部门信息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:modify_app_department_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "modify_app_department_info", keys = {"#args.departmentId"})
	public void modifyAppDepartmentInfo(@Valid @NotNull String appId, @Validated ModifyAppDepartmentArgs args) {
		transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(args.getDepartmentId());
				Query query = Query.query(criteria);
				AppDepartmentMongodb appDepartmentMongodb = mongoTemplate.findOne(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);

				if (appDepartmentMongodb == null) {
					throw new ConflictBusinessException("更新部门失败(应用部门不存在)");
				}

				Optional.ofNullable(args.getDepartmentName()).ifPresent(appDepartmentMongodb::setDepartmentName);
				Optional.ofNullable(args.getRemark()).ifPresent(appDepartmentMongodb::setRemark);
				appDepartmentMongodb.getMetadata().setUpdateUserId(CairoSecurityContextHolder.getSubappUserId());
				appDepartmentMongodb.getMetadata().setUpdateTime(LocalDateTime.now());
				mongoTemplate.save(appDepartmentMongodb, MongodbConstants.Collection.APP_DEPARTMENT);
				return appDepartmentMongodb;
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAppDepartmentInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改部门失败");
			}
		});

		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 部门移动
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:move_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "move_app_department", keys = {"#appId"})
	public void moveAppDepartment(@Valid @NotNull String appId, @Validated MoveAppDepartmentArgs args) {
		// 	1. 删除移动的节点，
		// 	2. 缩容
		//  3. 扩容
		//  4. 插入删移动的节点（更新leftNo,rightNo)
		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 先查询三个节点信息
				Criteria criteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(args.getMoveId(), args.getBeforeId(), args.getParentId());
				Query query = Query.query(criteria);
				Map<String, AppDepartmentMongodb> app_departmentIdMap = mongoTemplate.find(query, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT).stream().collect(Collectors.toMap(AppDepartmentMongodb::getDepartmentId, x -> x));
				// 移动的节点
				AppDepartmentMongodb moveAppDepartment = app_departmentIdMap.get(args.getMoveId());
				// 移动后的父节点
				AppDepartmentMongodb parentAppDepartment = app_departmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				AppDepartmentMongodb beforeAppDepartment = app_departmentIdMap.get(args.getBeforeId());
				if (moveAppDepartment == null || parentAppDepartment == null) {
					throw new ParamsErrorBusinessException("moveId is null or parentId 错误");
				}

				if (parentAppDepartment.getLeftNo() >= moveAppDepartment.getLeftNo() && parentAppDepartment.getRightNo() <= moveAppDepartment.getRightNo()) {
					throw new ParamsErrorBusinessException("parentId 不能设置为移动节点的子节点");
				}

				// 容错beforeId错误的问题，默认移动到最后
				if (beforeAppDepartment != null && !beforeAppDepartment.getParentId().equals(parentAppDepartment.getDepartmentId())) {
					beforeAppDepartment = null;
				}

				// 查询后删除移动的节点
				Criteria moveNodeCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(moveAppDepartment.getLeftNo())
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).lte(moveAppDepartment.getRightNo());
				Query moveNodeQuery = Query.query(moveNodeCriteria);
				moveNodeQuery.with(Sort.by(Sort.Order.asc(AppDepartmentMongodb.FIELD.LEFT_NO)));
				List<AppDepartmentMongodb> moveNodes = mongoTemplate.findAllAndRemove(moveNodeQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				// 移动的数值
				int moveNum = moveNodes.size() * 2;

				// 缩容左值
				Criteria subNodeLeftCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(moveAppDepartment.getRightNo() + 1);
				Query subNodeLeftQuery = Query.query(subNodeLeftCriteria);
				Update subNodeLeftUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.LEFT_NO, -moveNum)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeLeftUpdateResult = mongoTemplate.updateMulti(subNodeLeftQuery, subNodeLeftUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("subNodeLeftUpdateResult: {}", subNodeLeftUpdateResult);

				// 缩容右值
				Criteria subNodeRightCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).gte(moveAppDepartment.getRightNo() + 1);
				Query subNodeRightQuery = Query.query(subNodeRightCriteria);
				Update subNodeRightUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.RIGHT_NO, -moveNum)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult subNodeRightUpdateResult = mongoTemplate.updateMulti(subNodeRightQuery, subNodeRightUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("subNodeRightUpdateResult: {}", subNodeRightUpdateResult);

				Criteria newNodeCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(args.getBeforeId(), args.getParentId());
				Query newNodequery = Query.query(newNodeCriteria);

				Map<String, AppDepartmentMongodb> newAppDepartmentIdMap = mongoTemplate.find(newNodequery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT).stream().collect(Collectors.toMap(AppDepartmentMongodb::getDepartmentId, x -> x));

				// 移动后的父节点
				AppDepartmentMongodb newParentAppDepartment = newAppDepartmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				AppDepartmentMongodb newBeforeAppDepartment = beforeAppDepartment == null ? null : newAppDepartmentIdMap.get(args.getBeforeId());

				// 扩容
				int startAddNum = Optional.ofNullable(newBeforeAppDepartment).map(AppDepartmentMongodb::getLeftNo).orElse(newParentAppDepartment.getRightNo());
				// 扩容左值
				Criteria addNodeLeftCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(startAddNum);
				Query addNodeLeftQuery = Query.query(addNodeLeftCriteria);

				Update addNodeLeftUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.LEFT_NO, moveNum)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateMulti(addNodeLeftQuery, addNodeLeftUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);

				// 扩容右值
				Criteria addNodeRightCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).gte(startAddNum);
				Query addNodeRightQuery = Query.query(addNodeRightCriteria);
				Update addNodeRightUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.RIGHT_NO, moveNum)
					.currentDate(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult addNodeRightUpdateResult = mongoTemplate.updateMulti(addNodeRightQuery, addNodeRightUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.info("addNodeRightUpdateResult: {}", addNodeRightUpdateResult);

				newAppDepartmentIdMap = mongoTemplate.find(newNodequery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT).stream().collect(Collectors.toMap(AppDepartmentMongodb::getDepartmentId, x -> x));

				// 移动后的父节点
				AppDepartmentMongodb newParentAppDepartment2 = newAppDepartmentIdMap.get(args.getParentId());
				// 移动后的左边节点
				AppDepartmentMongodb newBeforeAppDepartment2 = beforeAppDepartment == null ? null : newAppDepartmentIdMap.get(args.getBeforeId());


				int leftNo = moveAppDepartment.getLeftNo();
				moveNodes.forEach(x -> {
					if (newBeforeAppDepartment2 == null) {
						// 使用右基点
						x.setLeftNo(x.getLeftNo() - leftNo + newParentAppDepartment2.getRightNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newParentAppDepartment2.getRightNo() - moveNum);
					} else {
						// 使用左基点
						x.setLeftNo(x.getLeftNo() - leftNo + newBeforeAppDepartment2.getLeftNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newBeforeAppDepartment2.getLeftNo() - moveNum);
					}
					if (x.getDepartmentId().equals(args.getMoveId())) {
						x.setParentId(args.getParentId());
					}
					x.setDepth(x.getDepth() + parentAppDepartment.getDepth() + 1 - moveAppDepartment.getDepth());

					x.getMetadata().setUpdateTime(LocalDateTime.now());
				});
				mongoTemplate.insert(moveNodes, MongodbConstants.Collection.APP_DEPARTMENT);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("moveAppDepartment", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动应用部门失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 应用部门删除，包含删除子级应用部门
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@BizLog(
		bizId = "app_department:delete_app_department",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	@Lock4j(name = "delete_app_department", keys = {"#args.departmentId"})
	public void deleteAppDepartment(@Valid @NotNull String appId, @Validated DeleteAppDepartmentArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				List<BasicAppUser> existsUserList = appDepartmentCommonService.existsUserList(appId, args.getDepartmentId());
				if (!existsUserList.isEmpty()) {
					String userNames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("应用部门已被用户[" + userNames + "]使用，不允许删除");
				}
				Query appDepartmentQuery = Query.query(
					Criteria
						.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
						.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).is(args.getDepartmentId())
				);

				AppDepartmentMongodb deleteAppDepartment = mongoTemplate.findOne(appDepartmentQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				if (deleteAppDepartment == null) {
					throw new ConflictBusinessException("应用部门不存在，删除失败");
				}
				if (deleteAppDepartment.isRoot()) {
					throw new ConflictBusinessException("根节点不能删除");
				}

				Query deleteMenuQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gte(deleteAppDepartment.getLeftNo())
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).lte(deleteAppDepartment.getRightNo())
				);
				int inc = -(deleteAppDepartment.getRightNo() - deleteAppDepartment.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("应用部门含有子级应用部门，请先删除子级应用部门后在操作");
				}

				// 更新左值
				Query otherMenuLeftQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.LEFT_NO).gt(deleteAppDepartment.getLeftNo())
				);
				Update otherMenuLeftUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.LEFT_NO, inc)
					.set(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 更新右值
				Query otherMenuRightQuery = Query.query(Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.RIGHT_NO).gt(deleteAppDepartment.getRightNo())
				);
				Update otherMenuRightUpdate = new Update()
					.inc(AppDepartmentMongodb.FIELD.RIGHT_NO, inc)
					.set(AppDepartmentMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

				// 删除目标
				List<AppDepartmentMongodb> deleteAppDepartmentList = mongoTemplate.findAllAndRemove(deleteMenuQuery, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				// 移动到删除影子表
				if (!deleteAppDepartmentList.isEmpty()) {
					mongoTemplate.insert(deleteAppDepartmentList, MongodbConstants.DeletedCollection.APP_DEPARTMENT);
				}

				// 移动其他菜单左右值

				UpdateResult otherAppDepartmentLeftUpdateResult = mongoTemplate.updateMulti(otherMenuLeftQuery, otherMenuLeftUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.debug("OtherAppDepartmentLeftUpdateResult: {}", otherAppDepartmentLeftUpdateResult);
				UpdateResult otherMenuLeftUpdateResult = mongoTemplate.updateMulti(otherMenuRightQuery, otherMenuRightUpdate, AppDepartmentMongodb.class, MongodbConstants.Collection.APP_DEPARTMENT);
				log.debug("OtherAppDepartmentRightUpdateResult: {}", otherMenuLeftUpdateResult);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeAppDepartment", e);
				throw new ConflictBusinessException("删除应用部门失败");
			}

		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 构建查询条件
	 *
	 * @param appId 应用id
	 * @param args  查询参数
	 * @return criteria
	 */
	protected Criteria buildCriteria(String appId, @NotNull GetAppDepartmentArgs args) {
		Criteria criteria = Criteria
			.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId);

		Optional.ofNullable(args.getDepartmentIds()).filter(x -> !x.isEmpty()).ifPresent(ids -> criteria.and(AppDepartmentMongodb.FIELD.DEPARTMENT_ID).in(ids));
		Optional.ofNullable(args.getParentId()).filter(x -> !x.isBlank()).ifPresent(parent -> criteria.and(AppDepartmentMongodb.FIELD.PARENT_ID).is(parent));

		return criteria;
	}

	/**
	 * 查询
	 *
	 * @param appId                 appId
	 * @param lastAppDepartmentList 应用部门列表
	 * @return 1
	 */
	@NewSpan
	protected List<PathAppDepartment> getPathAppDepartmentList(String appId, List<AppDepartmentMongodb> lastAppDepartmentList) {
		List<String> app_departmentIds = lastAppDepartmentList.stream().map(AppDepartmentMongodb::getDepartmentId).distinct().collect(Collectors.toList());
		// 获取应用部门列表中的父级应用部门列表并从已有的数据中去除重复
		Set<AppDepartmentMongodb> noSelectParentAppDepartmentList = lastAppDepartmentList.stream().filter(x -> !app_departmentIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<AppDepartmentMongodb> parentAppDepartmentMongodbList = Optional.of(noSelectParentAppDepartmentList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentAppDepartmentCriteria = Criteria
					.where(AppDepartmentMongodb.FIELD.APP_ID).is(appId)
					.and(AppDepartmentMongodb.FIELD.ROOT).is(false)
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
				List<AppDepartmentMongodb> departmentMongodbList = new ArrayList<>();
				allAppDepartmentList.forEach(app_department -> {
					if (app_department.getLeftNo() <= d.getLeftNo() && app_department.getRightNo() >= d.getRightNo()) {
						departmentMongodbList.add(app_department);
					}
				});

				return PathAppDepartment.builder()
					.departmentIds(departmentMongodbList.stream().map(AppDepartmentMongodb::getDepartmentId).collect(Collectors.toList()))
					.departmentNames(departmentMongodbList.stream().map(AppDepartmentMongodb::getDepartmentName).collect(Collectors.toList()))
					.depth(departmentMongodbList.size())
					.build();
			})
			.collect(Collectors.toList());
	}


	public List<MetadataAppDepartment> getAppDepartmentList(String appId, List<AppDepartmentMongodb> ms, Map<String, String> extensionMap) {
		final AppDepartmentExtension extension = Optional.of(extensionMap.getOrDefault(CairoAuthExtensionConstants.APP_DEPARTMENT, AppDepartmentExtension.ALL.name()))
			.map(AppDepartmentExtension::valueOf)
			.orElse(AppDepartmentExtension.ALL);

		Map<String, AppUser> metadataUserMap = new HashMap<>();
		if (extension.fields().contains(AppDepartmentField.METADATA)) {
			Set<String> userIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppDepartmentMongodb::getMetadata).collect(Collectors.toList()));
			if (!userIds.isEmpty()) {
				metadataUserMap.putAll(appUserCommonService.getAppUserMapByAppUserIds(appId, userIds));
			}
		}
		return ms.stream().map(x -> AppDepartmentConverter.convert(x, metadataUserMap, extension)).collect(Collectors.toList());
	}

}
