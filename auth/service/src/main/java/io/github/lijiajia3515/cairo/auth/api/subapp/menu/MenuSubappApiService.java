package io.github.lijiajia3515.cairo.auth.api.subapp.menu;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.Menu;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;


/**
 * [subapp_user/api] menu service
 */
@Slf4j
@Service
@Validated
public class MenuSubappApiService {

	private final MongoTemplate readMongoTemplate;

	public MenuSubappApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 获取菜单 集合模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<Menu> getMenuList(@Valid @NotNull String appId, @Valid @NotNull String endpointId,  @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuListArgs args) {
		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		Optional.ofNullable(args.getParentId()).ifPresent(menuId -> criteria.and(MenuMongodb.FIELD.PARENT_ID).is(menuId));

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(MenuMongodb.FIELD.LEFT_NO)
		));

		List<MenuMongodb> list = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		return list.stream().map(MenuConverter::convertMenu).collect(Collectors.toList());
	}

	/**
	 * 获取元素菜单 分页模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 元素 page
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<Menu> getMenuPageList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuPageListArgs args) {
		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		Optional.ofNullable(args.getParentId()).ifPresent(menuId -> criteria.and(MenuMongodb.FIELD.PARENT_ID).is(menuId));

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(MenuMongodb.FIELD.LEFT_NO)
		));

		long total = readMongoTemplate.count(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		query.with(args.pageable());
		List<MenuMongodb> menuMongodbList = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		List<Menu> menuList = menuMongodbList.stream().map(MenuConverter::convertMenu).collect(Collectors.toList());
		return new Page<>(args, menuList, total);
	}

	/**
	 * 获取菜单树（适用于我的资源接口）
	 *
	 * @param args 参数，父级id 默认全部
	 * @return 菜单数组
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MenuNode> getMenuTreeList(@Valid @NotNull String appId, @Valid @NotNull String endpointId,  @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuTreeArgs args) {
		String parentId = Optional.ofNullable(args).map(GetMenuTreeArgs::getParentId).orElse(ROOT_ID);


		Query parentQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(parentId)
		);
		MenuMongodb parentMenuMongodb = readMongoTemplate.findOne(parentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		if (parentMenuMongodb != null) {
			criteria.and(MenuMongodb.FIELD.LEFT_NO).gte(parentMenuMongodb.getLeftNo())
				.and(MenuMongodb.FIELD.RIGHT_NO).lte(parentMenuMongodb.getRightNo());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.DEPTH),
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));
		List<MenuMongodb> menus = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Set<String> menuIds = menus.stream().map(MenuMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, List<PermissionMongodb>> permissionMap = Optional.of(menuIds)
			.filter(x -> !x.isEmpty())
			.map(mIds -> {
				Query allElementQuery = Query.query(
					Criteria.where(PermissionMongodb.FIELD.APP_ID).is(appId)
						.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
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
				.hiddenMenu(x.isHiddenMenu())
				.tags(x.getTags())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.authorities(e.getAuthorities())
							.icon(e.getIcon())
							.type(e.getType())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.defaultPermission(Optional.ofNullable(e.getDefaultPermission()).orElse(false))
							.sort(e.getSort())
							.build()
						).collect(Collectors.toList())
				)
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, parentId);

	}

}
