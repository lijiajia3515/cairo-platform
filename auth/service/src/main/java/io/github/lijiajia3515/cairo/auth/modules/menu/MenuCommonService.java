package io.github.lijiajia3515.cairo.auth.modules.menu;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.PathMenu;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRolePermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuTreeArgs;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;
import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_PARENT_ID;
import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.TREE_ROOT;

/**
 * menu common service
 */
@Slf4j
@Service
@Validated
public class MenuCommonService {

	public static final Comparator<MenuNode> WEB_MENU_NODE_COMPARATOR = Comparator.comparing(MenuNode::getLeftNo).thenComparing(MenuNode::getMenuId);

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public MenuCommonService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
							 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
							 TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}


	/**
	 * 获取菜单树（适用于我的资源接口）
	 *
	 * @param args 参数，父级id 默认全部
	 * @return 菜单数组
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_tree",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MenuNode> getMenuTree(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuTreeArgs args) {
		String parentId = Optional.ofNullable(args).map(GetMenuTreeArgs::getParentId).orElse(ROOT_ID);


		Query parentQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(parentId)
		);
		MenuMongodb parentMenu = readMongoTemplate.findOne(parentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		if (parentMenu != null) {
			criteria.and(MenuMongodb.FIELD.LEFT_NO).gte(parentMenu.getLeftNo())
				.and(MenuMongodb.FIELD.RIGHT_NO).lte(parentMenu.getRightNo());
		}
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.DEPTH),
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));
		List<MenuMongodb> menus = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		Set<String> menuIds = menus.stream().map(MenuMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, List<PermissionMongodb>> elementMap = Optional.of(menuIds)
			.filter(x -> !x.isEmpty())
			.map(mIds -> {
				Query allElementQuery = Query.query(
					Criteria.where(PermissionMongodb.FIELD.APP_ID).is(appId)
						.and(PermissionMongodb.FIELD.MENU_ID).in(menuIds)
				);
				allElementQuery.with(Sort.by(
					Sort.Order.asc(PermissionMongodb.FIELD.MENU_ID),
					Sort.Order.asc(PermissionMongodb.FIELD.SORT)
				));
				return readMongoTemplate.find(allElementQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION).stream()
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
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.hiddenMenu(x.isHiddenMenu())
				.permissions(
					elementMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.authorities(e.getAuthorities())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.defaultPermission(Optional.ofNullable(e.getDefaultPermission()).orElse(false))
							.sort(e.getSort())
							.icon(e.getIcon())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, ROOT_ID);
	}

	/**
	 * 获取路径菜单map
	 *
	 * @param appId          应用ID
	 * @param endpointId  终端ID
	 * @param subappId      子应用ID
	 * @param subappVersion 子应用版本
	 * @param menuIds        menuIds
	 * @return 路径模式菜单 map
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_tree",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Map<String, PathMenu> getPathMenuMap(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, Collection<String> menuIds) {
		if (menuIds == null || menuIds.isEmpty()) return Collections.emptyMap();


		Query parentQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).in(menuIds)
		);
		List<MenuMongodb> firstMenuList = readMongoTemplate.find(parentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		// 利用左右值特性，查询出所有祖宗节点
		List<MenuMongodb> parentMenuList = Optional.of(firstMenuList)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentMenuCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.orOperator(noParentMenus.stream().map(x -> Criteria.where(MenuMongodb.FIELD.LEFT_NO).lt(x.getLeftNo()).and(MenuMongodb.FIELD.RIGHT_NO).gt(x.getRightNo())).collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentMenuCriteria);
				return readMongoTemplate.find(noParentMenuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
			}).orElse(Collections.emptyList());
		Set<MenuMongodb> allMenu = Stream.of(firstMenuList, parentMenuList).flatMap(Collection::stream).collect(Collectors.toSet());
		Map<String, MenuMongodb> allMenuMap = allMenu.stream().collect(Collectors.toMap(MenuMongodb::getMenuId, x -> x));

		Map<String, PathMenu> menuMap = new HashMap<>(menuIds.size());
		menuIds.forEach(menuId -> {
			MenuMongodb currentMenu = allMenuMap.get(menuId);
			if (currentMenu == null) return;
			List<MenuMongodb> currentMenuList = new ArrayList<>(currentMenu.getDepth());
			allMenu.forEach(x -> {
				if (x.getDepth() > 0 && currentMenu.getLeftNo() >= x.getLeftNo() && currentMenu.getRightNo() <= x.getRightNo()) {
					currentMenuList.add(x);
				}
			});
			currentMenuList.sort(Comparator.comparingInt(MenuMongodb::getDepth));
			menuMap.put(currentMenu.getMenuId(),
				PathMenu.builder()
					.menuId(currentMenu.getMenuId())
					.menuIds(currentMenuList.stream().map(MenuMongodb::getMenuId).collect(Collectors.toList()))
					.menuNames(currentMenuList.stream().map(MenuMongodb::getMenuName).collect(Collectors.toList()))
					.icon(currentMenu.getIcon())
					.component(currentMenu.getComponent())
					.path(currentMenu.getPath())
					.build()
			);

		});

		return menuMap;

	}

	/**
	 * 获取我的web菜单
	 *
	 * @param appId    appId
	 * @param tenantId tenantId
	 * @param userId   userId
	 * @return web menu
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_my_menu",
		scope = "read",
		params = {
			@BizLog.Param(key = "tenantId", value = "#tenantId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	public List<MenuNode> getMyTenantSubappUserMenu(String tenantId, String appId, String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, String userId) {
		Query userQuery = Query.query(Criteria
			.where(TenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(TenantAppUserMongodb.FIELD.APP_ID).is(appId)
			.and(TenantAppUserMongodb.FIELD.USER_ID).is(userId)
		);
		TenantAppUserMongodb user = readMongoTemplate.findOne(userQuery, TenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER);
		if (user == null) {
			return null;
		}
		List<String> permissionIds = new ArrayList<>();
		List<String> roleIds = Optional.ofNullable(user.getRoleIds()).orElse(Collections.emptyList());

		if (!roleIds.isEmpty()) {
			Criteria rolePermissionCriteria = Criteria
				.where(TenantAppRolePermissionMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppRolePermissionMongodb.FIELD.APP_ID).is(appId)
				.and(TenantAppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
				.and(TenantAppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(TenantAppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

			Query rolePermissionQuery = Query.query(rolePermissionCriteria);
			Set<String> rolePermissionIds = readMongoTemplate.find(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE_PERMISSION)
				.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
				.collect(Collectors.toSet());

			permissionIds.addAll(rolePermissionIds);
		}

		List<Criteria> permissionCriteria = new ArrayList<>(2);
		permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
		if (!permissionIds.isEmpty()) {
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
		}
		Criteria finalPermissionCriteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.orOperator(permissionCriteria);
		Query permissionQuery = Query.query(finalPermissionCriteria);
		List<PermissionMongodb> permissions = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		Set<String> menuIds = permissions.stream().map(PermissionMongodb::getMenuId).filter(Objects::nonNull).collect(Collectors.toSet());

		// 查询元素的所有菜单
		Query menuQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).in(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).in(menuIds)
			.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false)
		);
		List<MenuMongodb> permissionMenus = readMongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
		// 获取菜单中的父级菜单并从已有的数据中去除重复
		Set<MenuMongodb> noSelectParentMenus = permissionMenus.stream().filter(x -> !menuIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<MenuMongodb> parentMenus = Optional.of(noSelectParentMenus)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentMenuCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false)
					.orOperator(noParentMenus.stream().map(x -> Criteria.where(MenuMongodb.FIELD.LEFT_NO).lt(x.getLeftNo()).and(MenuMongodb.FIELD.RIGHT_NO).gt(x.getRightNo())).collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentMenuCriteria);
				return readMongoTemplate.find(noParentMenuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
			}).orElse(Collections.emptyList());
		permissionMenus.addAll(parentMenus);
		Set<MenuMongodb> allMenu = new HashSet<>(permissionMenus.size() + parentMenus.size());
		allMenu.addAll(permissionMenus);
		allMenu.addAll(parentMenus);

		Map<String, List<PermissionMongodb>> permissionMap = permissions.stream().collect(Collectors.groupingBy(PermissionMongodb::getMenuId));

		List<MenuNode> nodes = allMenu.stream()
			.map(x -> MenuNode.builder()
				.menuId(x.getMenuId())
				.parentId(x.getParentId())
				.menuName(x.getMenuName())
				.path(x.getPath())
				.component(x.getComponent())
				.icon(x.getIcon())
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.hiddenMenu(x.isHiddenMenu())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.icon(e.getIcon())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());

		List<MenuNode> sortedNodes = nodes.stream()
			.sorted(Comparator.comparing(MenuNode::getDepth).thenComparing(MenuNode::getLeftNo))
			.collect(Collectors.toList());

		return Tree2Converter.build(sortedNodes, ROOT_ID);
	}

	/**
	 * 获取我的应用用户web菜单
	 *
	 * @param appId  appId
	 * @param userId userId
	 * @return web menu
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_my_menu",
		scope = "read",
		params = {

			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "userId", value = "#userId")
		}
	)
	public List<MenuNode> getMyAppUserMenu(String appId, String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, String userId) {
		Query userQuery = Query.query(Criteria
			.where(AppUserMongodb.FIELD.APP_ID).is(appId)
			.and(AppUserMongodb.FIELD.USER_ID).is(userId)
		);
		AppUserMongodb user = readMongoTemplate.findOne(userQuery, AppUserMongodb.class, MongodbConstants.Collection.APP_USER);
		if (user == null) {
			return null;
		}
		List<String> permissionIds = new ArrayList<>();
		List<String> roleIds = Optional.ofNullable(user.getRoleIds()).orElse(Collections.emptyList());

		if (!roleIds.isEmpty()) {
			Criteria rolePermissionCriteria = Criteria
				.where(AppRolePermissionMongodb.FIELD.APP_ID).is(appId)
				.and(AppRolePermissionMongodb.FIELD.ROLE_ID).in(user.getRoleIds())
				.and(AppRolePermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
				.and(AppRolePermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

			Query rolePermissionQuery = Query.query(rolePermissionCriteria);
			Set<String> rolePermissionIds = readMongoTemplate.find(rolePermissionQuery, TenantAppRolePermissionMongodb.class, MongodbConstants.Collection.APP_ROLE_PERMISSION)
				.stream().filter(x -> x.getPermissionIds() != null).flatMap(x -> x.getPermissionIds().stream())
				.collect(Collectors.toSet());

			permissionIds.addAll(rolePermissionIds);
		}

		List<Criteria> permissionCriteria = new ArrayList<>(2);
		permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.DEFAULT_PERMISSION).is(true));
		if (!permissionIds.isEmpty()) {
			permissionCriteria.add(Criteria.where(PermissionMongodb.FIELD.PERMISSION_ID).in(permissionIds));
		}
		Criteria finalPermissionCriteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.orOperator(permissionCriteria);
		Query permissionQuery = Query.query(finalPermissionCriteria);
		List<PermissionMongodb> permissions = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		Set<String> menuIds = permissions.stream().map(PermissionMongodb::getMenuId).filter(Objects::nonNull).collect(Collectors.toSet());

		// 查询元素的所有菜单
		Query menuQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).in(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).in(menuIds)
		);
		List<MenuMongodb> permissionMenus = readMongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
		// 获取菜单中的父级菜单并从已有的数据中去除重复
		Set<MenuMongodb> noSelectParentMenus = permissionMenus.stream().filter(x -> !menuIds.contains(x.getParentId())).collect(Collectors.toSet());
		// 利用左右值特性，查询出所有祖宗节点
		List<MenuMongodb> parentMenus = Optional.of(noSelectParentMenus)
			.filter(x -> !x.isEmpty())
			.map(noParentMenus -> {
				Criteria noParentMenuCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.orOperator(noParentMenus.stream().map(x -> Criteria.where(MenuMongodb.FIELD.LEFT_NO).lt(x.getLeftNo()).and(MenuMongodb.FIELD.RIGHT_NO).gt(x.getRightNo())).collect(Collectors.toSet()));
				Query noParentMenuQuery = Query.query(noParentMenuCriteria);
				return readMongoTemplate.find(noParentMenuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
			}).orElse(Collections.emptyList());
		permissionMenus.addAll(parentMenus);
		Set<MenuMongodb> allMenu = new HashSet<>(permissionMenus.size() + parentMenus.size());
		allMenu.addAll(permissionMenus);
		allMenu.addAll(parentMenus);

		Map<String, List<PermissionMongodb>> permissionMap = permissions.stream().collect(Collectors.groupingBy(PermissionMongodb::getMenuId));

		List<MenuNode> nodes = allMenu.stream()
			.map(x -> MenuNode.builder()
				.menuId(x.getMenuId())
				.parentId(x.getParentId())
				.menuName(x.getMenuName())
				.path(x.getPath())
				.component(x.getComponent())
				.icon(x.getIcon())
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.tags(x.getTags())
				.hiddenMenu(x.isHiddenMenu())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.type(e.getType())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.icon(e.getIcon())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());

		List<MenuNode> sortedNodes = nodes.stream()
			.sorted(Comparator.comparing(MenuNode::getDepth).thenComparing(MenuNode::getLeftNo))
			.collect(Collectors.toList());

		return Tree2Converter.build(sortedNodes, ROOT_ID);
	}


	/**
	 * 获取系统管理员模式的菜单
	 *
	 * @param appId appId
	 * @return web menu
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_admin_menu",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId")
		}
	)
	public List<MenuNode> getAdminMenu(String appId, String endpointId, String subappId, String subappVersion) {
		Criteria menuCriteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.HIDDEN_MENU).is(false);
		Query menuQuery = Query.query(menuCriteria);
		menuQuery.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));
		List<MenuMongodb> menus = readMongoTemplate.find(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
		Criteria permissionCriteria = Criteria
			.where(PermissionMongodb.FIELD.APP_ID).is(appId)
			.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		Query permissionQuery = Query.query(permissionCriteria);
		permissionQuery.with(Sort.by(
			Sort.Order.asc(PermissionMongodb.FIELD.MENU_ID),
			Sort.Order.asc(PermissionMongodb.FIELD.SORT)
		));
		List<PermissionMongodb> permissionMongodbList = readMongoTemplate.find(permissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
		Map<String, List<PermissionMongodb>> permissionsMap = permissionMongodbList.stream().collect(Collectors.groupingBy(PermissionMongodb::getMenuId));

		List<MenuNode> nodes = menus.stream().map(m -> {
			return MenuNode.builder()
				.menuId(m.getMenuId())
				.parentId(m.getParentId())
				.menuName(m.getMenuName())
				.path(m.getPath())
				.component(m.getComponent())
				.icon(m.getIcon())
				.leftNo(m.getLeftNo())
				.rightNo(m.getRightNo())
				.depth(m.getDepth())
				.tags(m.getTags())
				.hiddenMenu(m.isHiddenMenu())
				.permissions(permissionsMap.getOrDefault(m.getMenuId(), Collections.emptyList()).stream().map(e -> MenuNode.Permission.builder()
					.permissionId(e.getPermissionId())
					.permissionName(e.getPermissionName())
					.icon(e.getIcon())
					.type(e.getType())
					.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
					.build()).collect(Collectors.toList()))
				.build();
		}).collect(Collectors.toList());
		return Tree2Converter.build(nodes, ROOT_ID);
	}

	/**
	 * 创建默认菜单
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @return 资源菜单mongodb模型
	 */
	@NewSpan
	public MenuMongodb createDefaultMenu(String appId, String endpointId, String subappId, String subappVersion) {
		return transactionTemplate.execute(status -> {
			MenuMongodb defaultMenu = MenuMongodb.builder()
				.appId(appId)
				.endpointId(endpointId)
				.subappId(subappId)
				.subappVersion(subappVersion)
				.menuId(TREE_ROOT)
				.menuName(endpointId)
				.parentId(ROOT_PARENT_ID)
				.leftNo(1)
				.rightNo(2)
				.depth(0)
				.metadata(AppUserMetadataMongodb.builder()
					.createUserId(CairoSecurityContextHolder.getSubappUserId())
					.updateUserId(CairoSecurityContextHolder.getSubappUserId())
					.build())
				.build();
			return mongoTemplate.insert(defaultMenu, MongodbConstants.Collection.MENU);
		});
	}

	/**
	 * 确保子应用菜单树的虚拟根（菜单ID TREE_ROOT）存在
	 * <p>
	 * 空树时退化为 {@link #createDefaultMenu}；已存在"无根"菜单（导入/手工造树的
	 * 历史数据）时，不再盲插固定编号的根——那会撞 leftNo 唯一索引——而是把既有
	 * 整棵森林右移 2、深度 +1，原顶层节点（parentId=-1）重挂到根下，由新根
	 * （leftNo=1，rightNo=maxRightNo+3）完整包裹，恢复嵌套集不变量。
	 * 调用方需自带事务与同子应用锁（create_menu/move_menu 的 Lock4j）。
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @return 虚拟根菜单
	 */
	public MenuMongodb ensureRootMenu(String appId, String endpointId, String subappId, String subappVersion) {
		Criteria scope = Criteria.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		MenuMongodb root = mongoTemplate.findOne(
			Query.query(new Criteria().andOperator(scope, Criteria.where(MenuMongodb.FIELD.MENU_ID).is(TREE_ROOT))),
			MenuMongodb.class, MongodbConstants.Collection.MENU);
		if (root != null) {
			return root;
		}

		// 倒序取：配合逐条 +2 更新，避免 leftNo 唯一索引在中途撞上未移动的节点
		Query allQuery = Query.query(scope);
		allQuery.with(Sort.by(Sort.Order.desc(MenuMongodb.FIELD.LEFT_NO)));
		List<MenuMongodb> existing = mongoTemplate.find(allQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
		if (existing.isEmpty()) {
			return createDefaultMenu(appId, endpointId, subappId, subappVersion);
		}

		int maxRightNo = 0;
		for (MenuMongodb menu : existing) {
			Update update = new Update()
				.inc(MenuMongodb.FIELD.LEFT_NO, 2)
				.inc(MenuMongodb.FIELD.RIGHT_NO, 2)
				.inc(MenuMongodb.FIELD.DEPTH, 1)
				.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
			mongoTemplate.updateFirst(
				Query.query(new Criteria().andOperator(scope, Criteria.where(MenuMongodb.FIELD.MENU_ID).is(menu.getMenuId()))),
				update, MenuMongodb.class, MongodbConstants.Collection.MENU);
			maxRightNo = Math.max(maxRightNo, menu.getRightNo());
		}

		// 原顶层重挂到根下（此时根尚未插入，parentId=-1 的即森林顶层，无需排除自身）
		mongoTemplate.updateMulti(
			Query.query(new Criteria().andOperator(scope, Criteria.where(MenuMongodb.FIELD.PARENT_ID).is(ROOT_PARENT_ID))),
			new Update().set(MenuMongodb.FIELD.PARENT_ID, TREE_ROOT),
			MenuMongodb.class, MongodbConstants.Collection.MENU);

		// 包裹全部既有树：根占 (1, maxRightNo+3)
		MenuMongodb rootMenu = MenuMongodb.builder()
			.appId(appId)
			.endpointId(endpointId)
			.subappId(subappId)
			.subappVersion(subappVersion)
			.menuId(TREE_ROOT)
			.menuName(endpointId)
			.parentId(ROOT_PARENT_ID)
			.leftNo(1)
			.rightNo(maxRightNo + 3)
			.depth(0)
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(CairoSecurityContextHolder.getSubappUserId())
				.updateUserId(CairoSecurityContextHolder.getSubappUserId())
				.build())
			.build();
		return mongoTemplate.insert(rootMenu, MongodbConstants.Collection.MENU);
	}
}
