// 开放接口——登录/注册/验证码(open_api)
import fetch from './fetch';
import { openApi } from './urls';

// 登录
export const getOauth2Token_api = (params, headers) => {
    return fetch.post(openApi + '/oauth2/token', params, headers)
}

// 重置账号密码(根据手机号验证码)
export const resetAccountPasswordByPhone_api = (params, headers) => {
    return fetch.post(openApi + '/account/reset_account_password_by_phone_number', params, headers)
}

// 注册
export const registerAppUser_api = (params) => {
    return fetch.post(openApi + '/app_user/register_app_user', params, {})
}

// 图形验证码
export const getCaptchaCode_api = (params) => {
    return fetch.post(openApi + '/captcha/get_captcha_code', params, {})
}

// 验证行为验证码
export const verifyCaptchaCode_api = (params) => {
    return fetch.post(openApi + '/captcha/verify_captcha_code', params, {})
}

// 发送手机验证码
export const sendVerifyCodeSms_api = (params, headers) => {
    return fetch.post(openApi + '/verify_code/send_verify_code_sms', params, headers)
}

// 获取微信openid
export const getSnsToken_api = (params) => {
    return fetch.post(openApi + '/sns/get_sns_token', params, {})
}

// 三方认证列表
export const getEnabledSnsProviderList_api = (params) => {
    return fetch.post(openApi + '/sns_provider/get_sns_provider_list', params, {})
}
