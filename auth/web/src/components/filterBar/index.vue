<script setup>
// 列表页筛选区公共布局:控件流式换行(替代 t-row/t-col 栅格与 width>750 双分支),
// 查询/重置固定在动作区,回车触发查询;额外动作按钮(如 创建)放 #actions 插槽
const emit = defineEmits(['search', 'reset']);
</script>

<template>
  <header class="filter-bar">
    <div class="filter-bar__items" @keyup.enter="emit('search')">
      <slot />
    </div>
    <div class="filter-bar__actions">
      <t-button theme="primary" @click="emit('search')">查询</t-button>
      <t-button variant="outline" theme="primary" @click="emit('reset')">重置</t-button>
      <slot name="actions" />
    </div>
  </header>
</template>

<style lang="scss" scoped>
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px 16px;
  padding-bottom: 12px;

  &__items {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 16px;
    flex: 1 1 auto;
    min-width: 0;
  }

  &__actions {
    display: flex;
    gap: 8px;
    flex: none;
    margin-left: auto;
  }
}
</style>
