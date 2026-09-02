// URL 前缀常量——统一走 authApi
const openApi = _this.api.authApi + '/open_api';          // 开放 API:登录/注册/验证码
const appUserApi = _this.api.authApi + '/app_user_api';   // 终端用户上下文:个人中心/当前终端
const subappUserApi = _this.api.authApi + '/subapp_user_api'; // 子应用用户上下文
const manageApi = _this.api.authApi + '/cairo_web_manage_api'; // 运营管理台

// 运营子应用标识(管理台请求经 fetch.subappPost 注入)
const manageSubappId = _this.manageSubappId;
const manageSubappVersion = _this.manageSubappVersion;

export { openApi, appUserApi, subappUserApi, manageApi, manageSubappId, manageSubappVersion };
