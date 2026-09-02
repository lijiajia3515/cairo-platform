package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.AppUserMetadataPermission;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.MetadataPermission;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.PathMenu;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * 功能权限 converter
 */
public class PermissionConverter {


	public static MetadataPermission convertMetadataPermission(PermissionMongodb m, Map<String, PathMenu> pathMenuMap, Map<String, AppUser> metadataUserMap) {
		return MetadataPermission.builder()
			.menuId(m.getMenuId())
			.menuIcon(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getIcon).orElse(null))
			.menuNames(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getMenuNames).orElse(Collections.singletonList(m.getMenuId())))
			.permissionId(m.getPermissionId())
			.permissionName(m.getPermissionName())
			.authorities(m.getAuthorities())
			.type(m.getType())
			.defaultPermission(Optional.ofNullable(m.getDefaultPermission()).orElse(false))
			.hiddenPermission(Optional.ofNullable(m.getHiddenPermission()).orElse(false))
			.sort(m.getSort())
			.icon(m.getIcon())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static AppUserMetadataPermission convertAppUserMetadataPermission(PermissionMongodb m, Map<String, PathMenu> pathMenuMap, Map<String, AppUser> metadataUserMap) {
		return AppUserMetadataPermission.builder()
			.menuId(m.getMenuId())
			.menuIcon(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getIcon).orElse(null))
			.menuNames(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getMenuNames).orElse(Collections.singletonList(m.getMenuId())))
			.permissionId(m.getPermissionId())
			.permissionName(m.getPermissionName())
			.authorities(m.getAuthorities())
			.type(m.getType())
			.defaultPermission(Optional.ofNullable(m.getDefaultPermission()).orElse(false))
			.hiddenPermission(Optional.ofNullable(m.getHiddenPermission()).orElse(false))
			.sort(m.getSort())
			.icon(m.getIcon())
			.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap))
			.build();
	}

	public static Permission convertPermission(PermissionMongodb m, Map<String, PathMenu> pathMenuMap) {
		return Permission.builder()
			.menuId(m.getMenuId())
			.menuIcon(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getIcon).orElse(null))
			.menuNames(Optional.ofNullable(pathMenuMap.get(m.getMenuId())).map(PathMenu::getMenuNames).orElse(Collections.singletonList(m.getMenuId())))
			.permissionId(m.getPermissionId())
			.permissionName(m.getPermissionName())
			.authorities(m.getAuthorities())
			.type(m.getType())
			.defaultPermission(Optional.ofNullable(m.getDefaultPermission()).orElse(false))
			.hiddenPermission(Optional.ofNullable(m.getHiddenPermission()).orElse(false))
			.sort(m.getSort())
			.build();
	}

}
