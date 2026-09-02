package io.github.lijiajia3515.cairo.auth.modules.app_user;


import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.AppUserAuthModel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserAuthArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.core.page.Page;

import java.util.List;

public interface AppUserClientApiService {

    /**
     * 查询用户列表
     * 需要权限： app_user:read｜app_user:all
     * @param args args
     * @return 用户集合
     */
    List<AppUser> getAppUserList(GetAppUserClientArgs args);

    /**
     * 查询用户分页
     * 需要权限： app_user:read｜app_user:all
     * @param args args
     * @return 分页对象
     */
    Page<AppUser> getAppUserPageList(GetAppUserClientArgs args);

    /**
     * 获取用户认证信息
     * 需要权限： app_user:app_user_auth｜app_user:all
     * @param args args
     * @return 用户信息
     */
   AppUserAuthModel getAppUserAuth(GetAppUserAuthArgs args);

}
