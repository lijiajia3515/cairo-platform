package io.github.lijiajia3515.cairo.auth.modules.menu;

import io.github.lijiajia3515.cairo.auth.domain.dto.menu.Menu;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MetadataMenu;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.api.client.menu.ClientMenu;

import java.util.Map;

/**
 * menu converter
 */
public class MenuConverter {

	public static Menu convertMenu(MenuMongodb m) {
		return Menu.builder()
			.menuId(m.getMenuId())
			.parentId(m.getParentId())
			.menuName(m.getMenuName())
			.path(m.getPath())
			.component(m.getComponent())
			.icon(m.getIcon())
			.hiddenMenu(m.isHiddenMenu())
			.tags(m.getTags())
			.build();
	}

	public static MetadataMenu convertMetadataMenu(MenuMongodb m, Map<String, AppUser> metadataUserMap) {
		return MetadataMenu.builder()
			.menuId(m.getMenuId())
			.parentId(m.getParentId())
			.menuName(m.getMenuName())
			.path(m.getPath())
			.component(m.getComponent())
			.icon(m.getIcon())
			.hiddenMenu(m.isHiddenMenu())
			.tags(m.getTags())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static ClientMenu convertClientMenu(MenuMongodb menuMongodb) {
		return ClientMenu.builder()
			.menuId(menuMongodb.getMenuId())
			.parentId(menuMongodb.getParentId())
			.menuName(menuMongodb.getMenuName())
			.path(menuMongodb.getPath())
			.component(menuMongodb.getComponent())
			.icon(menuMongodb.getIcon())
			.hiddenMenu(menuMongodb.isHiddenMenu())
			.tags(menuMongodb.getTags())
			.leftNo(menuMongodb.getLeftNo())
			.rightNo(menuMongodb.getRightNo())
			.depth(menuMongodb.getDepth())
			.build();
	}
}
