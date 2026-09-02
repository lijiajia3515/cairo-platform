// 行政区划
import fetch from './fetch';
import { openApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 区域树形列表
export const getAreaList_api = (params) => {
    return fetch.post(openApi + '/area/get_area_list', params, {})
}

// 行政区划 分页列表
export const getAreaPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/get_area_page_list', params, headers)
}

// 行政区划 状态
export const modifyAreaStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/modify_area_status', params, headers)
}

// 行政区划 热门
export const modifyAreaHot_api = (params, headers) => {
    return fetch.post(manageApi + '/area/modify_area_hot', params, headers)
}

// 行政区划 删除
export const deleteArea_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/delete_area', params, headers)
}

// 行政区划 移动
export const moveArea_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/move_area', params, headers)
}

// 行政区划 创建
export const createArea_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/create_area', params, headers)
}

// 行政区划 编辑
export const modifyAreaInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/area/modify_area_info', params, headers)
}
