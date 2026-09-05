// URL 前缀常量——统一走 authApi
const openApi = _this.api.authApi + '/open_api';          // 开放 API:登录/注册/验证码
const appUserApi = _this.api.authApi + '/app_user_api';   // 应用级用户上下文:个人中心/当前终端
const subappUserApi = _this.api.authApi + '/subapp_user_api'; // 子应用级用户上下文
const manageApi = _this.api.authApi + '/cairo_web_manage_api'; // 运营管理台

// 子应用请求上下文(全部 subappPost 调用经此注入;ES 模块活绑定,切换子应用时
// 由 subappContext store 调 setSubappContext 重赋值,40+ 个 api 文件零改动跟随)
let manageSubappId = _this.manageSubappId;
let manageSubappVersion = _this.manageSubappVersion;

export const setSubappContext = (subappId, subappVersion) => {
  manageSubappId = subappId;
  manageSubappVersion = subappVersion;
};

export { openApi, appUserApi, subappUserApi, manageApi, manageSubappId, manageSubappVersion };
