// 终端管理
import fetch from './fetch';
import { appUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 获取当前终端列表
export const getCurrentEndpointList_api = (params) => {
    return fetch.appUserPost(appUserApi + '/endpoint/get_endpoint_list', params, {});
}

// 终端-分页
export const getEndpointPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/get_endpoint_page_list', params, {});
}

// 终端-列表
export const getEndpointList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/get_endpoint_list', params, {});
}

// 终端-创建
export const createEndpoint_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/create_endpoint', params, {});
}

// 终端-修改
export const modifyEndpointInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/modify_endpoint_info', params, {});
}

// 终端-修改状态
export const modifyEndpointStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/modify_endpoint_status', params, {});
}

// 终端-删除
export const deleteEndpoint_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/endpoint/delete_endpoint', params, {});
}
