// 个人中心——我的账号/用户信息/登录会话/我的日志(app_user_api)
import fetch from './fetch';
import { appUserApi, subappUserApi } from './urls';

// 发送当前账号手机号验证码( (用户级别)
export const sendMyAccountPhoneNumberVerifyCode_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/verify_code/send_my_account_phone_number_verify_code', params, headers);
}

// 修改当前账号手机号
export const modifyMyAccountPhoneNumber_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/account/modify_my_account_phone_number', params, {});
}

// 修改当前账号头像（直接上传图片二进制，后端 consumes image/png|jpeg|gif）
export const modifyMyAccountAvatar_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/account/modify_my_account_avatar', params, headers);
}

// 修改当前账号密码
export const modifyMyAccountPassword_api = (params) => {
    return fetch.appUserPost(appUserApi + '/account/modify_my_account_password', params, {});
}

// 修改当前用户名
export const modifyMyAccountUsername_api = (params) => {
    return fetch.appUserPost(appUserApi + '/account/modify_my_account_username', params, {});
}

// 账号三方绑定列表
export const getMyAccountSnsList_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/account_sns/get_my_account_sns_list', params, headers)
}

// 账号三方绑定解绑
export const unbindAccountSns_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/account_sns/unbind_account_sns', params, headers)
}

// 账号三方绑定绑定
export const bindAccountSns_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/account_sns/bind_account_sns', params, headers)
}

// 获取当前账号密码状态(eu)
export const getMyAccountPasswordStatus_api = (params) => {
    return fetch.appUserPost(appUserApi + '/account/get_my_account_password_status', params, {});
}

// 获取当前用户信息
export const getMyAppUserInfo_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user/get_my_app_user_info', params, {});
}

// 修改当前用户信息
export const modifyMyAppUserInfo_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user/modify_my_app_user_info', params, {});
}

// 获取当前用户注销状态
export const getMyAppUserLogoffStatus_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/app_user/get_my_app_user_logoff_status', params, headers);
}

// 当前用户预注销信息
export const getMyAppUserPreLogoffInfo_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/app_user/get_my_app_user_pre_logoff_info', params, headers);
}

// 注销当前应用级用户
export const logoffMyAppUser_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/app_user/logoff_my_app_user', params, headers);
}

// 取消注销当前应用级用户
export const unlogoffMyAppUser_api = (params, headers) => {
    return fetch.appUserPost(appUserApi + '/app_user/unlogoff_my_app_user', params, headers);
}

// 退出登录
export const logoutAppUserAuthorization_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user_authorization/logout_app_user_authorization', params, {});
}

// 个人中心会话列表分页列表
export const getMyAppUserAuthorizationPage_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user_authorization/get_my_app_user_authorization_page_list', params, {});
}

// 个人中心会话列表下线
export const offlineMyAppUserAuthorization_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user_authorization/offline_my_app_user_authorization', params, {});
}

// 获取我的(app_user 应用级用户)登录日志分页列表
export const getMyAppUserLoginLogPage_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_user_login_log/get_my_app_user_login_log_page_list', params, {});
}

// 按显式子应用上下文拉菜单(子应用切换器预取各子应用菜单用)
export const getSubappUserMenuByContext_api = (subappId, subappVersion, params) => {
    return fetch.subappPost(subappId, subappVersion, subappUserApi + '/app_user/get_my_subapp_user_menu', params, {},);
}

// 按显式子应用上下文拉功能权限(与菜单同源预取,切换子应用零请求)
export const getSubappUserPermissionIdsByContext_api = (subappId, subappVersion, params) => {
    return fetch.subappPost(subappId, subappVersion, subappUserApi + '/app_user/get_my_subapp_user_permission_ids', params, {});
}

// 获取我的应用业务日志分页列表
export const getMyAppBizLogPage_api = (params) => {
    return fetch.appUserPost(appUserApi + '/app_biz_log/get_my_app_biz_log_page_list', params, {})
}

// 获取我的子应用业务日志分页列表
export const getMySubappBizLogPage_api = (params) => {
    return fetch.appUserPost(appUserApi + '/subapp_biz_log/get_my_subapp_biz_log_page_list', params, {})
}
