export default (path) => {

  let obj = {

    // '/index': () => import('@/views/index/index.vue'),
    '/home': () => import('@/views/home'),


    '/system/app_user_tag': () => import('@/views/system/appUserTag'),
    '/system/department': () => import('@/views/system/department'),
    '/system/app_role': () => import('@/views/system/appRole'),
    '/system/session': () => import('@/views/system/session'),
    '/system/app_user': () => import('@/views/system/appUser'),

    '/main/userInfo': () => import('@/views/main/userInfo'),

    // '/user/myInfo': () => import('@/views/main/user/myInfo'),
    '/account_center/account': () => import('@/views/account/list'),
    '/account_center/account_session': () => import('@/views/account/session'),

    '/tenant_center/tenant': () => import('@/views/tenant/list'), // 企业

    '/develop/app': () => import('@/views/app/list'), // 应用
    '/develop/endpoint': () => import('@/views/endpoint/list'), // 终端
    '/develop/client': () => import('@/views/client/list'), // 客户端
    '/develop/menu': () => import('@/views/develop/menu'), // 菜单
    '/develop/subapp': () => import('@/views/develop/subapp'), // 子应用
    '/develop/subappVersion': () => import('@/views/develop/subappVersion'), // 子应用版本
    '/develop/permission': () => import('@/views/develop/permission'), // 功能权限
    '/develop/app_release': () => import('@/views/develop/appRelease'), // 应用发行
    '/develop/sys_dict': () => import('@/views/develop/sysDict'), // 系统级字典
    '/develop/login_log': () => import('@/views/develop/loginLog'), // 登录日志
    '/develop/biz_log': () => import('@/views/develop/bizLog'), // 业务日志
    '/develop/sns_provider': () => import('@/views/develop/snsProvider'), // 第三方账号
    '/develop/area': () => import('@/views/develop/area'), //行政区划
    '/develop/link': () => import('@/views/develop/link'), //短链



    '/profile/userinfo': () => import('@/views/user/info'),
    '/profile/login_log': () => import('@/views/user/loginLog'),
    '/profile/biz_log': () => import('@/views/user/logBiz'),

    '/tenant_center/app': () => import('@/views/tenant/app'), // 企业应用
    '/tenant_center/endpoint': () => import('@/views/tenant/endpoint'), // 企业终端
    '/tenant_center/subapp': () => import('@/views/tenant/subapp'), // 企业子应用



    '/sms/template': () => import('@/views/sms/template'), // 短信模板
    '/sms/msg': () => import('@/views/sms/msg'), // 短信消息

    '/wxmp/provider': () => import('@/views/wxmp/provider'), // 公众号管理
    '/wxmp/template_msg': () => import('@/views/wxmp/templateMsg'), // 公众号模板消息
    '/wxmp/template_msg_record': () => import('@/views/wxmp/templateMsgRecord'), // 公众号模板消息记录

    '/notify/category': () => import('@/views/notify/category'),
    '/notify/template': () => import('@/views/notify/template'),
    '/notify/record': () => import('@/views/notify/record'),





  }

  return obj[path] || null;
}
