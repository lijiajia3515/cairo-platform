package io.github.lijiajia3515.cairo.auth.api.tenant_subapp.tenant_app_role;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleCommonService;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleField;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.CreateRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.DeleteRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.DeleteTenantRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.GetRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_role.ModifyRoleStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.BasicTenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * [tenant_app_user/api] tenant app role service
 */
@Slf4j
@Validated
@Component
public class TenantAppRoleTenantSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final TenantAppUserCommonService tenantAppUserCommonService;
	private final TenantAppRoleCommonService tenantAppRoleCommonService;

	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_KEY = "tenant_app_role";

	private final SerialService serialService;



	public TenantAppRoleTenantSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												TransactionTemplate transactionTemplate,
												TenantAppUserCommonService userClientService,
												TenantAppRoleCommonService tenantAppRoleCommonService,
												SerialService serialService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.tenantAppUserCommonService = userClientService;
		this.tenantAppRoleCommonService = tenantAppRoleCommonService;
		this.serialService = serialService;
	}


	/**
	 * get role list
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 * @return role page
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role:get_tenant_app_role_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataTenantAppRole> getTenantAppRoleList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetRoleArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));
		final List<TenantAppRoleMongodb> rms = readMongoTemplate.find(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
		return getTenantAppRoleList(tenantId, appId, rms, args.getExtension());
	}


	/**
	 * get role page list
	 *
	 * @param tenantId 租户id
	 * @param appId    应用id
	 * @param args     args
	 * @return role page
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role:get_tenant_app_role_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	Page<MetadataTenantAppRole> getTenantAppRolePageList(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated GetRoleArgs args) {
		Criteria criteria = buildCriteria(tenantId, appId, args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<TenantAppRoleMongodb> rms = readMongoTemplate.find(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
		final List<MetadataTenantAppRole> rs = getTenantAppRoleList(tenantId, appId, rms, args.getExtension());
		return new Page<>(args, rs, total);
	}

	@NewSpan
	@BizLog(
		bizId = "tenant_app_role:get_tenant_app_role_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "roleId", value = "#roleId"),
		}
	)
	Optional<TenantAppRole> getTenantAppRoleInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String roleId) {
		Query query = Query.query(Criteria
			.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(roleId));
		TenantAppRoleMongodb role = readMongoTemplate.findOne(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
		return Optional.ofNullable(role).map(TenantAppRoleConverter::convert);
	}

	@NewSpan
	List<MenuNode> getTenantAppRolePermission(@Valid String tenantId, @Valid @NotNull String appId, @Valid @NotNull String roleId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion) {
		Query menuRootQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(ROOT_ID)
			.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false)
		);
		MenuMongodb parentResource = mongoTemplate.findOne(menuRootQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		if (parentResource == null) {
			return null;
		}

		Criteria menuCriteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.LEFT_NO).gte(parentResource.getLeftNo())
			.and(MenuMongodb.FIELD.RIGHT_NO).lte(parentResource.getRightNo())
			.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false);
		Query menuQuery = Query.query(menuCriteria);
		menuQuery.with(Sort.by(MenuMongodb.FIELD.DEPTH, MenuMongodb.FIELD.LEFT_NO));

		List<MenuMongodb> menus = mongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria rolePermissionCriteria = Criteria
			.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(roleId)
			.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Query rolePermissionQuery = Query.query(rolePermissionCriteria);
		Set<String> rolePermissionIds = Optional.ofNullable(mongoTemplate.findOne(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION))
			.map(TenantAppRolePermissionMongodb::getPermissionIds)
			.<Set<String>>map(HashSet::new)
			.orElse(Collections.emptySet());


		Set<String> menuIds = menus.stream().map(MenuMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, List<PermissionMongodb>> permissionMap = Optional.of(menuIds).filter(x -> !x.isEmpty()).map(mIds -> {
			Query permissionQuery = Query.query(Criteria
				.where(PermissionMongodb.FIELD.APP_ID).is(appId)
				.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
				.and(PermissionMongodb.FIELD.MENU_ID).in(mIds)
				.and(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(false)
				.and(PermissionMongodb.FIELD.HIDDEN_PERMISSION).is(false)
			);
			permissionQuery.with(Sort.by(
				Sort.Order.asc(PermissionMongodb.FIELD.MENU_ID),
				Sort.Order.asc(PermissionMongodb.FIELD.SORT))
			);
			return readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION).stream()
				.collect(Collectors.groupingBy(PermissionMongodb::getMenuId));

		}).orElse(Collections.emptyMap());

		List<MenuNode> nodes = menus.stream()
			.map(x -> MenuNode.builder()
				.menuId(x.getMenuId())
				.parentId(x.getParentId())
				.menuName(x.getMenuName())
				.path(x.getPath())
				.component(x.getComponent())
				.icon(x.getIcon())
				.hiddenMenu(x.isHiddenMenu())
				.depth(x.getDepth())
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.defaultPermission(e.getDefaultPermission())
							.hiddenPermission(e.getHiddenPermission())
							.type(e.getType())
							.isSelected(rolePermissionIds.contains(e.getPermissionId()))
							.icon(e.getIcon())
							.sort(e.getSort())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, ROOT_ID);
	}

	/**
	 * 修改角色
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_app_role:create_tenant_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createTenantAppRole(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated CreateRoleArgs args) {
		TenantAppRoleMongodb insertedRole = transactionTemplate.execute(status -> {
			try {
				TenantAppRoleMongodb role = TenantAppRoleMongodb.builder()
					.tenantId(tenantId)
					.appId(appId)
					.roleId(serialService.nextStr(SERIAL_NAMESPACE, SERIAL_KEY,1,2001))
					.roleName(args.getRoleName())
					.remark(args.getRemark())
					.enabled(true)
					.sort(System.currentTimeMillis())
					.metadata(TenantAppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(role, MongodbConstants.Collection.TENANT_APP_ROLE);
			} catch (Exception e) {
				log.info("createRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建角色失败");
			}
		});
		if (insertedRole == null) {
			throw new ConflictBusinessException("创建角色失败");
		}
	}

	/**
	 * 修改角色信息
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_info", keys = {"#tenantId", "#appId", "#args.roleId"})
	@BizLog(
		bizId = "tenant_app_role:modify_tenant_app_role_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRoleInfo(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyRoleInfoArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId())
				);
				Update update = new Update();
				Optional.ofNullable(args.getRoleName()).ifPresent(name -> update.set(TenantAppRoleMongodb.FIELD.ROLE_NAME, name));
				Optional.ofNullable(args.getRemark()).ifPresent(remark -> update.set(TenantAppRoleMongodb.FIELD.REMARK, remark));

				update.set(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改角色失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改角色失败");
			}
		});
	}

	/**
	 * 修改角色权限
	 *
	 * @param tenantId 租户id
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_permission", keys = {"#tenantId", "#appId", "#args.endpointId", "#args.subappId", "#args.subappVersion", "#args.roleId"})
	@BizLog(
		bizId = "tenant_app_role:modify_tenant_app_role_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRolePermission(@Valid @NotNull String tenantId, @Valid @NotNull String appId,  @Validated ModifyRolePermissionArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query rolePermissionQuery = Query.query(Criteria
					.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(args.getRoleId())
					.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
					.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion())
				);
				boolean exists = mongoTemplate.exists(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				if (exists) {
					Update update = new Update();
					update.set(TenantAppRolePermissionMongodb.FIELD.PERMISSION_IDS, args.getPermissionIds());
					update.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
					update.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult rolePermissionUpdateResult = mongoTemplate.updateFirst(rolePermissionQuery, update, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
					if (rolePermissionUpdateResult.getModifiedCount() < 1) {
						throw new ConflictBusinessException("修改角色权限失败");
					}
				} else {
					TenantAppRolePermissionMongodb insertRolePermission = TenantAppRolePermissionMongodb.builder()
						.tenantId(tenantId)
						.appId(appId)
						.endpointId(args.getEndpointId())
						.subappId(args.getSubappId())
						.subappVersion(args.getSubappVersion())
						.roleId(args.getRoleId())
						.permissionIds(args.getPermissionIds())
						.metadata(TenantAppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getTenantAppUserId())
							.updateUserId(CairoSecurityContextHolder.getTenantAppUserId())
							.build())
						.build();
					mongoTemplate.insert(insertRolePermission, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				}
				Criteria roleCriteria = Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId());
				Query roleQuery = Query.query(roleCriteria);
				Update roleUpdate = Update.update(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				roleUpdate.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult roleUpdateResult = mongoTemplate.updateFirst(roleQuery, roleUpdate, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
				if (roleUpdateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改角色权限失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyRolePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改角色权限失败");
			}
		});
	}

	/**
	 * 修改角色状态
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "modify_tenant_app_role_status", keys = {"#tenantId", "#appId", "#args.roleId"})
	@BizLog(
		bizId = "tenant_app_role:modify_tenant_app_role_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyTenantAppRoleStatus(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated ModifyRoleStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId())
				);
				Update update = new Update();
				update.set(TenantAppRoleMongodb.FIELD.ENABLED, args.getEnabled());

				update.set(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				update.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改角色状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyRoleStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改角色状态失败");
			}
		});
	}

	/**
	 * 删除
	 *
	 * @param tenantId tenantId
	 * @param appId    appId
	 * @param args     args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_app_role", keys = {"#tenantId", "#appId", "#args.roleIds"})
	@BizLog(
		bizId = "tenant_app_role:delete_tenant_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteTenantAppRole(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Validated DeleteRoleArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				List<BasicTenantAppUser> existsUserList = tenantAppRoleCommonService.existsUserList(tenantId, appId, args.getRoleIds());
				if (!existsUserList.isEmpty()) {
					String nicknames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("角色被用户[" + nicknames + "]使用，不允许删除");
				}

				Criteria roleCriteria = Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).in(args.getRoleIds());
				Query roleQuery = Query.query(roleCriteria);

				Update roleUpdate = new Update();
				roleUpdate.set(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				roleUpdate.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult roleUpdateResult = mongoTemplate.updateMulti(roleQuery, roleUpdate, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
				List<TenantAppRoleMongodb> deletedTenantRoleMongodbList = mongoTemplate.findAllAndRemove(roleQuery, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
				if (!deletedTenantRoleMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedTenantRoleMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE);
				}

				Criteria rolePermissionCriteria = Criteria
					.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(args.getRoleIds());
				Query rolePermissionQuery = Query.query(rolePermissionCriteria);
				Update rolePermissionUpdate = new Update();
				rolePermissionUpdate.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				rolePermissionUpdate.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult rolePermissionUpdateResult = mongoTemplate.updateMulti(rolePermissionQuery, rolePermissionUpdate, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				List<TenantAppRolePermissionMongodb> deleteRolePermissionMongodbList = mongoTemplate.findAllAndRemove(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				if (!deleteRolePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteRolePermissionMongodbList, MongodbConstants.DeletedCollection.TENANT_APP_ROLE_PERMISSION);
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除角色失败");
			}
		});
	}

	/**
	 * 查看企业角色子应用版本
	 *
	 * @param appId         appId
	 * @param endpointId endpointId
	 * @param roleId        roleId
	 * @param subappId     subappId
	 */
	@NewSpan
	@BizLog(
		bizId = "tenant_role_subapp_version:get_tenant_role_subapp_version",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "roleId", value = "#roleId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
		}
	)
	public List<TenantAppRoleSubappVersion> getTenantRoleSubappVersion(String tenantId, String appId, String endpointId, String roleId, String subappId) {
		//查询应用角色权限
		Criteria tenantRolePermissionCriteria = Criteria
			.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(roleId)
			.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query tenantRolePermissionQuery = Query.query(tenantRolePermissionCriteria);
		List<TenantAppRolePermissionMongodb> tenantRolePermissionMongodbs = mongoTemplate.find(tenantRolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);


		//查询子应用版本
		Criteria subappVersionCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query subappVersionQuery = Query.query(subappVersionCriteria);
		List<SubappVersionMongodb> subappVersionMongodbs = mongoTemplate.find(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);


		return subappVersionMongodbs.stream().map(version -> {
			TenantAppRolePermissionMongodb rolePermissionMongodb = tenantRolePermissionMongodbs.stream().filter(p -> p.getSubappVersion().equals(version.getSubappVersion())).findFirst().orElse(null);
			if (rolePermissionMongodb != null) {
				return TenantAppRoleSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(true)
					.build();
			} else {
				return TenantAppRoleSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(false)
					.build();
			}
		}).collect(Collectors.toList());

	}

	/**
	 * 删除企业角色权限
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_tenant_role_permission", keys = {"#tenantId","#appId", "#args.roleId", "#args.endpointId", "#args.subappId", "#args.subappVersion"})
	@BizLog(
		bizId = "app_role:delete_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteTenantRolePermission(String tenantId,String appId, DeleteTenantRolePermissionArgs args) {
		//查询是否开通应用角色权限
		Criteria tenantRolePermissionCriteria = Criteria
			.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).is(args.getRoleId())
			.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
			.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		Query tenantRolePermissionQuery = Query.query(tenantRolePermissionCriteria);
		TenantAppRolePermissionMongodb tenantRolePermissionMongodb = mongoTemplate.findOne(tenantRolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
		if (tenantRolePermissionMongodb == null) {
			throw new ConflictBusinessException("企业角色权限未开通");
		}
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria deleteTenantRolePermissionCriteria = Criteria
					.where(TenantAppRolePermissionMongodb.FIELD._ID).is(tenantRolePermissionMongodb.get_id());
				Query deleteTenantRolePermissionQuery = Query.query(deleteTenantRolePermissionCriteria);
				Update deleteTenantRolePermissionUpdate = new Update();
				deleteTenantRolePermissionUpdate.set(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				deleteTenantRolePermissionUpdate.currentDate(TenantAppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				mongoTemplate.updateMulti(deleteTenantRolePermissionQuery, deleteTenantRolePermissionUpdate, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				List<TenantAppRolePermissionMongodb> deleteAppRolePermissionMongodbList = mongoTemplate.findAllAndRemove(deleteTenantRolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION);
				if (!deleteAppRolePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteAppRolePermissionMongodbList, MongodbConstants.DeletedCollection.APP_ROLE_PERMISSION);
				}
				Criteria roleCriteria = Criteria
					.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
					.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId());
				Query roleQuery = Query.query(roleCriteria);

				Update roleUpdate = new Update();
				roleUpdate.set(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getTenantAppUserId());
				roleUpdate.currentDate(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(roleQuery, roleUpdate, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteTenantRolePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除企业角色权限失败");
			}
		});

	}



	@NewSpan
	List<MetadataTenantAppRole> getTenantAppRoleList(String tenantId, String appId, List<TenantAppRoleMongodb> ms, Map<String, String> extensionMap) {
		TenantAppRoleExtension roleExtension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.ROLE)).map(TenantAppRoleExtension::valueOf).orElse(TenantAppRoleExtension.ALL);

		Map<String, Integer> userCountMap = new HashMap<>();
		if (roleExtension.fields().contains(TenantAppRoleField.USER_NUM)) {
			Set<String> roleIds = ms.stream().map(TenantAppRoleMongodb::getRoleId).collect(Collectors.toSet());
			if (!roleIds.isEmpty()) {
				final Field fieldKey = Fields.field(TenantAppUserMongodb.FIELD.ROLE_IDS);
				userCountMap.putAll(readMongoTemplate.aggregate(Aggregation.newAggregation(
						Aggregation.match(Criteria
							.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
							.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
							.and(fieldKey.getTarget()).elemMatch(new Criteria().in(roleIds))
						),
						Aggregation.project(Fields.from(fieldKey)),
						Aggregation.unwind(fieldKey.getName()),
						Aggregation.group(fieldKey.getName()).count().as("Num"),
						Aggregation.sort(Sort.by(Sort.Order.desc(fieldKey.getName())))
					), MongodbConstants.Collection.TENANT_APP_USER, BasicDBObject.class).getMappedResults().stream()
					.collect(Collectors.toMap(z -> z.getString("_id"), z -> z.getInt("Num"))));
			}
		}

		Set<String> metadataUserIds = CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(ms.stream().map(TenantAppRoleMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, TenantAppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds))
			.orElse(Collections.emptyMap());

		return ms.stream()
			.map(x -> TenantAppRoleConverter.convert(x, userCountMap, metadataUserMap, roleExtension))
			.collect(Collectors.toList());
	}


	Criteria buildCriteria(String tenantId, String appId, GetRoleArgs args) {
		Criteria criteria = Criteria
			.where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId);
		Optional.ofNullable(args.getKeyword()).filter(kw -> !kw.isBlank()).ifPresent(kw -> criteria.and(TenantAppRoleMongodb.FIELD.ROLE_NAME).regex(kw));
		Optional.ofNullable(args.getRoleIds()).filter(r -> !r.isEmpty()).ifPresent(r -> criteria.and(TenantAppRoleMongodb.FIELD.ROLE_ID).in(r));
		Optional.ofNullable(args.getEnabled()).ifPresent(e -> criteria.and(TenantAppRoleMongodb.FIELD.ENABLED).is(e));
		return criteria;
	}

}
