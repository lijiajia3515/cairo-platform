// 多窗口标签(tagsView):标签增删 + keep-alive 缓存名单 + 刷新恢复
import { defineStore } from 'pinia';

const STORAGE_KEY = 'cairo_tags_view';
const ACTIVE_KEY = 'cairo_tags_view_active';

// 路由路径 → 视图组件名(各视图 defineOptions({ name }) 与此约定一致,keep-alive include 按名匹配)
export const pathToName = (path) => path.replace(/^\//, '').replace(/\//g, '-');

const serialize = (tags) => tags.map((t) => ({ fullPath: t.fullPath, path: t.path, title: t.title, query: t.query }));

const readStored = () => {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '[]');
  } catch (e) {
    return [];
  }
};

export const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    tags: readStored(),
    // 当前活动标签的 fullPath:刷新/重进时由 '/' 重定向恢复,取代旧的 lastPath 机制
    activeFullPath: sessionStorage.getItem(ACTIVE_KEY) || '',
    // 各路径的刷新次数:拼进组件 key 触发确定性重挂(只影响当前标签,不动其他标签缓存)
    refreshTicks: {},
  }),
  getters: {
    // keep-alive include 名单:/iframe 不缓存(每次进入按 query 重载子应用)
    cachedViews: (state) =>
      state.tags
        .filter((t) => t.path !== '/iframe')
        .map((t) => pathToName(t.path)),
    // 恢复落点:活动标签优先,其次首个标签,兜底 /home
    activePath: (state) => state.activeFullPath || state.tags[0]?.fullPath || '/home',
  },
  actions: {
    addTag(route) {
      if (!route.name) return; // 未命名路由(404 兜底等)不开标签
      this.activeFullPath = route.fullPath;
      this.persistActive();
      if (this.tags.some((t) => t.fullPath === route.fullPath)) return;
      this.tags.push({
        fullPath: route.fullPath,
        path: route.path,
        title: route.query.title || route.meta?.title || route.name, // meta.title=菜单名(重名路由 name 是 path 兜底)
        query: route.query,
      });
      this.persist();
    },
    // 关闭并按需跳转;返回目标 fullPath(无剩余标签时回 /home)
    closeTag(fullPath) {
      const index = this.tags.findIndex((t) => t.fullPath === fullPath);
      if (index === -1) return '';
      this.tags.splice(index, 1);
      this.persist();
      if (this.tags.length === 0) return '/home';
      return this.tags[Math.min(index, this.tags.length - 1)].fullPath;
    },
    closeOthers(fullPath) {
      this.tags = this.tags.filter((t) => t.fullPath === fullPath);
      this.persist();
    },
    closeAll() {
      this.tags = [];
      this.persist();
      return '/home';
    },
    // 拖拽排序:把 from 移到 to 的位置(设置「标签拖拽排序」)
    moveTag(from, to) {
      const fromIndex = this.tags.findIndex((t) => t.fullPath === from);
      const toIndex = this.tags.findIndex((t) => t.fullPath === to);
      if (fromIndex === -1 || toIndex === -1 || fromIndex === toIndex) return;
      const [moved] = this.tags.splice(fromIndex, 1);
      this.tags.splice(toIndex, 0, moved);
      this.persist();
    },
    // 刷新:对应路径 key 追加刷新序号,组件以新 key 重挂载得到全新实例;
    // 旧实例成为缓存孤儿,由 keep-alive :max LRU 兜底回收
    refreshCurrent(fullPath) {
      const tag = this.tags.find((t) => t.fullPath === fullPath);
      if (!tag) return;
      this.refreshTicks = { ...this.refreshTicks, [tag.path]: (this.refreshTicks[tag.path] || 0) + 1 };
    },
    // 视图组件 key:fullPath + 刷新序号
    viewKey(route) {
      const tick = this.refreshTicks[route.path];
      return tick ? `${route.fullPath}-r${tick}` : route.fullPath;
    },
    resetTags() {
      this.tags = [];
      this.activeFullPath = '';
      this.refreshTicks = {};
      sessionStorage.removeItem(STORAGE_KEY);
      sessionStorage.removeItem(ACTIVE_KEY);
    },
    persist() {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(serialize(this.tags)));
    },
    persistActive() {
      sessionStorage.setItem(ACTIVE_KEY, this.activeFullPath);
    },
  },
});
