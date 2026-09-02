<!-- 用户信息 -->
<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, } from 'vue';
import { useRouter } from 'vue-router';

import useState from '@/hooks/useState';

onMounted(() => {

});

const emit = defineEmits(['close'])

const props = defineProps({
  userId: {
    type: String,
  },
  nickname: {
    type: String,
  },
  accountAvatarUrl: {
    type: String,
  },
  joinTime: {
    type: String,
  },
});

watch(() => props.userId, () => {
  userId.value = props.userId;
})
watch(() => props.nickname, () => {
  nickname.value = props.nickname;
})
watch(() => props.accountAvatarUrl, () => {
  accountAvatarUrl.value = props.accountAvatarUrl;
})
watch(() => props.joinTime, () => {
  joinTime.value = props.joinTime;
})

const userId = ref(props.userId);
const nickname = ref(props.nickname);
const accountAvatarUrl = ref(props.accountAvatarUrl);
const joinTime = ref(props.joinTime);


const visible = ref(false);

const open = () => {
  visible.value = true;
}

const close = () => {
  emit('close')
  visible.value = false;
}

defineExpose({
  open
})


onUnmounted(() => {

})
</script>


<template>
  <t-dialog attach="body" :cancelBtn="null" :confirmBtn="null" @close="close" :visible="visible">
    <template #header>用户详情</template>
    <div class="empty"></div>
    <t-row>
      <t-col :span="11">
        <t-image fit="cover" style="width:100px;height:100px" :src="accountAvatarUrl" />
      </t-col>
      <div class="empty"></div>
      <t-col :span="11">用户名：{{ userId }}</t-col>
      <div class="empty"></div>
      <t-col :span="11">昵称：{{ nickname }}</t-col>
      <div class="empty"></div>
      <t-col :span="11">加入时间：{{ joinTime }}</t-col>
      <div class="empty"></div>
    </t-row>
  </t-dialog>
</template>

<style lang="scss" scoped>
.userInfo__wrapper {}
</style>
