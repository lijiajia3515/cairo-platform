<script setup>
// transfer
import {ref, onMounted, watch} from 'vue';
import {useRouter} from 'vue-router';

import useState from '@/hooks/useState';

onMounted(() => {

});

const props = defineProps({
  type: {
    type: String,
    default: 'account', // account user
  },
  list: {
    type: Array,
    default: () => [],
  },
  actives: {
    type: Array,
    default: () => [],
  },
  keys: {
    type: Object,
    default: () => ({
      label: 'nickname', value: 'accountId'
    })
  },
  modelValue: {
    type: Array,
    default: () => []
  },
  pagination: {
    type: Array,
    default: () => [
      {
        pageSize: 5,
        defaultCurrent: 1,
      },
      {
        pageSize: 5,
        defaultCurrent: 1,
      },
    ]
  }
});

const emits = defineEmits(["update:modelValue", "change"]);

let list = ref(props.list);

let actives = ref(props.actives); // 目标数据列表数据

watch(() => props.list, () => {
  list.value = props.list;
});

watch(actives, () => {
  emits("update:modelValue", actives.value);
}, {
  deep: true,
  immediate: true
})


</script>


<template>
  <div class="transfer__wrapper">
    <t-transfer v-model="actives" theme="primary" :data="list" :search="true" :keys="props.keys" :pagination="pagination" :operation="['移除', '加入']">
      <template #title="props">
        <div>{{ props.type === 'target' ? '已选择账号' : '可选择账号' }}</div>
      </template>
      <template #transferItem="{ data }">
        <div style="width: auto;overflow: hidden;display: flex;">
          <t-avatar style="margin-left:10px;margin-right:5px" :imageProps="{ lazy: true }" size="20px" :image="data.avatarUrl" shape="round"></t-avatar>
          <t-tooltip :content="data.nickname + ' (' + (data?.phoneNumber || data?.username || data?.accountId) + ')'">
            <span class="sl1" style="display: inline-block;width: 200px;">{{ data.nickname + ' (' + (data?.phoneNumber || data?.username || data?.accountId) + ')' }}</span>
          </t-tooltip>
        </div>
      </template>
    </t-transfer>
  </div>
</template>

<style lang="scss" scoped>
.transfer__wrapper {
}

::v-deep(.t-transfer__search .t-transfer__list) {
  width: 286px;
  height: 200px;
}
</style>
