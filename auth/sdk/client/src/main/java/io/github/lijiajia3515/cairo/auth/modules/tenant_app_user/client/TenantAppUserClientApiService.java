package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.client;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.TenantAppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;


public interface TenantAppUserClientApiService {

	/**
	 * 查询用户列表
	 * 需要权限： tenant_app_user:read｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户集合
	 */
	List<TenantAppUser> getTenantAppUserList(GetTenantAppUserArgs args);

	/**
	 * 查询用户分页
	 * 需要权限： tenant_app_user:read｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 分页对象
	 */
	Page<TenantAppUser> getTenantAppUserPageList(GetTenantAppUserArgs args);

	/**
	 * 获取用户认证信息
	 * 需要权限： tenant_app_user:tenant_app_user_auth｜tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户信息
	 */
	TenantAppUserAuthModel getTenantAppUserAuth(GetTenantAppUserAuthArgs args);

	List<TenantAppUser> getTenantAppUserSubDepartmentList(GetTenantAppUserArgs args);
}
