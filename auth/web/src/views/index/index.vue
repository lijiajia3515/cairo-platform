<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

import { merge } from 'lodash';
import zhConfig from 'tdesign-vue-next/es/locale/zh_CN';

import Menu from '@/components/menu';
import Header from '@/components/header';
import TagsView from '@/components/tagsView';

import useCollapsed from '@/hooks/useCollapsed';
import { useThemeConfigStore } from '@/store/themeConfig';
import { useTagsViewStore } from '@/store/tagsView';
import { useUserStore } from '@/store/user';

let translateConfig = ref(zhConfig);

const globalConfig = merge(translateConfig, {
  // 可以在此处定义更多自定义配置，具体可配置内容参看 API 文档
  calendar: {},
  table: {},
  pagination: {},
});

const router = useRouter();
const themeStore = useThemeConfigStore();
const tagsStore = useTagsViewStore();
const userStore = useUserStore();

const collapsed = useCollapsed();
// 侧栏宽度:折叠 64,展开为可拖拽宽度(默认 200,拖拽范围 180-320,松手持久化)
const dragWidth = ref(themeStore.config.asideWidth || 200);
const asideWidth = computed(() => (collapsed.value ? '64px' : dragWidth.value + 'px'));

// logo 点击=折叠/展开菜单
const toggleCollapse = () => {
  themeStore.update({ isCollapse: !collapsed.value });
};

const onResizeStart = (e) => {
  e.preventDefault();
  const onMove = (ev) => {
    dragWidth.value = Math.min(320, Math.max(180, ev.clientX));
  };
  const onUp = () => {
    document.removeEventListener('mousemove', onMove);
    document.removeEventListener('mouseup', onUp);
    themeStore.update({ asideWidth: dragWidth.value });
  };
  document.addEventListener('mousemove', onMove);
  document.addEventListener('mouseup', onUp);
};


const cachedViews = computed(() => tagsStore.cachedViews);
const showTagsView = computed(() => themeStore.config.isTagsView);
const watermarkVisible = computed(() => themeStore.config.isWatermark);
const watermarkText = computed(() => userStore.userGetter?.nickname || userStore.userGetter?.accountNickname || 'Cairo');

// 水印背景(SVG 平铺,账号昵称)
const watermarkStyle = computed(() => ({
  backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='160'%3E%3Ctext x='20' y='90' font-size='14' fill='rgba(0,0,0,0.06)' transform='rotate(-20 20 90)'%3E${encodeURIComponent(watermarkText.value)}%3C/text%3E%3C/svg%3E")`,
}));

onMounted(() => {
  // 进入壳时应用持久化的个性化配置(暗黑/品牌色)
  themeStore.apply();
});

onUnmounted(() => {

});

</script>


<template>
  <t-config-provider :global-config="globalConfig">
    <!-- 壳布局:logo 夹左上角(横向属页头行/竖向属菜单列顶格),
         右列 header 为功能区(折叠按钮+切换+面包屑+用户) -->
    <t-layout class="shell">
      <t-aside :width="asideWidth" class="aside-content">
        <div class="aside-logo" :class="{ collapsed }" title="折叠/展开菜单" @click="toggleCollapse">
          <img src="/logo.svg" alt="CAIRO" />
          <span v-show="!collapsed" class="aside-title">CAIRO</span>
        </div>
        <Menu></Menu>
        <!-- 侧栏拖拽调宽(折叠态隐藏) -->
        <div v-show="!collapsed" class="aside-resizer" @mousedown="onResizeStart"></div>
      </t-aside>
      <t-layout class="right-layout">
        <t-header>
          <Header></Header>
        </t-header>
        <!-- 主内容容器:圆角白底托起标签导航与页面(灰底留作外衬,避免整片灰不像页面) -->
        <div class="main-container">
          <TagsView v-if="showTagsView && tagsStore.tags.length"></TagsView>
          <t-content class="lay-content">
            <!-- Component 是 vue-router scoped slot 的 API 契约（大写 C），不属于业务字段，禁止 camelCase 翻转 -->
            <router-view v-slot="{ Component, route }">
              <keep-alive :include="cachedViews" :max="30">
                <component :is="Component" :key="tagsStore.viewKey(route)"></component>
              </keep-alive>
            </router-view>
          </t-content>
        </div>
      </t-layout>
    </t-layout>

    <!-- 页面水印(个性化开关) -->
    <div v-if="watermarkVisible" class="watermark-layer" :style="watermarkStyle"></div>
  </t-config-provider>
</template>

<style lang="scss" scoped>
.shell {
  width: 100%;
  height: 100%;
}

.aside-content {
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--td-component-stroke);
  background: var(--td-bg-color-container);
  transition: width 0.2s;
  position: relative;

  // logo 夹角区:高度与 t-header 同令牌,点击折叠菜单
  .aside-logo {
    flex: none;
    height: var(--td-comp-size-xxxl);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    border-bottom: 1px solid var(--td-component-stroke);
    box-sizing: border-box;
    overflow: hidden;
    cursor: pointer;
    user-select: none;

    img {
      width: 26px;
      height: 26px;
      flex: none;
    }

    .aside-title {
      font-family: Inter, var(--td-font-family);
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 0.08em;
      color: var(--td-text-color-primary);
      white-space: nowrap;
    }
  }

  .aside-resizer {
    position: absolute;
    top: 0;
    right: -3px;
    width: 6px;
    height: 100%;
    cursor: col-resize;
    z-index: 10;

    &:hover {
      background: var(--td-brand-color);
      opacity: 0.35;
    }
  }

}

.right-layout {
  flex: 1;
  min-width: 0;
}

// 主内容容器:页头下方留灰底外衬,圆角白底承载标签行+路由页
.main-container {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin: 8px 10px 10px;
  background: var(--td-bg-color-container);
  border-radius: var(--td-radius-large);
  overflow: hidden;
}

.lay-content {
  flex: 1;
  min-height: 0;
  box-sizing: border-box;
  padding: 12px;
  overflow: auto;
}

.watermark-layer {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  background-repeat: repeat;
}
</style>
