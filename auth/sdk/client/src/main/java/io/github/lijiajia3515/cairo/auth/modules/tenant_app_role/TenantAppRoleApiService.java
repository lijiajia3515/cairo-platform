package io.github.lijiajia3515.cairo.auth.modules.tenant_app_role;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;

public interface TenantAppRoleApiService {
	/**
	 * 获取角色列表
	 * 需要权限： tenant_app_role:read｜tenant_app_role:all
	 *
	 * @param args 参数
	 * @return 角色列表
	 */
	List<MetadataTenantAppRole> getTenantAppRoleList(GetTenantAppRoleArgs args);

	/**
	 * 获取角色分页列表
	 * 需要权限： tenant_app_role:read｜tenant_app_role:all
	 *
	 * @param args 参数
	 * @return 角色分页
	 */
	Page<MetadataTenantAppRole> getTenantAppRolePageList(GetTenantAppRoleArgs args);

}
