<template>
  <div class="loginLog__wrapper">
    <t-tabs v-model="active" theme="card">
      <t-tab-panel :destroyOnHide="true" value="web" label="网页端">
        <WebApp></WebApp>
      </t-tab-panel>
      <t-tab-panel :destroyOnHide="true" value="Android" label="安卓">
        <AndroidApp></AndroidApp>
      </t-tab-panel>
      <t-tab-panel :destroyOnHide="true" value="ios" label="ios">
        <IosApp></IosApp>
      </t-tab-panel>
    </t-tabs>
  </div>
</template>

<script setup>
defineOptions({ name: 'manage-develop-app_release' })

import { ref } from 'vue';

import WebApp from './components/webApp.vue';
import IosApp from './components/iosApp.vue';
import AndroidApp from './components/androidApp.vue';

const active = ref('web')
</script>

<style lang="scss" scoped></style>
