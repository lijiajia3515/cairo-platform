package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetCustomTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.GetTenantAppUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_user_authorization.TenantAppUserAuthorizationModel;

public interface TenantAppUserAuthorizationClientApiService {


	/**
	 * 获取企业终端用户认证
	 * 需要权限 tenant_app_user_authorization:get_tenant_app_user_authorization | tenant_app_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	TenantAppUserAuthorizationModel getTenantAppUserAuthorization(GetTenantAppUserAuthorizationArgs args);

	/**
	 * 获取企业终端用户认证
	 * 需要权限 tenant_app_user_authorization:get_tenant_app_user_authorization | tenant_app_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	TenantAppUserAuthorizationModel getCustomTenantAppUserAuthorization(GetCustomTenantAppUserAuthorizationArgs args);


}
