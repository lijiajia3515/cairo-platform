// 账号管理与账号会话
import fetch from './fetch';
import { subappUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 账号-获取账号分页列表(cairo)
export const getAccountPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/get_account_page_list', params, {});
}

// 账号-获取账号列表(cairo)
export const getAccountList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/get_account_list', params, {});
}

// 账号-创建账号(cairo)
export const createAccount_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/create_account', params, {});
}

// 账号-修改账号信息(cairo)
export const modifyAccountInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/modify_account_info', params, {});
}

// 账号-修改账号状态(cairo)
export const modifyAccountStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/modify_account_status', params, {});
}

// 账号-管理员重置用户密码
export const resetAccountPassword_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/reset_account_password', params, {});
}

// 账号-删除账号(cairo)
export const deleteAccount_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/delete_account', params, {});
}

// 账号-注销账号(cairo)
export const logoffAccount_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/logoff_account', params, {});
}

// 账号-取消注销账号(cairo)
export const unlogoffAccount_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/unlogoff_account', params, {});
}

// 账号-修改账号锁定状态(cairo)
export const modifyAccountLockStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account/modify_account_lock_status', params, {});
}

// 账号会话分页列表
export const getAccountAuthorizationPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account_authorization/get_account_authorization_page_list', params, {});
}

// 账号会话下线
export const offlineAccountAuthorization_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account_authorization/offline_account_authorization', params, {});
}

// 账号会话全部下线
export const offlineAllAccountAuthorization_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/account_authorization/offline_all_account_authorization', params, {});
}

// 搜索账号
export const searchAccountInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/account/search_account_info', params, {});
}
