<script setup>
// 通知消息记录:占位空页,通知通道接入后实现记录列表
defineOptions({ name: 'manage-notify-record' })
</script>

<template>
  <div class="page-placeholder">
    <t-card :bordered="false">
      <div class="placeholder__title">通知消息记录</div>
      <div class="placeholder__desc">页面建设中,敬请期待</div>
    </t-card>
  </div>
</template>

<style lang="scss" scoped>
.page-placeholder {
  :deep(.t-card) {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
  }

  .placeholder__title {
    font-size: 20px;
    font-weight: 600;
    color: var(--td-text-color-primary);
    text-align: center;
  }

  .placeholder__desc {
    margin-top: 8px;
    font-size: 13px;
    color: var(--td-text-color-placeholder);
    text-align: center;
  }
}
</style>
