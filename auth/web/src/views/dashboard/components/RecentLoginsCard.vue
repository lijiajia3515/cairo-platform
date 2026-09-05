<script setup>
defineProps({ rows: { type: Array, default: () => [] } });

const columns = [
  { colKey: 'account', title: '账号' },
  { colKey: 'ip', title: 'IP' },
  { colKey: 'region', title: '地区' },
  { colKey: 'type', title: '方式' },
  { colKey: 'result', title: '结果' },
  { colKey: 'time', title: '时间' },
];
</script>

<template>
  <t-card title="最近登录" :bordered="false">
    <t-table table-layout="auto" row-key="account" :data="rows" :columns="columns" size="small" :pagination="false">
      <template #result="{ row }">
        <t-tag v-if="row.result === '成功'" theme="success" size="small">成功</t-tag>
        <t-tag v-else theme="danger" size="small">失败</t-tag>
      </template>
    </t-table>
  </t-card>
</template>
