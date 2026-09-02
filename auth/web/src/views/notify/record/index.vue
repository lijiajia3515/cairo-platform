<template>
  <div>

  </div>
</template>

<script setup>
import {
  ref, reactive, onMounted, onUnmounted,
  nextTick, watch,
} from 'vue';
import { useRouter } from 'vue-router';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import useState from '@/hooks/useState';
import List from '@/components/list';
import Dialog from '@/components/dialog';

</script>

<style lang="scss" scoped></style>