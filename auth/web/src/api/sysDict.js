// 系统级字典
import fetch from './fetch';
import { subappUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 获取系统级字典分页列表 父项
export const getSysDictPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/get_sys_dict_page_list', params, headers)
}

// 创建系统级字典 父项
export const createSysDict_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/create_sys_dict', params, headers)
}

// 修改系统级字典信息 父项
export const modifySysDictInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/modify_sys_dict_info', params, headers)
}

// 删除系统级字典
export const deleteSysDict_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/delete_sys_dict', params, headers)
}

// 同步系统级字典
export const syncSysDict_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/sync_sys_dict', params, headers)
}

//克隆
export const copySysDictByAppId_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/copy_sys_dict_by_app_id', params, headers)
}

//拷贝
export const copySysDictByDictId_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/copy_sys_dict_by_dict_id', params, headers)
}

// 获取系统级字典详细信息
export const getSubappUserSysDictDetailInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/sys_dict/get_sys_dict_detail_info', params, headers)
}

// 获取系统级字典项分页 子项
export const getSysDictItemPage_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/get_sys_dict_item_page_list', params, headers)
}

// 添加系统级字典项 子项
export const putSysDictItem_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/put_sys_dict_item', params, headers)
}

// 修改系统级字典项 子项
export const modifySysDictItem_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/modify_sys_dict_item_info', params, headers)
}

// 修改系统级字典项状态 子项
export const modifySysDictItemStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/modify_sys_dict_item_status', params, headers)
}

// 删除系统级字典项 子项
export const deleteSysDictItem_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/delete_sys_dict_item', params, headers)
}

// 获取系统级字典详细信息
export const getSysDictDetailInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/get_sys_dict_detail_info', params, headers)
}

// 移动系统级字典详细信息
export const moveSysDictItem_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sys_dict/move_sys_dict_item', params, headers)
}
