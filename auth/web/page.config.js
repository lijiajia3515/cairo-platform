// 请注意，`globalThis` 对象在一些旧版本的浏览器中可能不支持。如果你的目标环境需要兼容旧版浏览器，可以考虑使用其他方式来实现全局变量的定义和访问，例如使用模块化工具或全局状态管理库。 直接使用 configData.
// ^(?![0-9-])(?!.*-$)[a-zA-Z0-9-]{1,20}$
globalThis._this = {
    // 服务器地址
    server: 'http://127.0.0.1:10010',
    appid: 'cairo', // 应用ID
    manageSubappId: 'manage', //子应用ID
    manageSubappVersion: 'v1', //子应用ID
    client_id: 'cairo_web_v1', // 客户端ID
    client_secret: 'cairo_web_v1', //客户端密钥

    scope: null,
    callbackUrl: 'http://127.0.0.1:10010/open_api/oauth2/callback?redirect_uri=',
    api: {
        authApi: '', // auth 与 system 服务已合并，统一走该地址
    },

    storage: { // 存储本地字段重命名
        lastPath: 'cairo_lastPath',
        systemAppId: 'cairo_systemAppId', // 系统级字典
    },
    cookie: {
        token: 'cairo_user_access_token',
        refresh_token: 'cairo_user_refresh_token',
        token_type: 'cairo_user_token_type',
        auth_type: 'app_user',
        app_id: 'app_id',
        endpoint_id: 'endpoint_id',
        remember: 'cairo_remember',
        remember1: 'remember1' // 记住我 手机号
    },

    // 协议文件为本地静态资源（public/agreement/），部署后可直接修改无需重新构建
    userAgreement: '/agreement/user.html', // 用户协议
    privacyPolicy: '/agreement/privacy.html', // 隐私政策
}
