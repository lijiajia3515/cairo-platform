<script setup>
// 列表页公共表格壳:49 页唯一表格入口
// 统一默认:fixed 布局 + 未设宽列补 minWidth/ellipsis(长数据不再撑爆表格)、size=medium(与筛选区字号同级)、
// 分页默认展示每页条数/跳页;页面显式设置始终优先
import { computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  configs: {
    type: Object,
    default: () => ({
      data: [],
      columns: [],
      hover: false,
      stripe: false,
      bordered: false,
      resizable: false,
      showHeader: true,
      tableLayout: 'fixed',
      rowKey: 'id',
      size: 'medium',
      pagination: {
        defaultCurrent: 1,
        defaultPageSize: 10,
        total: 0,
      },
    }),
  },
});

const emit = defineEmits(['page-change', 'onDragSort']);

onMounted(() => {

});

const getValues = (key) => {
  return props.configs?.[key];
}

// 未显式设宽的列按语义猜一个 minWidth,配合 fixed 布局约束列宽
const guessMinWidth = (col) => {
  const key = String(col.colKey || '');
  const title = String(col.title || '');
  if (key === 'operation') return 160;
  if (/time$/i.test(key) || title.includes('时间')) return 170;
  if (/Id$/.test(key) || /ID$/.test(title)) return 190;
  return 120;
};

// 列后处理:无自定义 cell 的列统一补 minWidth + ellipsis;有 cell/width 的不动
const mergedColumns = computed(() => {
  const columns = getValues('columns') || [];
  return columns.map((col) => {
    if (col.cell) return col;
    const next = { ...col, ellipsis: col.ellipsis ?? true };
    if (!next.width && !next.minWidth) {
      next.minWidth = guessMinWidth(col);
    }
    return next;
  });
});

// 分页默认:可切每页条数 + 可跳页;页面配置覆盖默认
const mergedPagination = computed(() => ({
  pageSizeOptions: [10, 20, 50, 100],
  showJumper: true,
  showPageSize: true,
  ...(getValues('pagination') || {}),
}));

// 布局默认 fixed(长串不再拉伸整表);仅显式传 'auto' 才走自动布局
const tableLayout = computed(() => (getValues('tableLayout') === 'auto' ? 'auto' : 'fixed'));

const size = computed(() => getValues('size') ?? 'medium');

const onPageChange = (pageInfo) => {
  emit('page-change', pageInfo)
}
const onDragSort = (params) => {
  emit('onDragSort', params);
}

onUnmounted(() => {

})
</script>


<template>
  <div class="list__wrapper">
    <t-table :loading="getValues('loading')" :row-key="getValues('rowKey')" :data="getValues('data')"
      :columns="mergedColumns" :stripe="getValues('stripe')" :bordered="getValues('bordered')"
      :resizable="getValues('resizable')" :table-layout="tableLayout"
      :table-content-width="getValues('tableContentWidth')" :size="size" :pagination="mergedPagination" :show-header="getValues('showHeader')"
      :cell-empty-content="getValues('cellEmptyContent')" :drag-sort="getValues('dragSort')" @drag-sort="onDragSort" @page-change="onPageChange">
    </t-table>
  </div>
</template>
<style lang="scss" scoped>
.list__wrapper {}
</style>
