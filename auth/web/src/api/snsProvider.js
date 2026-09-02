// 第三方账号厂商与类型
import fetch from './fetch';
import { manageApi, manageSubappId, manageSubappVersion } from './urls';

// 第三方账号厂商
export const getProviderTypeList = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/get_provider_type_list', params, {});
}

// 第三方账号厂商
export const getProviderPartnerList = (params) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/get_provider_partner_list', params, {});
}

//第三方认证 分页列表
export const getSnsProviderPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/get_sns_provider_page_list', params, headers)
}

//第三方认证 列表
export const getSnsProviderList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/get_sns_provider_list', params, headers)
}

//第三方认证 创建
export const createSnsProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/create_sns_provider', params, headers)
}

//第三方认证 修改
export const modifySnsProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/modify_sns_provider', params, headers)
}

//第三方认证 删除
export const deleteSnsProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/delete_sns_provider', params, headers)
}

//第三方认证 编辑状态
export const modifySnsProviderStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sns_provider/modify_sns_provider_status', params, headers)
}
