// 日志管理——登录日志/业务日志
import fetch from './fetch';
import { manageApi, manageSubappId, manageSubappVersion } from './urls';

// 登录日志
// 获取账号登录日志分页列表
export const getAccountLoginLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account_login_log/get_account_login_log_page_list', params, headers)
}

// 获取客户端登录日志分页列表
export const getClientLoginLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client_login_log/get_client_login_log_page_list', params, headers)
}

// 获取应用级用户(app_user,UI 显示为"应用")登录日志分页列表
export const getAppUserLoginLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app_user_login_log/get_app_user_login_log_page_list', params, headers)
}

// 获取企业应用级用户(tenant_app_user,UI 显示为"企业应用")登录日志分页列表
export const getTenantAppUserLoginLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app_user_login_log/get_tenant_app_user_login_log_page_list', params, headers)
}

// 获取开放级业务日志
export const getOpenBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/open_biz_log/get_open_biz_log_page_list', params, headers)
}

// 获取账号级业务日志
export const getAccountBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account_biz_log/get_account_biz_log_page_list', params, headers)
}

// 获取应用级业务日志
export const getAppBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app_biz_log/get_app_biz_log_page_list', params, headers)
}

// 获取子应用级业务日志
export const getSubappBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_biz_log/get_subapp_biz_log_page_list', params, headers)
}

// 获取企业应用级业务日志
export const getTenantAppBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app_biz_log/get_tenant_app_biz_log_page_list', params, headers)
}

// 获取企业子应用级业务日志
export const getTenantSubappBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_subapp_biz_log/get_tenant_subapp_biz_log_page_list', params, headers)
}

// 获取客户端级业务日志
export const getClientBizLogPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client_biz_log/get_client_biz_log_page_list', params, headers)
}
