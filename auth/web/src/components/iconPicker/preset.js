// icon 库预置清单:iconify icon-park 多彩系(与既有彩色图标同族,CDN 直链免登录)
// 命名即语义,挑的是后台菜单常用意象;补充直接往数组加 icon-park 图标名即可
const CDN = 'https://api.iconify.design/icon-park';

export const iconUrl = (name) => `${CDN}/${name}.svg?height=64`;

export const ICON_PRESET_GROUPS = [
  {
    group: '通用',
    names: ['home', 'dashboard', 'menu', 'app-store', 'all-application', 'category-management', 'config', 'setting-two'],
  },
  {
    group: '账号与权限',
    names: ['user', 'user-positioning', 'peoples', 'id-card', 'key', 'lock', 'shield', 'user-security'],
  },
  {
    group: '业务与数据',
    names: ['business-man', 'enterprise', 'building-one', 'chart-line', 'chart-histogram', 'analysis', 'data-server', 'database-config'],
  },
  {
    group: '开发运维',
    names: ['code', 'code-laptop', 'terminal', 'api', 'plug-in', 'bug', 'log', 'history-query'],
  },
  {
    group: '消息通知',
    names: ['remind', 'message-one', 'email', 'send-email', 'announcement', 'alarm', 'phone-telephone', 'smile'],
  },
  {
    group: '操作',
    names: ['add', 'edit', 'delete', 'refresh', 'download', 'upload', 'link-two', 'more-app'],
  },
];
