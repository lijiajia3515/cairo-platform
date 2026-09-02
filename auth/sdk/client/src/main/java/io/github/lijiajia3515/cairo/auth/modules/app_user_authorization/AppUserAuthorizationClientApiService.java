package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization;

import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.AppUserAuthorizationModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user_authorization.GetAppUserAuthorizationArgs;

public interface AppUserAuthorizationClientApiService {


	/**
	 * 获取终端账号认证
	 * 需要权限 app_user_authorization:get_app_user_authorization | app_user:all
	 *
	 * @param args args
	 * @return auth model
	 */
	AppUserAuthorizationModel getAppUserAuthorization(GetAppUserAuthorizationArgs args);


}
