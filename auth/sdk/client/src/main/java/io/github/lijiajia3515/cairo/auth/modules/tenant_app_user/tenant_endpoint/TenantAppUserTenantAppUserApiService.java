package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.tenant_endpoint;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user.GetTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;

public interface TenantAppUserTenantAppUserApiService {

	/**
	 * 获取企业应用用户列表
	 * 需要权限 tenant_app_user:read ｜ tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户集合
	 */
	List<TenantAppUser> getTenantAppUserList(GetTenantAppUserArgs args);

	/**
	 * 获取企业应用用户分页列表
	 * 需要权限 tenant_app_user:read ｜ tenant_app_user:all
	 *
	 * @param args args
	 * @return 用户分页集合
	 */
	Page<TenantAppUser> getTenantAppUserPageList(GetTenantAppUserArgs args);


}
