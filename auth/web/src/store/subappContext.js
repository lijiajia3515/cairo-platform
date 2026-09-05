// 子应用上下文:切换器的数据与切换动作(混合地基——同仓子应用路由重建,外链子应用未来走 iframe)
import { defineStore } from 'pinia';

import { setSubappContext } from '@/api/urls';
import { getCurrentSubappList_api } from '@/api/subapp';
import { getSubappUserMenuByContext_api, getSubappUserPermissionIdsByContext_api } from '@/api/personal';
import { useUserStore } from '@/store/user';

const CURRENT_KEY = 'cairo_subapp_current';

const readCurrent = () => {
  try {
    return JSON.parse(sessionStorage.getItem(CURRENT_KEY) || 'null');
  } catch (e) {
    return null;
  }
};

export const useSubappContextStore = defineStore('subappContext', {
  state: () => ({
    loaded: false,
    // [{ subappId, subappVersion, subappName, menus, permissions }]
    list: [],
    current: readCurrent() || { subappId: _this.manageSubappId, subappVersion: _this.manageSubappVersion, subappName: '管理台' },
  }),
  getters: {
    // 可见 = 有菜单的子应用;仅 1 个时切换条整体隐藏。
    // 顺序:后端无排序(Mongo 自然序=插入序,先建在前),前端兜底 manage 置首、其余按名称稳定排
    visible: (state) =>
      state.list
        .filter((s) => s.menus?.length)
        .slice()
        .sort((a, b) => {
          const rank = (x) => (x.subappId === _this.manageSubappId ? 0 : 1);
          return rank(a) - rank(b) || String(a.subappName).localeCompare(String(b.subappName), 'zh-CN');
        }),
    // 首页语义:当前子应用首个真实菜单路由(深度优先首个可见叶子,且有子菜单的分组节点
    // 不算——其 component 是分组值非可渲染路由);无命中兜底平台虚拟路由 /home
    homePath(state) {
      const entry = state.list.find((s) => s.subappId === state.current.subappId);
      const walk = (nodes) => {
        for (const n of nodes || []) {
          if (n.hiddenMenu) continue;
          // 叶子节点:无子菜单且带组件才算真实路由
          if (n.component && (!n.menus || !n.menus.length)) return n.component;
          const deeper = walk(n.menus);
          if (deeper) return deeper;
        }
        return '';
      };
      return walk(entry?.menus) || '/home';
    },
  },
  actions: {
    // 启动期:把持久化的当前子应用应用到请求上下文(在任何 subappPost 之前调用)
    initCurrent() {
      setSubappContext(this.current.subappId, this.current.subappVersion);
    },
    // 退出登录清痕:回管理台默认上下文,清内存清单与持久化 current,
    // 避免换账号后沿用上一用户的子应用定位(旧账号无该子应用权限时菜单会拉空)
    reset() {
      this.loaded = false;
      this.list = [];
      this.current = { subappId: _this.manageSubappId, subappVersion: _this.manageSubappVersion, subappName: '管理台' };
      sessionStorage.removeItem(CURRENT_KEY);
      setSubappContext(this.current.subappId, this.current.subappVersion);
    },
    // 拉子应用清单与各自菜单/权限(菜单非空即有入口)。
    // 内存态缓存(pig 同款):登录/刷新首拉一次,之后切换子应用零远端请求;
    // 刷新页面即重拉——菜单/权限后端变更最迟下次刷新生效,不必退出登录。
    // 版本一律用清单返回的基线——菜单变更随前端发版管控,不做版本探测动态兜底
    async load() {
      if (this.loaded) return;
      try {
        const res = await getCurrentSubappList_api({});
        if (res.code !== 'Success') return;
        const entries = await Promise.all((res?.data || []).map(async (s) => {
          const version = s.subappVersion || _this.manageSubappVersion;
          const [menus, permissions] = await Promise.all([
            getSubappUserMenuByContext_api(s.subappId, version, {})
              .then((mr) => (mr.code === 'Success' ? mr?.data || [] : []))
              .catch(() => []), // 菜单拉取失败视为无入口
            getSubappUserPermissionIdsByContext_api(s.subappId, version, {})
              .then((pr) => (pr.code === 'Success' ? pr?.data || [] : []))
              .catch(() => []),
          ]);
          return { subappId: s.subappId, subappVersion: version, subappName: s.subappName || s.subappId, menus, permissions };
        }));
        this.list = entries;
        this.loaded = true;
        this.fixStaleCurrent();
      } catch (e) {
        console.log('subapp context load fail', e);
      }
    },
    // 持久化的 current 不在清单(子应用下线/换账号残留)时回管理台默认,
    // 避免带着失效上下文拉空菜单
    fixStaleCurrent() {
      if (this.list.some((s) => s.subappId === this.current.subappId)) return;
      this.current = { subappId: _this.manageSubappId, subappVersion: _this.manageSubappVersion, subappName: '管理台' };
      sessionStorage.removeItem(CURRENT_KEY);
      setSubappContext(this.current.subappId, this.current.subappVersion);
    },
    // 换芯:请求上下文/菜单树/权限随子应用切换,不清标签(跨子应用标签共存的前提)
    applyContext(subappId) {
      const target = this.list.find((s) => s.subappId === subappId);
      if (!target) return false;
      if (target.subappId === this.current.subappId) return true;
      this.current = { subappId: target.subappId, subappVersion: target.subappVersion, subappName: target.subappName };
      sessionStorage.setItem(CURRENT_KEY, JSON.stringify(this.current));
      setSubappContext(target.subappId, target.subappVersion);
      useUserStore().saveMenuList(target.menus); // 菜单树即时切换(侧栏跟随)
      useUserStore().savePermissionList(target.permissions || []); // 权限同包预取,切换零请求
      return true;
    },
    // 切换按钮:换芯后落目标子应用首页(不清标签;SPA 内导航,不整页重载)
    async switchTo(subappId) {
      const target = this.list.find((s) => s.subappId === subappId);
      if (!target || !target.menus?.length) return;
      if (target.subappId === this.current.subappId) return;
      this.applyContext(subappId);
      // 重建路由表后 SPA 导航到目标首页——beforeEach 的标签驱动换芯会兜底
      const { default: router } = await import('@/router');
      const home = this.homePath;
      if (router.resolve(home).matched.length) {
        router.push(home);
      } else {
        location.href = location.origin + home; // 路由未注册(异常态)兜底整页
      }
    },
  },
});
