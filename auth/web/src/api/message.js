// 消息——短信/微信模板消息/通知消息
import fetch from './fetch';
import { manageApi, manageSubappId, manageSubappVersion } from './urls';

//微信公众号 分页列表
export const getWxmpProviderPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/get_wxmp_provider_page_list', params, headers)
}

export const getWxmpProviderList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/get_wxmp_provider_list', params, headers)
}

//微信公众号 创建
export const createWxmpProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/create_wxmp_provider', params, headers)
}

//微信公众号 修改
export const modifyWxmpProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/modify_wxmp_provider', params, headers)
}

//微信公众号 删除
export const deleteWxmpProvider_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/delete_wxmp_provider', params, headers)
}

//微信公众号 修改状态
export const modifyWxmpProviderStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_provider/modify_wxmp_provider_status', params, headers)
}

// 微信公众号模板 消息分页列表
export const getWxmpTemplateMsgPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/get_wxmp_template_msg_page_list', params, headers)
}

// 微信公众号模板 创建
export const createWxmpTemplateMsg_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/create_wxmp_template_msg', params, headers)
}

// 微信公众号模板 修改
export const modifyWxmpTemplateMsgInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/modify_wxmp_template_msg_info', params, headers)
}

// 微信公众号模板 详情
export const getWxmpTemplateMsgDetailInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/get_wxmp_template_msg_detail_info', params, headers)
}

// 微信公众号模板 删除
export const deleteWxmpTemplateMsg_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/delete_wxmp_template_msg', params, headers)
}

// 微信公众号模板 编辑状态
export const modifyWxmpTemplateMsgStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/modify_wxmp_template_msg_status', params, headers)
}

// 微信公众号模板 消息列表
export const getWxmpTemplateMsgList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg/get_wxmp_template_msg_list', params, headers)
}

// 微信公众号模板 消息记录分页列表
export const getWxmpTemplateMsgRecordPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg_record/get_wxmp_template_msg_record_page_list', params, headers)
}

// 微信公众号模板 消息记录分页列表
export const retryWxmpTemplateMsgRecord_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/wxmp_template_msg_record/retry_wxmp_template_msg_record', params, headers)
}

// 获取短信模板分页列表
export const getSmsTemplatePageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/get_sms_template_page_list', params, headers)
}

// 添加短信模板
export const createSmsTemplate_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/create_sms_template', params, headers)
}

// 修改短信模板信息
export const modifySmsTemplateInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/modify_sms_template_info', params, headers)
}

// 删除短信模板
export const deleteSmsTemplate_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/delete_sms_template', params, headers)
}

// 修改短信模板状态
export const modifySmsTemplateStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/modify_sms_template_status', params, headers)
}

// 获取短信模板详细信息（单个）
export const getSmsTemplateDetailInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/get_sms_template_detail_info', params, headers)
}

// 获取短信模板列表
export const getSmsTemplateList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/sms_template/get_sms_template_list', params, headers)
}

// 获取短信消息分页列表
export const getSmsMsgPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/sms_msg/get_sms_msg_page_list', params, headers)
}

// 重试短信消息
export const retrySmsMsg_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion,manageApi + '/sms_msg/retry_sms_msg', params, headers)
}

// 移动端通知
// 通知消息分类 分页
export const getNotifyCategoryPageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/get_notify_category_page_list', params, headers)
}

// 通知消息模板 列表
export const getNotifyCategoryList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/get_notify_category_list', params, headers)
}

// 通知消息分类 创建
export const createNotifyCategory_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/create_notify_category', params, headers)
}

// 通知消息分类 修改
export const modifyNotifyCategoryInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/modify_notify_category_info', params, headers)
}

// 通知消息分类 删除
export const deleteNotifyCategory_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/delete_notify_category', params, headers)
}

// 通知消息分类 修改状态
export const modifyNotifyCategoryStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_category/modify_notify_category_status', params, headers)
}

// 通知消息模板 分页
export const getNotifyTemplatePageList_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/get_notify_template_page_list', params, headers)
}

// 通知消息模板 修改状态
export const modifyNotifyTemplateStatus_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/modify_notify_template_status', params, headers)
}

// 通知消息模板 删除
export const deleteNotifyTemplate_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/delete_notify_template', params, headers)
}

// 通知消息模板 创建
export const createNotifyTemplate_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/create_notify_template', params, headers)
}

// 通知消息模板 编辑
export const modifyNotifyTemplateInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/modify_notify_template_info', params, headers)
}

// 通知消息模板 详情
export const getNotifyTemplateDetailInfo_api = (params, headers) => {
    return fetch.subappPost(manageSubappId, manageSubappVersion, manageApi + '/notify_template/get_notify_template_detail_info', params, headers)
}
