package io.github.lijiajia3515.cairo.auth.modules.permission;

import io.github.lijiajia3515.cairo.auth.domain.api.client.permission.GetPermissionListArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;

import java.util.List;

/**
 * 功能权限-apiService
 */

public interface PermissionClientApiService {

	/**
	 * 获取功能权限list
	 *
	 * @param args      参数
	 * @return 功能权限集合
	 */
	List<Permission>  getPermissionList(GetPermissionListArgs args);

	/**
	 * 获取我的功能权限list
	 * @param args      参数
	 * @return 功能权限集合
	 */
	List<Permission> getMyPermissionList(GetPermissionListArgs args);
}
