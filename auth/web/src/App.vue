<script setup>
import { ref, onMounted, provide, nextTick } from 'vue';

onMounted(() => {

});

let isRouterAlive = ref(true);

const reload = () => {
  isRouterAlive.value = false;
  nextTick(() => {
    isRouterAlive.value = true;
  })
}

provide('reload', reload)

</script>

<template>
  <!-- Component 是 vue-router scoped slot 的 API 契约（大写 C），不属于业务字段，禁止 camelCase 翻转 -->
  <router-view v-if="isRouterAlive" v-slot="{ Component }">
    <component :is="Component" />
  </router-view>
</template>

<style lang="scss" scoped>
// 挂载前
</style>
