// 个性化配置(TDesign 可落地子集)
import { defineStore } from 'pinia';

const STORAGE_KEY = 'cairo_theme_config';

// 预设品牌色板(TDesign 默认蓝 + 常用备选)
export const BRAND_COLORS = ['', '#0052D9', '#00A870', '#ED7B2F', '#D54941', '#8E4EC6'];

const defaults = () => ({
  isDark: false, // 暗黑模式(theme-mode)
  isCollapse: false, // 菜单折叠
  isAccordion: true, // 菜单手风琴(同屏仅展开一组)
  isTagsView: true, // 多标签栏
  isBreadcrumb: true, // 面包屑导航
  isBreadcrumbIcon: false, // 面包屑图标(pig 同款)
  isTagIcon: true, // 标签栏图标(favicon 位)
  isTagDrag: false, // 标签拖拽排序
  isWatermark: false, // 页面水印
  brandColor: '', // 品牌主色(空=TDesign 默认)
  asideWidth: 200, // 侧栏宽度(px,可拖拽 180-320)
});

const readStored = () => {
  try {
    return { ...defaults(), ...JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') };
  } catch (e) {
    return defaults();
  }
};

export const useThemeConfigStore = defineStore('themeConfig', {
  state: () => ({ themeConfig: readStored() }),
  getters: {
    config: (state) => state.themeConfig,
  },
  actions: {
    // 写配置并即时生效
    update(partial) {
      this.themeConfig = { ...this.themeConfig, ...partial };
      this.persist();
      this.apply();
    },
    persist() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.themeConfig));
    },
    reset() {
      this.themeConfig = defaults();
      this.persist();
      this.apply();
    },
    // 应用到 DOM:暗黑走 theme-mode 属性,品牌色走 TDesign CSS 变量(派生色用 color-mix 计算)
    apply() {
      const c = this.themeConfig;
      const root = document.documentElement;
      if (c.isDark) {
        root.setAttribute('theme-mode', 'dark');
      } else {
        root.removeAttribute('theme-mode');
      }
      const vars = {
        '--td-brand-color': c.brandColor,
        '--td-brand-color-hover': c.brandColor ? 'color-mix(in srgb, var(--td-brand-color), #fff 12%)' : '',
        '--td-brand-color-active': c.brandColor ? 'color-mix(in srgb, var(--td-brand-color), #000 12%)' : '',
        '--td-brand-color-focus': c.brandColor,
        '--td-brand-color-light': c.brandColor ? 'color-mix(in srgb, var(--td-brand-color), #fff 90%)' : '',
      };
      for (const [k, v] of Object.entries(vars)) {
        if (v) {
          root.style.setProperty(k, v);
        } else {
          root.style.removeProperty(k);
        }
      }
    },
  },
});
