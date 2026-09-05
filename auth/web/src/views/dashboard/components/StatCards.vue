<script setup>
defineProps({ items: { type: Array, default: () => [] } });
</script>

<template>
  <t-row :gutter="[16, 16]">
    <t-col v-for="s in items" :key="s.key" :span="3">
      <t-card :bordered="false" class="stat-card">
        <div class="value">{{ s.value }}</div>
        <div class="label">{{ s.label }}</div>
        <div class="trend" :class="s.trend >= 0 ? 'up' : 'down'">
          {{ s.trend >= 0 ? '+' : '' }}{{ s.trend }}% 较昨日
        </div>
      </t-card>
    </t-col>
  </t-row>
</template>

<style lang="scss" scoped>
.stat-card {
  text-align: center;

  .value {
    font-size: 26px;
    font-weight: 600;
    color: var(--td-brand-color);
  }

  .label {
    margin-top: 4px;
    font-size: 13px;
    color: var(--td-text-color-secondary);
  }

  .trend {
    margin-top: 6px;
    font-size: 12px;

    &.up { color: var(--td-success-color); }
    &.down { color: var(--td-error-color); }
  }
}
</style>
