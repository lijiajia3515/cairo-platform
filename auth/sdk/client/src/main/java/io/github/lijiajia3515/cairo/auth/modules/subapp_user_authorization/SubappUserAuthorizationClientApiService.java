package io.github.lijiajia3515.cairo.auth.modules.subapp_user_authorization;


import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.SubappUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.subapp_user.GetSubappUserAuthorizationArgs;

public interface SubappUserAuthorizationClientApiService {


	/**
	 * 获取终端账号认证
	 * 需要权限 subapp_user_authorization:get_subapp_user_authorization | subapp_user_authorization:all
	 *
	 * @param args args
	 * @return auth model
	 */
	SubappUserAuthorizationModel getSubappUserAuthorization(GetSubappUserAuthorizationArgs args);


}
