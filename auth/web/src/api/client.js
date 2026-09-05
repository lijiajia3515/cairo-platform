// 客户端管理
import fetch from './fetch';
import { appUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 获取当前应用客户端列表
export const getCurrentClientList_api = (params) => {
    return fetch.appUserPost(appUserApi + '/client/get_client_list', params, {});
}

// 客户端-分页
export const getClientPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/get_client_page_list', params, {});
}

// 客户端-列表
export const getClientList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/get_client_list', params, {});
}

// 客户端-创建
export const createClient_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/create_client', params, {});
}

// 客户端-修改
export const modifyClientInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/modify_client_info', params, {});
}

// 客户端-修改状态
export const modifyClientStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/modify_client_status', params, {});
}

// 客户端-修改状态
export const deleteClient_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/delete_client', params, {});
}

// 客户端-修改秘钥
export const modifyClientSecret_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/client/modify_client_secret', params, {});
}
