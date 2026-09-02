<template>
  <div class="main">
    <iframe :src="currentUrl" frameborder="0"></iframe>
  </div>
</template>

<script setup>
import {
  ref, reactive, onMounted, onUnmounted,
  nextTick, watch,
} from 'vue';
import useState from '@/hooks/useState';
import { useRoute } from "vue-router"
const [currentUrl, setCurrentUrl] = useState('')
onMounted(() => {
  const route = useRoute()
  setCurrentUrl(route.query.url)
})
</script>

<style lang="scss" scoped>
.main {
  width: 100%;
  height: calc(100vh - 72px);
  margin: auto;

  iframe {
    width: 100%;
    height: 100%;
  }
}
</style>