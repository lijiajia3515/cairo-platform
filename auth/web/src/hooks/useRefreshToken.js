
import {
  MessagePlugin
} from 'tdesign-vue-next';
import { debounce } from 'lodash';

import { getRefreshToken, setRefreshToken, setTokenType, setToken, setAuthType, setAppId, setEndpointId } from '@/utils';
import {
  getOauth2Token_api
} from '@/api';

// 必定是存在刷新token 才会调用
export default function() {
  return new Promise(debounce(async (resolve, reject) => {
    try {
      let refreshToken = getRefreshToken();
      let headers = {
        'Accept-Language': 'zh-CH,zh;q=0.9,en;q=0.8',
        'Accept': 'application/json'
      };
      let formData = new FormData();
      formData.append('client_id', _this.client_id);
      formData.append('client_secret', _this.client_secret);
      formData.append('grant_type', 'app_user:app_user_refresh_token');
      formData.append('app_user_refresh_token', refreshToken.value);
      const res = await getOauth2Token_api(formData, headers);
      if (res.code == 'Success') {
        MessagePlugin.success('刷新令牌成功');
        let token = setToken();
        let refresh_token = setRefreshToken();
        let token_type = setTokenType();
        let auth_type = setAuthType();
        let appId = setAppId();
        let endpointId = setEndpointId();
        token.value = res.data.access_token;
        refresh_token.value = res.data.refresh_token;
        token_type.value = res.data.token_type;
        auth_type.value = res.data.auth_type;
        appId.value = res.data.app_id;
        endpointId.value = res.data.endpoint_id;
        resolve(res.data.access_token);
      }
    } catch (err) {
      console.log(err, '刷新失败')
      // 刷新令牌失败 跳转登录
      reject(false)
    }
  }))
}