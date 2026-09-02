package io.github.lijiajia3515.cairo.auth.api.subapp.app_role;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappVersionMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.framework.security.app_user.CairoAuthAppUserService;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleSubappVersion;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.CreateAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.DeleteAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.DeleteAppRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRoleInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRolePermissionArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_role.ModifyAppRoleStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.BasicAppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
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
 * [subapp_user/api] app role service
 */
@Slf4j
@Validated
@Component
public class AppRoleSubappApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final CairoAuthAppUserService cairoAuthAppUserService;
	private final AppUserCommonService userCommonService;
	private final AppRoleCommonService roleCommonService;

	public AppRoleSubappApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									   TransactionTemplate transactionTemplate,
									   CairoAuthAppUserService cairoAuthAppUserService,
									   AppUserCommonService userClientService,
									   AppRoleCommonService roleCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.cairoAuthAppUserService = cairoAuthAppUserService;
		this.userCommonService = userClientService;
		this.roleCommonService = roleCommonService;
	}


	/**
	 * get app_role list
	 *
	 * @param appId appId
	 * @param args  args
	 * @return app_role page
	 */
	@NewSpan
	@BizLog(
		bizId = "app_role:get_app_role_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataAppRole> getAppRoleList(@Valid @NotNull String appId, @Validated GetAppRoleArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));
		final List<AppRoleMongodb> rms = readMongoTemplate.find(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
		return getAppRoleList(appId, rms, args.getExtension());
	}


	/**
	 * get app_role page list
	 *
	 * @param appId 应用id
	 * @param args  args
	 * @return app_role page
	 */
	@NewSpan
	@BizLog(
		bizId = "app_role:get_app_role_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	Page<MetadataAppRole> getAppRolePageList(@Valid @NotNull String appId, @Validated GetAppRoleArgs args) {
		Criteria criteria = buildCriteria(appId, args);
		Query query = Query.query(criteria);

		long total = readMongoTemplate.count(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);

		query.with(args.pageable());
		query.with(Sort.by(
			Sort.Order.desc(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME)
		));
		List<AppRoleMongodb> rms = readMongoTemplate.find(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
		final List<MetadataAppRole> rs = getAppRoleList(appId, rms, args.getExtension());
		return new Page<>(args, rs, total);
	}

	@NewSpan
	@BizLog(
		bizId = "app_role:get_app_role_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "app_roleId", value = "#app_roleId"),
		}
	)
	Optional<AppRole> getAppRoleInfo(@Valid @NotNull String appId, @Valid @NotNull String app_roleId) {
		Query query = Query.query(Criteria
			.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
			.and(AppRoleMongodb.FIELD.ROLE_ID).is(app_roleId));
		AppRoleMongodb role = readMongoTemplate.findOne(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
		return Optional.ofNullable(role).map(AppRoleConverter::convert);
	}

	@NewSpan
	List<MenuNode> getAppRolePermission(@Valid @NotNull String appId, @Valid @NotNull String roleId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion) {
		Query menuRootQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(ROOT_ID)
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
		menuQuery.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.DEPTH),
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));

		List<MenuMongodb> menus = mongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria appRolePermissionCriteria = Criteria
			.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(roleId)
			.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		Set<String> appRolePermissionIds = Optional.ofNullable(mongoTemplate.findOne(appRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION))
			.map(AppRolePermissionMongodb::getPermissionIds)
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
				.and(PermissionMongodb.FIELD.HIDDEN_PERMISSION).is(false)
			);
			permissionQuery.with(Sort.by(
				Sort.Order.asc(PermissionMongodb.FIELD.SORT)
			));
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
							.type(e.getType())
							.defaultPermission(e.getDefaultPermission())
							.hiddenPermission(e.getHiddenPermission())
							.isSelected(appRolePermissionIds.contains(e.getPermissionId()))
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
	 * 修改应用角色
	 */
	@NewSpan
	@BizLog(
		bizId = "app_role:create_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAppRole(@Valid @NotNull String appId, @Validated CreateAppRoleArgs args) {
		AppRoleMongodb insertedAppRole = transactionTemplate.execute(status -> {
			try {
				AppRoleMongodb app_role = AppRoleMongodb.builder()
					.appId(appId)
					.roleId(Optional.ofNullable(args.getRoleId()).filter(x -> !x.isBlank()).orElse(CoreConstants.SNOWFLAKE.nextIdStr()))
					.roleName(args.getRoleName())
					.remark(args.getRemark())
					.enabled(true)
					.sort(CoreConstants.SNOWFLAKE.nextId())
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build()
					)
					.build();
				return mongoTemplate.insert(app_role, MongodbConstants.Collection.APP_ROLE);
			} catch (Exception e) {
				log.info("createAppRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建应用角色失败");
			}
		});
		if (insertedAppRole == null) {
			throw new ConflictBusinessException("创建应用角色失败");
		}
	}

	/**
	 * 修改应用角色信息
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_role_info", keys = {"#appId", "#args.roleId"})
	@BizLog(
		bizId = "app_role:modify_app_role_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppRoleInfo(@Valid @NotNull String appId, @Validated ModifyAppRoleInfoArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(AppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId())
				);
				Update update = new Update();
				Optional.ofNullable(args.getRoleName()).ifPresent(name -> update.set(AppRoleMongodb.FIELD.ROLE_NAME, name));
				Optional.ofNullable(args.getRemark()).ifPresent(remark -> update.set(AppRoleMongodb.FIELD.REMARK, remark));

				update.set(AppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改应用角色失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyAppRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用角色失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 修改应用角色权限
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_role_permission", keys = {"#appId", "#args.endpointId", "#args.subappId", "#args.subappVersion", "#args.roleId"})
	@BizLog(
		bizId = "app_role:modify_app_role_permission",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppRolePermission(@Valid @NotNull String appId, @Validated ModifyAppRolePermissionArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query app_rolePermissionQuery = Query.query(Criteria
					.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(args.getRoleId())
					.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
					.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion())
				);
				boolean exists = mongoTemplate.exists(app_rolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				if (exists) {
					Update update = new Update();
					update.set(AppRolePermissionMongodb.FIELD.PERMISSION_IDS, args.getPermissionIds());
					update.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
					update.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult app_rolePermissionUpdateResult = mongoTemplate.updateFirst(app_rolePermissionQuery, update, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
					if (app_rolePermissionUpdateResult.getModifiedCount() < 1) {
						throw new ConflictBusinessException("修改应用角色权限失败");
					}
				} else {
					AppRolePermissionMongodb insertAppRolePermission = AppRolePermissionMongodb.builder()
						.appId(appId)
						.endpointId(args.getEndpointId())
						.subappId(args.getSubappId())
						.subappVersion(args.getSubappVersion())
						.roleId(args.getRoleId())
						.permissionIds(args.getPermissionIds())
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getSubappUserId())
							.updateUserId(CairoSecurityContextHolder.getSubappUserId())
							.build())
						.build();
					mongoTemplate.insert(insertAppRolePermission, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				}
				Criteria app_roleCriteria = Criteria
					.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(AppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId());
				Query app_roleQuery = Query.query(app_roleCriteria);
				Update app_roleUpdate = Update.update(AppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				app_roleUpdate.currentDate(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult app_roleUpdateResult = mongoTemplate.updateFirst(app_roleQuery, app_roleUpdate, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
				if (app_roleUpdateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改应用角色权限失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyAppRolePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用角色权限失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 修改应用角色状态
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "modify_app_role_status", keys = {"#appId", "#args.roleId"})
	@BizLog(
		bizId = "app_role:modify_app_role_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppRoleStatus(@Valid @NotNull String appId, @Validated ModifyAppRoleStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(AppRoleMongodb.FIELD.ROLE_ID).is(args.getRoleId())
				);
				Update update = new Update();
				update.set(AppRoleMongodb.FIELD.ENABLED, args.getEnabled());

				update.set(AppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult result = mongoTemplate.updateFirst(query, update, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);

				if (result.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改应用角色状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("modifyAppRoleStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改应用角色状态失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 删除
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_app_roles", keys = {"#appId"})
	@BizLog(
		bizId = "app_role:delete_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteAppRole(@Valid @NotNull String appId, @Validated DeleteAppRoleArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				List<BasicAppUser> existsUserList = roleCommonService.existsAppUserList(appId, args.getRoleIds());
				if (!existsUserList.isEmpty()) {
					String nicknames = existsUserList.stream().map(x -> String.format("\"%s\"", x.getNickname())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("应用角色被用户[" + nicknames + "]使用，不允许删除");
				}

				Criteria appRoleCriteria = Criteria
					.where(AppRoleMongodb.FIELD.APP_ID).is(appId)
					.and(AppRoleMongodb.FIELD.ROLE_ID).in(args.getRoleIds());
				Query appRoleQuery = Query.query(appRoleCriteria);

				Update app_roleUpdate = new Update();
				app_roleUpdate.set(AppRoleMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				app_roleUpdate.currentDate(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult appRoleUpdateResult = mongoTemplate.updateMulti(appRoleQuery, app_roleUpdate, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
				List<AppRoleMongodb> deletedAppRoleMongodbList = mongoTemplate.findAllAndRemove(appRoleQuery, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
				if (!deletedAppRoleMongodbList.isEmpty()) {
					mongoTemplate.insert(deletedAppRoleMongodbList, MongodbConstants.DeletedCollection.APP_ROLE);
				}

				Criteria appRolePermissionCriteria = Criteria
					.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
					.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(args.getRoleIds());
				Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
				Update appRolePermissionUpdate = new Update();
				appRolePermissionUpdate.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				appRolePermissionUpdate.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult appRolePermissionUpdateResult = mongoTemplate.updateMulti(appRolePermissionQuery, appRolePermissionUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				List<AppRolePermissionMongodb> deleteAppRolePermissionMongodbList = mongoTemplate.findAllAndRemove(appRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				if (!deleteAppRolePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteAppRolePermissionMongodbList, MongodbConstants.DeletedCollection.APP_ROLE_PERMISSION);
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteAppRole", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用角色失败");
			}
		});

		// remove cache
		cairoAuthAppUserService.removeAllAppUserCache(appId);
	}

	/**
	 * 查看应用角色子应用版本
	 *
	 * @param appId         appId
	 * @param endpointId endpointId
	 * @param roleId        roleId
	 * @param subappId     subappId
	 */
	@NewSpan
	@BizLog(
		bizId = "app_role_subapp_version:get_app_role_subapp_version",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "roleId", value = "#roleId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
		}
	)
	public List<AppRoleSubappVersion> getAppRoleSubappVersion(String appId, String endpointId, String roleId, String subappId) {
		//查询应用角色权限
		Criteria appRolePermissionCriteria = Criteria
			.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(roleId)
			.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		List<AppRolePermissionMongodb> appRolePermissionMongodbs = mongoTemplate.find(appRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);


		//查询子应用版本
		Criteria subappVersionCriteria = Criteria
			.where(SubappVersionMongodb.FIELD.SUBAPP_ID).is(subappId);
		Query subappVersionQuery = Query.query(subappVersionCriteria);
		List<SubappVersionMongodb> subappVersionMongodbs = mongoTemplate.find(subappVersionQuery, SubappVersionMongodb.class, MongodbConstants.Collection.SUBAPP_VERSION);


		return subappVersionMongodbs.stream().map(version -> {
			AppRolePermissionMongodb rolePermissionMongodb = appRolePermissionMongodbs.stream().filter(p -> p.getSubappVersion().equals(version.getSubappVersion())).findFirst().orElse(null);
			if (rolePermissionMongodb != null) {
				return AppRoleSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(true)
					.build();
			} else {
				return AppRoleSubappVersion.builder()
					.subappId(version.getSubappId())
					.subappVersion(version.getSubappVersion())
					.subappRemark(version.getSubappRemark())
					.enabled(false)
					.build();
			}
		}).collect(Collectors.toList());

	}

	/**
	 * 删除应用角色权限
	 *
	 * @param appId appId
	 * @param args  args
	 */
	@NewSpan
	@Lock4j(name = "delete_app_role_permission", keys = {"#appId", "#roleId", "#endpointId", "#subappId", "#subappVersion"})
	@BizLog(
		bizId = "app_role:delete_app_role",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteAppRolePermission(String appId, DeleteAppRolePermissionArgs args) {
		//查询是否开通应用角色权限
		Criteria appRolePermissionCriteria = Criteria
			.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
			.and(AppRolePermissionMongodb.FIELD.ROLE_ID).is(args.getRoleId())
			.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(args.getSubappId())
			.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(args.getSubappVersion());
		Query appRolePermissionQuery = Query.query(appRolePermissionCriteria);
		AppRolePermissionMongodb appRolePermissionMongodb = mongoTemplate.findOne(appRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
		if (appRolePermissionMongodb == null) {
			throw new ConflictBusinessException("应用角色权限未开通");
		}
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria deleteAppRolePermissionCriteria = Criteria
					.where(AppRolePermissionMongodb.FIELD._ID).is(appRolePermissionMongodb.get_id());
				Query deleteAppRolePermissionQuery = Query.query(deleteAppRolePermissionCriteria);
				Update deleteAppRolePermissionUpdate = new Update();
				deleteAppRolePermissionUpdate.set(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				deleteAppRolePermissionUpdate.currentDate(AppRolePermissionMongodb.FIELD.METADATA.UPDATE_TIME);

				 mongoTemplate.updateMulti(deleteAppRolePermissionQuery, deleteAppRolePermissionUpdate, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				List<AppRolePermissionMongodb> deleteAppRolePermissionMongodbList = mongoTemplate.findAllAndRemove(deleteAppRolePermissionQuery, AppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION);
				if (!deleteAppRolePermissionMongodbList.isEmpty()) {
					mongoTemplate.insert(deleteAppRolePermissionMongodbList, MongodbConstants.DeletedCollection.APP_ROLE_PERMISSION);
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("deleteAppRolePermission", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用角色权限失败");
			}
		});

	}

	@NewSpan
	List<MetadataAppRole> getAppRoleList(String appId, List<AppRoleMongodb> ms, Map<String, String> extensionMap) {
		AppRoleExtension app_roleExtension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.APP_ROLE)).map(AppRoleExtension::valueOf).orElse(AppRoleExtension.ALL);

		Map<String, Integer> userCountMap = new HashMap<>();
		if (app_roleExtension.fields().contains(AppRoleField.USER_NUM)) {
			Set<String> app_roleIds = ms.stream().map(AppRoleMongodb::getRoleId).collect(Collectors.toSet());
			if (!app_roleIds.isEmpty()) {
				final Field fieldKey = Fields.field(AppUserMongodb.FIELD.ROLE_IDS);
				userCountMap.putAll(readMongoTemplate.aggregate(Aggregation.newAggregation(
						Aggregation.match(Criteria
							.where(AppUserMongodb.FIELD.APP_ID).is(appId)
							.and(fieldKey.getTarget()).elemMatch(new Criteria().in(app_roleIds))
						),
						Aggregation.project(Fields.from(fieldKey)),
						Aggregation.unwind(fieldKey.getName()),
						Aggregation.group(fieldKey.getName()).count().as("Num"),
						Aggregation.sort(Sort.by(Sort.Order.desc(fieldKey.getName())))
					), MongodbConstants.Collection.APP_USER, BasicDBObject.class).getMappedResults().stream()
					.collect(Collectors.toMap(z -> z.getString("_id"), z -> z.getInt("Num"))));
			}
		}

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppRoleMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> userCommonService.getAppUserMapByAppUserIds(appId, userIds))
			.orElse(Collections.emptyMap());

		return ms.stream()
			.map(x -> AppRoleConverter.convert(x, userCountMap, metadataUserMap, app_roleExtension))
			.collect(Collectors.toList());
	}


	Criteria buildCriteria(String appId, GetAppRoleArgs args) {
		Criteria criteria = Criteria
			.where(AppRoleMongodb.FIELD.APP_ID).is(appId);
		Optional.ofNullable(args.getKeyword()).filter(kw -> !kw.isEmpty()).ifPresent(kw -> criteria.and(AppRoleMongodb.FIELD.ROLE_NAME).regex(kw));
		Optional.ofNullable(args.getRoleIds()).filter(r -> !r.isEmpty()).ifPresent(r -> criteria.and(AppRoleMongodb.FIELD.ROLE_ID).in(r));
		Optional.ofNullable(args.getEnabled()).ifPresent(e -> criteria.and(AppRoleMongodb.FIELD.ENABLED).is(e));
		return criteria;
	}

}
