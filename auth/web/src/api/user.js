// 终端用户管理——用户/标签/部门/角色/系统会话
import fetch from './fetch';
import { subappUserApi, manageSubappId, manageSubappVersion } from './urls';

// ===== 用户管理接口 start =====
// 用户-分页
export const getAppUserPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/get_app_user_page_list', params, {});
}

// 用户-查询
export const getAppUserList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/get_app_user_list', params, {});
}

// 用户-创建
export const createAppUser_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/create_app_user', params, {});
}

// 创建账号并且创建用户
export const createAccountAndAppUser_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/create_account_and_app_user', params, {});
}

// 用户-修改信息
export const modifyAppUserInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/modify_app_user_info', params, {});
}

// 用户-注销
export const logoffAppUser_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/logoff_app_user', params, {});
}

// 用户-取消注销
export const unlogoffAppUser_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/unlogoff_app_user', params, {});
}

// 用户-删除
export const deleteAppUser_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/delete_app_user', params, {});
}

// 用户-修改状态
export const modifyAppUserStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/modify_app_user_status', params, {});
}

// 转移至其他账号
export const transferAppUserToOtherAccount_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user/transfer_app_user_to_other_account', params, {});
}

// 获取用户标签分页列表
export const getAppUserTagPage_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/get_app_user_tag_page_list', params, {});
}

// 获取用户标签列表
export const getAppUserTagList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/get_app_user_tag_list', params, {});
}

// 创建用户标签
export const createAppUserTag_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/create_app_user_tag', params, {});
}

// 修改用户标签信息
export const modifyAppUserTagInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/modify_app_user_tag_info', params, {});
}

// 修改用户标签状态
export const modifyAppUserTagStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/modify_app_user_tag_status', params, {});
}

// 删除用户标签
export const deleteAppUserTag_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_tag/delete_app_user_tag', params, {});
}

// 获取部门树形列表
export const getAppDepartmentTree_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_department/get_app_department_tree', params, headers)
}

// 创建部门
export const createAppDepartment_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_department/create_app_department', params, headers)
}

// 修改部门信息
export const modifyAppDepartment_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_department/modify_app_department_info', params, headers)
}

// 删除部门
export const deleteAppDepartment_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_department/delete_app_department', params, headers)
}

// 获取角色分页集合
export const getAppRolePageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/get_app_role_page_list', params, headers)
}

// 获取角色集合
export const getAppRoleList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/get_app_role_list', params, headers)
}

// 创建角色
export const createAppRole_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/create_app_role', params, headers)
}

// 修改角色信息
export const modifyAppRoleInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/modify_app_role_info', params, headers)
}

// 修改角色状态
export const modifyAppRoleStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/modify_app_role_status', params, headers)
}

// 删除角色
export const deleteAppRole_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/delete_app_role', params, headers)
}

// 修改角色权限
export const modifyAppRolePermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/modify_app_role_permission', params, headers)
}

// 获取角色菜单权限
export const getAppRolePermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/get_app_role_permission', params, headers)
}

// 获取角色子应用版本
export const getAppRoleSubappVersion_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/get_app_role_subapp_version', params, headers)
}

// 删除应用角色权限
export const deleteAppRolePermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_role/delete_app_role_permission', params, headers)
}

//登录会话分页列表
export const getAppUserAuthorizationPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_authorization/get_app_user_authorization_page_list', params, headers)
}

//登录会话下线
export const offlineAppUserAuthorization_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_authorization/offline_app_user_authorization', params, headers)
}

//登录会话全部下线
export const offlineAllAppUserAuthorization_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_user_authorization/offline_all_app_user_authorization', params, headers)
}
