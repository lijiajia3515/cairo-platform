import axios from "axios";
import {
  MessagePlugin
} from 'tdesign-vue-next';

import getStatusCode from './status';

// 注意：不要在这里默认 'Content-Type': 'application/json'。
// axios 对普通对象负载会自动设置 application/json；而 OAuth2 token 等接口用 FormData 传参，
// 若默认头是 application/json，axios 会把 FormData 转成 JSON body，服务端将读不到 grant_type 等参数。
var request = axios.create({
  baseURL: _this.server,
})

request.interceptors.request.use((config) => {
  return config;
})

request.interceptors.response.use((res) => {
  // 自定义接口返回值
  let json = {
    code: res.data.code,
    data: res.data.data,
    status: res.status,
  }
  return json;
}, async function(error) {
  // 无响应体：网络中断、跨域被拦、超时
  if (!error.response) {
    MessagePlugin.error('服务端错误, 响应超时');
    return Promise.reject(error);
  }
  // 网关 502/504 等场景下响应体可能是 HTML 而非业务 JSON
  const code = error.response.data?.code;
  if (!code) {
    MessagePlugin.error(`请求失败 (HTTP ${error.response.status})`);
    return Promise.reject(error);
  }
  return getStatusCode(code, error, request);

})

export default request;
