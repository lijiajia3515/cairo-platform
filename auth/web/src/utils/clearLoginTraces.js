// 登录痕迹清除:凭证 cookie + 多标签缓存 + 子应用上下文 + 用户态,一次清干净。
// 所有退出/被踢出登录的路径(主动登出、token 刷新失败、守卫发现无凭证)统一调用——
// 残留旧用户的标签/子应用定位,会在版本更新、菜单/权限变更后引发幽灵页面与权限错乱。
import { useTagsViewStore } from '@/store/tagsView';
import { useSubappContextStore } from '@/store/subappContext';
import { useUserStore } from '@/store/user';

import {
  setToken,
  setRefreshToken,
  setTokenType,
  setAuthType,
  setAppId,
  setEndpointId,
} from '@/utils';

export const clearLoginTraces = () => {
  // 凭证类 cookie
  setToken().value = null;
  setRefreshToken().value = null;
  setTokenType().value = null;
  setAuthType().value = null;
  setAppId().value = null;
  setEndpointId().value = null;
  // 会话级缓存与内存态(调用点均在 Pinia 就绪后,守卫/组件事件内)
  useTagsViewStore().resetTags();
  useSubappContextStore().reset();
  useUserStore().savePermissionList([]);
  useUserStore().saveMenuList([]);
};
