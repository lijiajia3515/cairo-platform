<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useTagsViewStore } from '@/store/tagsView';
import { useSubappContextStore } from '@/store/subappContext';
import { useThemeConfigStore } from '@/store/themeConfig';

const route = useRoute();
const router = useRouter();

const tagsStore = useTagsViewStore();
const subappStore = useSubappContextStore();
const themeConfig = computed(() => useThemeConfigStore().config);

const tags = computed(() => tagsStore.tags);
const activePath = computed(() => route.fullPath);

// 路由路径 → 菜单图标(Chrome favicon 同款):跨子应用标签都覆盖,
// 深度遍历全部子应用菜单建映射;无图标的菜单回退圆点
const iconOf = (path) => {
  for (const s of subappStore.list) {
    const stack = [...(s.menus || [])];
    while (stack.length) {
      const n = stack.shift();
      if (n.component === path) return n.icon || '';
      if (n.menus?.length) stack.push(...n.menus);
    }
  }
  return '';
};

// 右键菜单
const menuVisible = ref(false);
const menuX = ref(0);
const menuY = ref(0);
const menuTarget = ref('');

const openMenu = (e, tag) => {
  menuTarget.value = tag.fullPath;
  menuX.value = e.clientX;
  menuY.value = e.clientY;
  menuVisible.value = true;
};

const closeMenu = () => {
  menuVisible.value = false;
};

const refresh = () => {
  tagsStore.refreshCurrent(menuTarget.value);
  closeMenu();
};

const closeCurrent = () => {
  const target = menuTarget.value;
  closeMenu();
  const next = tagsStore.closeTag(target);
  if (next && route.fullPath === target) {
    router.push(next);
  }
};

const closeOthers = () => {
  tagsStore.closeOthers(menuTarget.value);
  if (route.fullPath !== menuTarget.value) {
    router.push(menuTarget.value);
  }
  closeMenu();
};

// 新窗口打开(独立会话隔离上下文,适合跨子应用并行操作)
const openInNewWindow = () => {
  const target = menuTarget.value;
  closeMenu();
  window.open(location.origin + target, '_blank');
};

const closeAll = () => {
  closeMenu();
  const next = tagsStore.closeAll();
  router.push(next);
};

const closeTag = (fullPath) => {
  const next = tagsStore.closeTag(fullPath);
  if (next && route.fullPath === fullPath) {
    router.push(next);
  }
};

// 拖拽排序(设置「标签拖拽排序」开启):重排 tags 数组并持久化
let dragFrom = ref('');
const onDragStart = (e, fullPath) => {
  dragFrom.value = fullPath;
  e.dataTransfer.effectAllowed = 'move';
};
const onDragOver = (e, fullPath) => {
  if (dragFrom.value && dragFrom.value !== fullPath) e.dataTransfer.dropEffect = 'move';
};
const onDrop = (to) => {
  const from = dragFrom.value;
  dragFrom.value = '';
  if (!from || from === to) return;
  tagsStore.moveTag(from, to);
};

onMounted(() => {
  document.addEventListener('click', closeMenu, { capture: true });
});

onUnmounted(() => {
  document.removeEventListener('click', closeMenu, { capture: true });
});
</script>

<template>
  <div class="tags-view">
    <div class="tags-scroll">
      <div v-for="tag in tags" :key="tag.fullPath" class="tag-item" :class="{ active: tag.fullPath === activePath }"
        :draggable="themeConfig.isTagDrag" @dragstart="onDragStart($event, tag.fullPath)"
        @dragover.prevent="onDragOver($event, tag.fullPath)" @drop.prevent="onDrop(tag.fullPath)" @dragend="dragFrom = ''"
        :title="tag.title" @click="router.push(tag.fullPath)" @contextmenu.prevent="openMenu($event, tag)">
        <!-- 菜单图标(favicon 位):设置可关;有则显示,无则回退圆点 -->
        <img v-if="themeConfig.isTagIcon && iconOf(tag.path)" class="tag-icon" :src="iconOf(tag.path)" alt="">
        <span v-else-if="themeConfig.isTagIcon" class="tag-dot"></span>
        <span class="tag-title">{{ tag.title }}</span>
        <!-- 图标按钮必须原生元素承载 @click.stop:t-icon 是组件,自定义事件无 $event,.stop 会抛 stopPropagation is not a function -->
        <span v-if="tag.fullPath === activePath" class="tag-refresh" title="刷新" @click.stop="tagsStore.refreshCurrent(tag.fullPath)">
          <t-icon name="refresh" size="12" />
        </span>
        <span v-if="tags.length > 1" class="tag-close" @click.stop="closeTag(tag.fullPath)">
          <t-icon name="close" size="12" />
        </span>
      </div>
    </div>

    <teleport to="body">
      <div v-if="menuVisible" class="tags-context-menu" :style="{ left: menuX + 'px', top: menuY + 'px' }">
        <div class="menu-item" @click="openInNewWindow">新窗口打开</div>
        <div class="menu-item" @click="refresh">刷新当前</div>
        <div class="menu-item" @click="closeCurrent">关闭当前</div>
        <div class="menu-item" @click="closeOthers">关闭其他</div>
        <div class="menu-item" @click="closeAll">关闭全部</div>
      </div>
    </teleport>
  </div>
</template>

<style lang="scss" scoped>
// Chrome 式融合标签条:整条灰底托底(浏览器 tab strip 角色,非托盘框),
// 活动标签满高贴底、上圆角、底色=内容区底色——与下方页面连成同一块白底,
// 无分隔线;未选标签沉在灰条上,hover 提亮。全部走 TDesign 令牌,暗黑自动跟随
.tags-view {
  flex: none;
  width: 100%;
  height: 38px;
  display: flex;
  // 浅一档的灰(component=#e8e8e8 比卡外 page 底还深,白卡内会显脏突兀)
  background: var(--td-bg-color-secondarycontainer);
  box-sizing: border-box;

  .tags-scroll {
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: stretch; // 标签满高,活动标签底边才能与内容区无缝贴合
    padding: 5px 8px 0; // 顶部留灰边可见,底部零留白=融合关键
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;

    &::-webkit-scrollbar {
      height: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--td-scrollbar-color);
      border-radius: 3px;
    }
  }

  // 未选=灰条上的透明标签,次级文字色;active=白底主文字,即「页面本身」
  .tag-item {
    display: inline-flex;
    align-items: center;
    padding: 0 12px;
    margin-right: 2px;
    border-radius: var(--td-radius-large) var(--td-radius-large) 0 0;
    background: transparent;
    font-size: 12px;
    color: var(--td-text-color-secondary);
    cursor: pointer;
    user-select: none;
    flex: none;
    transition: none; // 静态排版,避免切页跳动

    &:hover {
      color: var(--td-text-color-primary);
      background: var(--td-bg-color-container-hover);
    }

    &.active {
      background: var(--td-bg-color-container); // 与内容同底=融合
      color: var(--td-text-color-primary);

      &:hover {
        background: var(--td-bg-color-container);
      }

      .tag-dot {
        background: var(--td-brand-color);
        opacity: 1;
      }
    }

    .tag-icon {
      width: 14px;
      height: 14px;
      margin-right: 6px;
      flex: none;
      object-fit: contain;
    }

    .tag-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--td-text-color-placeholder);
      opacity: 0.8;
      margin-right: 6px;
      flex: none;
    }

    .tag-title {
      max-width: 140px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .tag-refresh,
    .tag-close {
      margin-left: 6px;
      border-radius: 50%;
      width: 16px;
      height: 16px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: var(--td-text-color-secondary);

      &:hover {
        background: var(--td-bg-color-component);
      }
    }

    .tag-refresh:hover {
      color: var(--td-brand-color);
    }

    .tag-close:hover {
      color: var(--td-error-color);
    }
  }
}

.tags-context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 120px;
  padding: 4px 0;
  background: var(--td-bg-color-container);
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-medium);
  box-shadow: var(--td-shadow-2);

  .menu-item {
    padding: 6px 14px;
    font-size: 13px;
    color: var(--td-text-color-primary);
    cursor: pointer;

    &:hover {
      background: var(--td-bg-color-container-hover);
      color: var(--td-brand-color);
    }
  }
}
</style>
