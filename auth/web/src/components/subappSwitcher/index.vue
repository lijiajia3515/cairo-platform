<script setup>
import { computed, ref } from 'vue';

import { useSubappContextStore } from '@/store/subappContext';

const subappStore = useSubappContextStore();

// 直显按钮上限,超出收进「更多」浮层(阿里云控制台顶部导航同款)
const MAX_DIRECT = 4;

const currentId = computed(() => subappStore.current.subappId);
const visible = computed(() => subappStore.visible);
const directItems = computed(() => visible.value.slice(0, MAX_DIRECT));
const moreItems = computed(() => visible.value.slice(MAX_DIRECT));
const moreVisible = ref(false);

const onSwitch = (subappId) => {
  moreVisible.value = false;
  subappStore.switchTo(subappId);
};
</script>

<template>
  <div v-if="visible.length > 1" class="subapp-switcher">
    <div v-for="item in directItems" :key="item.subappId" class="subapp-btn"
      :class="{ active: item.subappId === currentId }" :title="item.subappName"
      @click="onSwitch(item.subappId)">
      {{ item.subappName }}
    </div>

    <t-popup v-if="moreItems.length" trigger="click" placement="bottom-start" :visible="moreVisible"
      @visible-change="(v) => (moreVisible = v)">
      <div class="subapp-btn more-btn" :class="{ active: moreItems.some((i) => i.subappId === currentId) }">
        更多
        <t-icon name="chevron-down" size="12" />
      </div>
      <template #content>
        <div class="more-panel">
          <div v-for="item in moreItems" :key="item.subappId" class="more-item"
            :class="{ active: item.subappId === currentId }" @click="onSwitch(item.subappId)">
            {{ item.subappName }}
          </div>
        </div>
      </template>
    </t-popup>
  </div>
</template>

<style lang="scss" scoped>
// 左侧细分隔与面包屑同语言(界定「侧栏控件|导航内容」),与折叠钮的间距交给父级 gap
.subapp-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  padding-left: 12px;
  border-left: 1px solid var(--td-component-stroke);

  .subapp-btn {
    padding: 3px 12px;
    border-radius: var(--td-radius-round);
    border: 1px solid var(--td-component-stroke);
    font-size: 12px;
    color: var(--td-text-color-primary);
    cursor: pointer;
    user-select: none;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 120px;
    display: inline-flex;
    align-items: center;
    gap: 2px;

    &:hover {
      color: var(--td-brand-color);
      border-color: var(--td-brand-color);
    }

    &.active {
      background: var(--td-brand-color);
      border-color: var(--td-brand-color);
      color: #fff;
    }
  }

  .more-btn {
    margin-left: auto;
  }
}

.more-panel {
  padding: 4px;

  .more-item {
    padding: 6px 14px;
    border-radius: var(--td-radius-default);
    font-size: 12px;
    color: var(--td-text-color-primary);
    cursor: pointer;
    white-space: nowrap;

    &:hover {
      background: var(--td-bg-color-container-hover);
      color: var(--td-brand-color);
    }

    &.active {
      color: var(--td-brand-color);
      font-weight: 600;
    }
  }
}
</style>
