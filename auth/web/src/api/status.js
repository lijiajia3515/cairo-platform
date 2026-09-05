import {
  MessagePlugin
} from 'tdesign-vue-next';
import { debounce } from 'lodash';


import {
  getOauth2Token_api
} from '@/api';
import {
  setToken, getToken,
  setRefreshToken, getRefreshToken,
  setTokenType, getTokenType,
} from '@/utils';
import { clearLoginTraces } from '@/utils/clearLoginTraces';

const clearAndLogin = () => {
  let timer = setTimeout(() => {
    // 凭证过期被踢出:凭证/标签/子应用上下文等登录痕迹一并清除,
    // 避免换账号后恢复上一用户的标签页与子应用定位
    clearLoginTraces();
    window.location.reload();
    clearTimeout(timer)
  }, 800)
  // router.replace('/login');
}

export default async function(code, error, request) {
  switch (code) {
    case 'Request.NotFound':
      MessagePlugin.error('接口请求未找到');
      break;

    // 认证错误（Auth.TokenExpired 为后端 CairoAuthBusiness.TOKEN_EXPIRED 实际码值；Auth.Expired 为历史码，一并兼容）
    case 'Auth.Expired':
    case 'Auth.TokenExpired': {
      MessagePlugin.error('登录过期');
      // 访问凭证过期
      const isSuccess = await getRefreshTokenFunc();
      if (isSuccess) {
        // 重新请求接口
        let token = getToken();
        let tokenType = getTokenType();
        error.response.config.headers['authorization'] = tokenType.value + ' ' + token.value;
        if (error?.config?.url.includes('/app_user/get_my_subapp_user_menu')) { // 菜单接口过期必须刷新
          window.location.reload();
          return false;
        } else {
          return request(error.response.config);
        }
      } else {
        // 刷新token失败 跳转登录
        clearAndLogin();
      }
      break;
    }
    case 'Auth.OAuth2Error': // 认证错误
      MessagePlugin.error('认证错误');
      clearAndLogin();
      break;
    case 'Auth.OAuthError': // 刷新Token错误 OAuth2异常包装
      MessagePlugin.error('刷新Token错误');
      clearAndLogin();
      break;
    case 'Auth.Bad': // 凭证错误
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.Disabled': // 账号已禁用
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.Locked': // 账号已锁定
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.AccountNotFound':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.AccountDisabled':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.AccountLocked':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.TokenInvalid':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.AppUserNotFound':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.AppUserDisabled':
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.Denied': // 不允许访问
      MessagePlugin.error(error.response.data.message);
      return Promise.reject(error.response.data)
    case 'Auth.Unauthorized': // 需要认证
      MessagePlugin.error(error.response.data.message);
      clearAndLogin();
      break;
    case 'Auth.SnsCodeBad': // 账号不存在
      window.location.href = window.location.origin + '/login'
      clearAndLogin();
      break;
    default:
      MessagePlugin.error(error.response.data.message);
      return Promise.reject(error.response.data)
  }
}


let promise;
// 刷新token
const getRefreshTokenFunc = debounce(() => {
  if (promise) {
    return promise;
  }
  promise = new Promise((resolve, reject) => {
    // executor 内 async 逻辑抽出为独立异步函数，异常统一 reject
    (async () => {
      try {
        let freshToken = getRefreshToken();
        if (!freshToken.value) {
          MessagePlugin.success('刷新token失败，请重新登录');
          let timer = setTimeout(() => {
            clearAndLogin();
            clearTimeout(timer)
          }, 1000)
          return;
        }
        let headers = {
          'Accept-Language': 'zh-CH,zh;q=0.9,en;q=0.8',
          'Accept': 'application/json'
        };
        let formData = new FormData();
        formData.append('client_id', _this.client_id);
        formData.append('client_secret', _this.client_secret);
        // formData.append('tenant_id', _this.tenant_id);
        // formData.append('scoped', _this.scoped);

        formData.append('grant_type', 'app_user:app_user_refresh_token');
        formData.append('app_user_refresh_token', freshToken.value)
        const res = await getOauth2Token_api(formData, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('刷新令牌成功');
          let token = setToken();
          let refresh_token = setRefreshToken();
          let token_type = setTokenType();
          token.value = res.data.access_token;
          refresh_token.value = res.data.refresh_token;
          token_type.value = res.data.token_type;
          resolve(res.data.access_token);
        }
      } catch (err) {
        reject(false)
      }
    })();
  })
  promise.finally(() => {
    promise = null;
  });

  return promise;

})



