<!-- 账号信息 -->
<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, } from 'vue';
import { useRouter } from 'vue-router';

import useState from '@/hooks/useState';

onMounted(() => {

});

const emit = defineEmits(['close'])

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  }
});

// 本地展示副本：避免与 prop 同名（no-dupe-keys），打开时随 prop 同步、关闭时清空
let accountData = ref({});

watch(() => props.data, () => {
  accountData.value = props.data;
})


const visible = ref(false);

const open = () => {
  visible.value = true;
}

const close = () => {
  emit('close')
  visible.value = false;
  accountData.value = {};
}

defineExpose({
  open
})


onUnmounted(() => {

})
</script>


<template>
  <t-dialog attach="body" :cancelBtn="null" :confirmBtn="null" @close="close" :visible="visible">
    <template #header>账号详情</template>
    <div class="empty"></div>
    <t-row>
      <t-col :span="11">
        <t-image fit="cover" style="width:100px;height:100px" :src="accountData?.avatarUrl" />
      </t-col>
      <div class="empty"></div>
      <t-col :span="11">账号ID：{{ accountData?.accountId }}</t-col>
      <div class="empty"></div>
      <t-col :span="11">昵称：{{ accountData?.nickname }}</t-col>
      <div class="empty"></div>
      <t-col :span="11">加入时间：{{ accountData?.joinTime }}</t-col>
      <div class="empty"></div>
    </t-row>
  </t-dialog>
</template>

<style lang="scss" scoped>
.userInfo__wrapper {}
</style>
