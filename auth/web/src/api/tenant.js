// 企业管理——tenant 四件套
import fetch from './fetch';
import { manageApi, manageSubappId, manageSubappVersion } from './urls';

// ===== 企业（tenant）管理接口 start =====
// 获取企业分页
export const getTenantPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/get_tenant_page_list', params, {});
}

// 获取企业列表
export const getTenantList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/get_tenant_list', params, {});
}

// 创建企业
export const createTenant_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/create_tenant', params, {});
}

// 修改企业信息
export const modifyTenantInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/modify_tenant_info', params, {});
}

// 修改企业状态
export const modifyTenantStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/modify_tenant_status', params, {});
}

// 删除企业
export const deleteTenant_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/delete_tenant', params, {});
}

// 修改企业拥有者
export const modifyTenantOwner_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant/modify_tenant_owner', params, {});
}

// ===== 企业应用（tenant_app）接口 start =====
// 企业应用-获取企业应用分页列表
export const getTenantAppPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/get_tenant_app_page_list', params, {});
}

// 企业应用-获取企业应用列表
export const getTenantAppList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/get_tenant_app_list', params, {});
}

// 企业应用-创建
export const createTenantApp_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/create_tenant_app', params, {});
}

// 企业应用-修改信息
export const modifyTenantAppInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/modify_tenant_app_info', params, {});
}

// 企业应用-修改状态
export const modifyTenantAppStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/modify_tenant_app_status', params, {});
}

// 企业应用-删除企业应用
export const deleteTenantApp_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_app/delete_tenant_app', params, {});
}

// 获取企业终端分页列表
export const getTenantEndpointPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_endpoint/get_tenant_endpoint_page_list', params, {});
}

// 获取企业终端列表
export const getTenantEndpointList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_endpoint/get_tenant_endpoint_list', params, {});
}

// 创建企业终端
export const createTenantEndpoint_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_endpoint/create_tenant_endpoint', params, {});
}

// 修改企业终端状态
export const modifyTenantEndpointStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_endpoint/modify_tenant_endpoint_status', params, {});
}

// 删除企业终端
export const deleteTenantEndpoint_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_endpoint/delete_tenant_endpoint', params, {});
}

//企业子应用分页
export const getTenantSubappPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_subapp/get_tenant_subapp_page_list', params, headers)
}

//企业子应用创建
export const createTenantSubapp_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_subapp/create_tenant_subapp', params, headers)
}

//企业子应用删除
export const deleteTenantSubapp_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_subapp/delete_tenant_subapp', params, headers)
}

//企业子应用状态
export const modifyTenantSubappStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/tenant_subapp/modify_tenant_subapp_status', params, headers)
}
