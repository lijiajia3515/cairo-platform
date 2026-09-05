export default (path) => {

  let obj = {

    // '/index': () => import('@/views/index/index.vue'),
    '/home': () => import('@/views/home'),


    // 账号域
    '/manage/account': () => import('@/views/account/list'),
    '/manage/account/session': () => import('@/views/account/session'),

    // 企业域
    '/manage/tenant': () => import('@/views/tenant/list'),
    '/manage/tenant/app': () => import('@/views/tenant/app'),
    '/manage/tenant/endpoint': () => import('@/views/tenant/endpoint'),
    '/manage/tenant/subapp': () => import('@/views/tenant/subapp'),

    // 开发域
    '/manage/develop/app': () => import('@/views/app/list'),
    '/manage/develop/endpoint': () => import('@/views/endpoint/list'),
    '/manage/develop/subapp': () => import('@/views/develop/subapp'),
    '/manage/develop/subapp/version': () => import('@/views/develop/subappVersion'),
    '/manage/develop/menu': () => import('@/views/develop/menu'),
    '/manage/develop/client': () => import('@/views/client/list'),
    '/manage/develop/permission': () => import('@/views/develop/permission'),
    '/manage/develop/sns_provider': () => import('@/views/develop/snsProvider'),
    '/manage/develop/app_release': () => import('@/views/develop/appRelease'),
    '/manage/develop/area': () => import('@/views/develop/area'),
    '/manage/develop/link': () => import('@/views/develop/link'),
    '/manage/develop/sys_dict': () => import('@/views/develop/sysDict'),
    '/manage/develop/login_log': () => import('@/views/develop/loginLog'),
    '/manage/develop/biz_log': () => import('@/views/develop/bizLog'),

    // 短信域
    '/manage/sms/template': () => import('@/views/sms/template'),
    '/manage/sms/msg': () => import('@/views/sms/msg'),

    // 微信公众号域
    '/manage/wxmp/provider': () => import('@/views/wxmp/provider'),
    '/manage/wxmp/template_msg': () => import('@/views/wxmp/templateMsg'),
    '/manage/wxmp/template_msg_record': () => import('@/views/wxmp/templateMsgRecord'),

    // 通知域
    '/manage/notify/category': () => import('@/views/notify/category'),
    '/manage/notify/template': () => import('@/views/notify/template'),
    '/manage/notify/record': () => import('@/views/notify/record'),

    // 通讯录
    '/manage/contact/user': () => import('@/views/system/user'),
    '/manage/contact/department': () => import('@/views/system/department'),
    '/manage/contact/user_tag': () => import('@/views/system/userTag'),
    '/manage/contact/user_group': () => import('@/views/system/userGroup'),
    '/manage/system/role': () => import('@/views/system/userRole'),
    '/manage/system/session': () => import('@/views/system/session'),

    // 管理首页(manage 子应用菜单入口)
    '/manage/home': () => import('@/views/manageHome'),

    // 个人
    '/profile': () => import('@/views/main/userInfo'),

    // 看板子应用首页:路由不可与子应用前缀 /dashboard 重名,取 /dashboard/index
    // (与 /manage/home 同理:前缀根留给子应用域,页面路由独立命名)
    '/dashboard/index': () => import('@/views/dashboard/index.vue'),

  }

  return obj[path] || null;
}
