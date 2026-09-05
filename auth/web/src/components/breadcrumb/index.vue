<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

import { useUserStore } from '@/store/user';
import { useThemeConfigStore } from '@/store/themeConfig';

const route = useRoute();
const userStore = useUserStore();
const themeStore = useThemeConfigStore();
const themeConfig = computed(() => themeStore.config);

// 从菜单树推导当前路由的层级链(分组/子分组/叶子);未命中(隐藏路由/iframe)回退当前路由名
const findChain = (list, target, ancestors) => {
  for (const item of list) {
    const chain = [...ancestors, item];
    if (item.component === target) return chain;
    if (item.menus?.length) {
      const found = findChain(item.menus, target, chain);
      if (found) return found;
    }
  }
  return null;
};

// 分组的首个可见叶子路由:面包屑点父级导航到第一个子菜单;全隐藏时放宽
const firstLeaf = (node) => {
  if (!node.menus?.length) return node.component || '';
  const visible = node.menus.filter((m) => !m.hiddenMenu);
  for (const child of (visible.length ? visible : node.menus)) {
    const leaf = firstLeaf(child);
    if (leaf) return leaf;
  }
  return '';
};

const items = computed(() => {
  const home = { menuName: '首页', to: '/home' };
  const chain = findChain(userStore.menuListGetter, route.path, []);
  if (!chain) {
    // 菜单树外的页面(个人信息/iframe 等):只显示 首页 + 页面名(meta.title=菜单名,
    // 重名路由 name 是 path 兜底不可作显示名)
    const name = route.query.title || route.meta?.title || route.name;
    return name && route.path !== '/home' ? [home, { menuName: String(name), to: '' }] : [home];
  }
  // 子应用归属不再进面包屑:页头左侧子应用切换条已承担该标识,重复且无图标;
  // 面包屑只表达菜单树层级:首页 > 分组 > 页面
  return [
    home,
    ...chain.map((node, i) => ({
      menuName: node.menuName,
      icon: node.icon || '',
      to: i < chain.length - 1 ? firstLeaf(node) : '',
    })),
  ];
});
</script>

<template>
  <t-breadcrumb class="breadcrumb">
    <t-breadcrumb-item v-for="(item, i) in items" :key="i" :to="item.to || undefined">
      <span class="crumb">
        <img v-if="themeConfig.isBreadcrumbIcon && item.icon" class="crumb-icon" :src="item.icon" alt="">
        {{ item.menuName }}
      </span>
    </t-breadcrumb-item>
  </t-breadcrumb>
</template>

<style lang="scss" scoped>
.crumb {
  display: inline-flex;
  align-items: center;
  gap: 4px;

  .crumb-icon {
    width: 14px;
    height: 14px;
    object-fit: contain;
  }
}

.breadcrumb {
  // 收敛到与 header 同一文本层级;色板交给 TDesign 变量(暗黑自动跟随)
  // 静态排版:字号收敛、色阶弱化层级、禁过渡动画——header 内嵌场景避免
  // 切页时字宽变化引起的视觉跳动
  :deep(.t-breadcrumb__item) {
    .t-breadcrumb__inner {
      font-size: 13px;
      transition: none;
    }

    .t-breadcrumb--text-overflow {
      max-width: 140px;
    }

    .t-breadcrumb__separator {
      color: var(--td-text-color-placeholder);
      margin: 0 4px;
    }

    &:last-child .t-breadcrumb__inner {
      color: var(--td-text-color-primary);
      font-weight: 500;
    }
  }
}
</style>
