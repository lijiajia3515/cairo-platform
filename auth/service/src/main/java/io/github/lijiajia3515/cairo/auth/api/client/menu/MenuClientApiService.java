package io.github.lijiajia3515.cairo.auth.api.client.menu;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
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

@Component
public class MenuClientApiService {
	private final MongoTemplate readMongoTemplate;

	public MenuClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.readMongoTemplate = readMongoTemplate;

	}

	/**
	 * 获取菜单树
	 *
	 * @return 菜单数组
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MenuNode> getMenuTreeList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Valid @NotNull String rootId) {
		String parentId = Optional.ofNullable(rootId).orElse(ROOT_ID);

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
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.hiddenMenu(x.isHiddenMenu())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.authorities(e.getAuthorities())
							.icon(e.getIcon())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.defaultPermission(Optional.ofNullable(e.getDefaultPermission()).orElse(false))
							.sort(e.getSort())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, parentId);

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
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<ClientMenu> getMenuList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuListArgs args) {
		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		Optional.ofNullable(args.getParentId()).ifPresent(parentId -> criteria.and(MenuMongodb.FIELD.PARENT_ID).is(parentId));
		Optional.ofNullable(args.getMenuIds()).ifPresent(menuIds -> criteria.and(MenuMongodb.FIELD.MENU_ID).in(menuIds));
		if (null!=args.getMenuNos()&& !args.getMenuNos().isEmpty()){
			criteria.orOperator(args.getMenuNos().stream().map(x -> Criteria.where(MenuMongodb.FIELD.LEFT_NO).lt(x.getLeftNo()).and(MenuMongodb.FIELD.RIGHT_NO).gt(x.getRightNo())).collect(Collectors.toSet()));
		}
		if (null!=args.getLeftNo()){
			criteria.and(MenuMongodb.FIELD.LEFT_NO).gt(args.getLeftNo());
		}
		if (null!=args.getRightNo()){
			criteria.and(MenuMongodb.FIELD.RIGHT_NO).lt(args.getRightNo());
		}
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(MenuMongodb.FIELD.LEFT_NO)
		));

		List<MenuMongodb> list = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		return list.stream().map(MenuConverter::convertClientMenu).collect(Collectors.toList());
	}
}
