<script setup>
// 筛选项:统一标签宽度与控件宽度,终结 labelWidth 80/90/100 混排导致的输入框错位
defineProps({
  label: { type: String, default: '' },
  // 宽控件(日期区间等)占 330px,默认 200px
  wide: { type: Boolean, default: false },
});
</script>

<template>
  <div class="filter-item" :class="{ 'filter-item--wide': wide }">
    <span v-if="label" class="filter-item__label">{{ label }}</span>
    <div class="filter-item__control">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 200px;
  flex: none;

  &--wide {
    width: 330px;
  }

  &__label {
    flex: none;
    max-width: 6em;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    text-align: right;
    color: var(--td-text-color-secondary);
    font-size: 14px;
  }

  &__control {
    flex: 1 1 auto;
    min-width: 0;

    // 控件统一撑满剩余宽度
    :deep(.t-input),
    :deep(.t-select),
    :deep(.t-cascader),
    :deep(.t-tree-select),
    :deep(.t-date-range-picker),
    :deep(.t-date-picker) {
      width: 100%;
    }
  }
}
</style>
