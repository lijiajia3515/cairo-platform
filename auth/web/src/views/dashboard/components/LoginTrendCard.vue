<script setup>
defineProps({ data: { type: Object, required: true } });
</script>

<template>
  <t-card title="近 7 日登录趋势" :bordered="false">
    <!-- 占位:纯 CSS 柱状示意,接图表库后替换 -->
    <div class="bars">
      <div v-for="(d, i) in data.days" :key="d" class="bar-col">
        <div class="bar success" :style="{ height: (data.success[i] / 2.6) + 'px' }" :title="`成功 ${data.success[i]}`"></div>
        <div class="bar failed" :style="{ height: (data.failed[i] / 2.6) + 'px' }" :title="`失败 ${data.failed[i]}`"></div>
        <div class="day">{{ d }}</div>
      </div>
    </div>
    <div class="legend">
      <span class="dot success"></span>成功
      <span class="dot failed"></span>失败
    </div>
  </t-card>
</template>

<style lang="scss" scoped>
.bars {
  display: flex;
  align-items: flex-end;
  gap: 18px;
  height: 120px;
  padding: 8px 4px 0;

  .bar-col {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    align-items: center;
    gap: 2px;

    .bar {
      width: 14px;
      border-radius: 2px 2px 0 0;

      &.success { background: var(--td-brand-color); }
      &.failed { background: var(--td-error-color); opacity: 0.55; }
    }

    .day {
      margin-top: 6px;
      font-size: 11px;
      color: var(--td-text-color-placeholder);
    }
  }
}

.legend {
  margin-top: 8px;
  font-size: 12px;
  color: var(--td-text-color-secondary);

  .dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 2px;
    margin: 0 4px 0 12px;

    &.success { background: var(--td-brand-color); }
    &.failed { background: var(--td-error-color); }
  }
}
</style>
