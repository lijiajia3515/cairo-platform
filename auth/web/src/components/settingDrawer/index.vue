<script setup>
import { computed } from 'vue';
import { CloseIcon } from 'tdesign-icons-vue-next';

import { useThemeConfigStore, BRAND_COLORS } from '@/store/themeConfig';

defineProps({ visible: Boolean });
defineEmits(['update:visible']);

const themeStore = useThemeConfigStore();
const config = computed(() => themeStore.config);

const colorLabels = ['', '默认蓝', '青绿', '活力橙', '绯红', '紫罗兰'];

const onReset = () => {
  themeStore.reset();
};
</script>

<template>
  <t-drawer :visible="visible" header="个性化设置" size="280px" :footer="false" @close="$emit('update:visible', false)">
    <div class="setting-group">
      <div class="setting-title">主题模式</div>
      <div class="setting-row">
        <span>暗黑模式</span>
        <t-switch :value="config.isDark" @change="(v) => themeStore.update({ isDark: v })" />
      </div>
      <div class="setting-row">
        <span>品牌主色</span>
        <div class="color-list">
          <div v-for="(color, i) in BRAND_COLORS" :key="i" class="color-dot"
            :class="{ active: config.brandColor === color, 'is-default': !color }" :style="color ? { background: color } : {}"
            :title="colorLabels[i]" @click="themeStore.update({ brandColor: color })">
            <CloseIcon v-if="!color" size="12" />
          </div>
        </div>
      </div>
    </div>

    <div class="setting-group">
      <div class="setting-title">界面</div>
      <div class="setting-row">
        <span>菜单折叠</span>
        <t-switch :value="config.isCollapse" @change="(v) => themeStore.update({ isCollapse: v })" />
      </div>
      <div class="setting-row">
        <span>菜单手风琴</span>
        <t-switch :value="config.isAccordion" @change="(v) => themeStore.update({ isAccordion: v })" />
      </div>
      <div class="setting-row">
        <span>面包屑导航</span>
        <t-switch :value="config.isBreadcrumb" @change="(v) => themeStore.update({ isBreadcrumb: v })" />
      </div>
      <div class="setting-row">
        <span>多标签栏</span>
        <t-switch :value="config.isTagsView" @change="(v) => themeStore.update({ isTagsView: v })" />
      </div>
      <div class="setting-row">
        <span>面包屑图标</span>
        <t-switch :value="config.isBreadcrumbIcon" @change="(v) => themeStore.update({ isBreadcrumbIcon: v })" />
      </div>
      <div class="setting-row">
        <span>标签栏图标</span>
        <t-switch :value="config.isTagIcon" @change="(v) => themeStore.update({ isTagIcon: v })" />
      </div>
      <div class="setting-row">
        <span>标签拖拽排序</span>
        <t-switch :value="config.isTagDrag" @change="(v) => themeStore.update({ isTagDrag: v })" />
      </div>
      <div class="setting-row">
        <span>页面水印</span>
        <t-switch :value="config.isWatermark" @change="(v) => themeStore.update({ isWatermark: v })" />
      </div>
    </div>

    <div class="setting-group">
      <t-button block variant="outline" theme="default" @click="onReset">恢复默认</t-button>
    </div>
  </t-drawer>
</template>

<style lang="scss" scoped>
.setting-group {
  margin-bottom: 24px;

  .setting-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--td-text-color-primary);
    margin-bottom: 8px;
  }

  .setting-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    font-size: 13px;
    color: var(--td-text-color-primary);
  }

  .color-list {
    display: flex;
    gap: 6px;

    .color-dot {
      width: 20px;
      height: 20px;
      border-radius: 50%;
      border: 1px solid var(--td-component-stroke);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--td-text-color-placeholder);
      box-sizing: border-box;

      &.is-default {
        background: var(--td-bg-color-component);
      }

      &.active {
        outline: 2px solid var(--td-brand-color);
        outline-offset: 1px;
      }
    }
  }
}
</style>
