<!-- 与平台首页 /home 同源:菜单树里的落地入口复用同一视图,仅组件名随路径 slug
     (keep-alive 按组件名缓存,双路由各存一份实例);页面演进只改 views/home -->
<script setup>
defineOptions({ name: 'manage-home' })

import Home from '@/views/home/index.vue';
</script>

<template>
  <Home />
</template>
