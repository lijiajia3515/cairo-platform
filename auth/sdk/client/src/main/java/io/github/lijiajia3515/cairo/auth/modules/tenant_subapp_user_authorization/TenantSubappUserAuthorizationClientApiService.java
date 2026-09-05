package io.github.lijiajia3515.cairo.auth.modules.tenant_subapp_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.GetTenantSubappUserAuthorizationArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_subapp_user_authorization.TenantSubappUserAuthorizationModel;

public interface TenantSubappUserAuthorizationClientApiService {


	/**
	 * 获取应用级用户认证
	 * 需要权限 tenant_subapp_user_authorization:get_tenant_subapp_user_authorization | tenant_subapp_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	TenantSubappUserAuthorizationModel getTenantSubappUserAuthorization(GetTenantSubappUserAuthorizationArgs args);


}
