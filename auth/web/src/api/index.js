// API 单入口——按领域拆分,全部 re-export;引用方统一 from '@/api'
// 传输上下文:fetch.post(open)/appUserPost(app_user)/subappPost(管理台)
export * from './open'; // 开放接口——登录/注册/验证码(open_api)
export * from './personal'; // 个人中心——我的账号/用户信息/登录会话/我的日志(app_user_api)
export * from './account'; // 账号管理与账号会话
export * from './user'; // 应用级用户管理——用户/标签/部门/角色/系统会话
export * from './app'; // 应用管理——应用/应用文件/应用发行
export * from './endpoint'; // 终端管理
export * from './client'; // 客户端管理
export * from './subapp'; // 子应用——子应用/版本/菜单/功能权限
export * from './snsProvider'; // 第三方账号厂商与类型
export * from './log'; // 日志管理——登录日志/业务日志
export * from './tenant'; // 企业管理——tenant 四件套
export * from './sysDict'; // 系统级字典
export * from './message'; // 消息——短信/微信模板消息/通知消息
export * from './link'; // 短链管理
export * from './area'; // 行政区划
