package io.github.lijiajia3515.cairo.auth.modules.app_role;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface AppRoleClientApiService {
	/**
	 * 获取应用角色列表
	 * 需要权限： app_role:read｜app_role:all
	 *
	 * @param args 参数
	 * @return 角色列表
	 */
	List<MetadataAppRole> getAppRoleList(GetAppRoleArgs args);

	/**
	 * 获取应用角色分页列表
	 * 需要权限： app_role:read｜app_role:all
	 *
	 * @param args 参数
	 * @return 角色分页
	 */
	Page<MetadataAppRole> getAppRolePageList(GetAppRoleArgs args);

}
