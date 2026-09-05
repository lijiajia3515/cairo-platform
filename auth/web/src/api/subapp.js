// 子应用——子应用/版本/菜单/功能权限
import fetch from './fetch';
import { appUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 获取当前应用子应用列表
export const getCurrentSubappList_api = (params) => {
    return fetch.appUserPost(appUserApi + '/subapp/get_subapp_list', params, {});
}

// 获取当前应用子应用版本列表
export const getCurrentSubappVersionList_api = (params) => {
    return fetch.appUserPost(appUserApi + '/subapp_version/get_subapp_version_list', params, {});
}

// 菜单_树结构
export const getMenuTree_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/get_menu_tree_list', params, headers);
}

// 子应用分页列表
export const getSubappPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/get_subapp_page_list', params, headers)
}

// 修改子应用状态
export const modifySubappStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/modify_subapp_status', params, headers)
}

//删除子应用
export const deleteSubapp_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/delete_subapp', params, headers)
}

//移动子应用
export const moveSubapp_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/move_subapp', params, headers)
}

//创建子应用
export const createSubapp_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/create_subapp', params, headers)
}

//编辑子应用信息
export const modifySubappInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/modify_subapp_info', params, headers)
}

//子应用列表
export const getSubappList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp/get_subapp_list', params, headers)
}

//子应用版本列表
export const getSubappVersionPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/get_subapp_version_page_list', params, headers)
}

//子应用版本
export const getSubappVersionList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/get_subapp_version_list', params, headers)
}

//子应用版本删除
export const deleteSubappVersion_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/delete_subapp_version', params, headers)
}

//子应用版本创建
export const createSubappVersion_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/create_subapp_version', params, headers)
}

//子应用版本编辑
export const modifySubappVersion_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/modify_subapp_version_info', params, headers)
}

//子应用版本状态
export const modifySubappVersionStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/modify_subapp_version_status', params, headers)
}

//子应用版本同步
export const syncSubappVersion_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/subapp_version/sync_subapp_version', params, headers)
}

// 菜单
export const getMenuPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/get_menu_page_list', params, headers);
}

export const modifyMenu_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/modify_menu', params, headers);
}

export const createMenu_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/create_menu', params, headers);
}

export const deleteMenu_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/delete_menu', params, headers);
}

export const moveMenu_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/menu/move_menu', params, headers);
}

// 功能权限 权限_分页
export const getPermissionPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/get_permission_page_list', params, headers)
}

// 功能权限 权限_列表
export const getPermissionList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/get_permission_list', params, headers)
}

// 功能权限 权限_创建
export const createPermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/create_permission', params, headers)
}

// 功能权限 权限_修改
export const modifyPermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/modify_permission', params, headers)
}

// 功能权限 权限_删除
export const deletePermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/delete_permission', params, headers)
}

// 功能权限 权限_移动
export const movePermission_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/permission/move_permission', params, headers)
}
