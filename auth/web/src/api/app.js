// 应用管理——应用/应用文件/应用发行
import fetch from './fetch';
import { subappUserApi, manageApi, manageSubappId, manageSubappVersion } from './urls';

// 应用-分页
export const getAppPageList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/get_app_page_list', params, {});
}

// 应用-列表
export const getAppList_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/get_app_list', params, {});
}

// 应用-创建
export const createApp_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/create_app', params, {});
}

// 应用-修改
export const modifyAppInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/modify_app_info', params, {});
}

// 应用-修改状态
export const modifyAppStatus_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/modify_app_status', params, {});
}

// 应用-删除
export const deleteApp_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/app/delete_app', params, {});
}

// 文件-获取临时文件上传签名
export const getAppFileUploadSign_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_file/get_upload_file_sign', params, {})
}

// 文件-获取公共文件上传签名
export const getPublicFileUploadSign_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/public_file/get_upload_file_sign', params, {})
}

// 文件-获取访问文件地址
export const accessAppFile_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/app_file/access_file', params, {})
}

// 文件-获取公共文件访问文件地址
export const accessPublicFile_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, subappUserApi + '/public_file/access_file', params, {})
}

// 获取应用发行分页列表
export const getAppReleasePageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/app_release/get_app_release_page_list', params, headers)
}

// 创建应用发行
export const createAppRelease_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/app_release/create_app_release', params, {})
}

// 更新应用发行信息
export const modifyAppReleaseInfo_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/app_release/modify_app_release_info', params, {})
}

// 删除应用发行
export const deleteAppRelease_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/app_release/delete_app_release', params, {})
}

// 设置为最新版本
export const setAppReleaseLatestVersion_api = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/app_release/set_app_release_latest_version', params, {})
}
