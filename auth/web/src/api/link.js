// 短链管理
import fetch from './fetch';
import { manageApi, manageSubappId, manageSubappVersion } from './urls';

// 短链分页接口
export const getLinkPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/link/get_link_page_list', params, headers)
}

// 短链修改状态
export const modifyLinkStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/link/modify_link_status', params, headers)
}

// 短链删除
export const deleteLink_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/link/delete_link', params, headers)
}

// 短链创建
export const createLink_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/link/create_link', params, headers)
}
