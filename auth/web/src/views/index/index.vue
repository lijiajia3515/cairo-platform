<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

import { merge } from 'lodash';
import enConfig from 'tdesign-vue-next/es/locale/en_US';
import zhConfig from 'tdesign-vue-next/es/locale/zh_CN';
import koConfig from 'tdesign-vue-next/es/locale/ko_KR';

import useState from '@/hooks/useState';

import Menu from '@/components/menu';
import Header from '@/components/header';

let translateConfig = ref(zhConfig);

const globalConfig = merge(translateConfig, {
  // 可以在此处定义更多自定义配置，具体可配置内容参看 API 文档
  calendar: {},
  table: {},
  pagination: {},
});


onMounted(() => {

});

onUnmounted(() => {

});

const changeConfig = (type) => {
  if (type == 'zh') {
    translateConfig.value = zhConfig;
  }
  if (type == 'en') {
    translateConfig.value = enConfig;
  }
  if (type == 'ko') {
    translateConfig.value = koConfig;
  }
}


const changeMyStyle = () => {
  document.documentElement.setAttribute('theme-mode', 'mystyle');
}

</script>


<template>
  <t-config-provider :global-config="globalConfig">
    <t-layout>
      <t-header>
        <Header></Header>
        <!-- <t-affix ref="affix" :offset-top="0">
          
        </t-affix> -->
        <!-- <t-space>
          <t-button @click="changeNomal">浅色主题</t-button>
          <t-button @click="changeBlack">暗黑主题</t-button>
          <t-button @click="changeMyStyle">自定义主题</t-button>

          <t-button @click="changeConfig('zh')">中文切换</t-button>
          <t-button @click="changeConfig('en')">英文切换</t-button>
          <t-button @click="changeConfig('ko')">韩语切换</t-button>
        </t-space> -->
      </t-header>
      <t-layout>
        <t-aside class="aside-content">
          <Menu></Menu>
        </t-aside>
        <t-content class="lay-content">
          <!-- Component 是 vue-router scoped slot 的 API 契约（大写 C），不属于业务字段，禁止 camelCase 翻转 -->
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <div :key="$route.path">
                <component :is="Component"></component>
              </div>
            </transition>
          </router-view>
        </t-content>
      </t-layout>
    </t-layout>
  </t-config-provider>
</template>

<style lang="scss" scoped>
.lay-content {
  width: calc(100% - 232px);
  box-sizing: border-box;
  padding: 10px 10px 0 10px;
}

@media screen and (min-width: 0px) and (max-width: 1200px) {
  .aside-content {
    width: 64px;
  }
}

@media screen and (min-width: 0px) and (max-width: 750px) {
  .lay-content {
    width: calc(100% - 64px);
  }
}
</style>