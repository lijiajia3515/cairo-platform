// template.js module.exports

export const vueTemplate = (compoenntName) => {
  return `<script setup lang="jsx">
import {ref, onMounted} from 'vue';
import {useRouter} from 'vue-router';

import useState from '@/hooks/useState';

onMounted(() => {

});


</script>


<template>
  <div class="${compoenntName}__wrapper">
    ${compoenntName}
  </div>
</template>

<style lang="scss" scoped>
.${compoenntName}__wrapper{

}
</style>
`
}
// export default {
//   vueTemplate: compoenntName => {
  
//   }
// }