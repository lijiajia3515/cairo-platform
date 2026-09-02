<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import useState from '@/hooks/useState';

import {
  setToken,
  setRefreshToken,
  setTokenType,
  setAuthType,
  setAppId,
  setEndpointId
} from '@/utils';

onMounted(() => {

});

const router = useRouter();


const goRouteHome = () => {
  router.replace('/home');
}

const goRouteLast = () => {
  router.go(-1);
}

const goLoginOut = () => {
  let token = setToken();
  let refresh_token = setRefreshToken();
  let token_type = setTokenType();
  let auth_type = setAuthType();
  let appId = setAppId();
  let endpointId = setEndpointId();
  token.value = null;
  refresh_token.value = null;
  token_type.value = null;
  auth_type.value = null;
  appId.value = null;
  endpointId.value = null;
  router.replace('/login');
}

onUnmounted(() => {

})
</script>


<template>
  <div class="notfound__wrapper">
    <div class="main">
      <h6 class="title">404 Not Found</h6>
      <p>页面找不到</p>
      <div class="empty"></div>
      <div>
        <t-space>
          <t-button @click="goRouteHome" variant="outline" theme="default">返回首页</t-button>
          <t-button @click="goRouteLast" variant="outline" theme="default">返回上一级</t-button>
          <t-button @click="goLoginOut" variant="outline" theme="default">退出登录</t-button>
        </t-space>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.notfound__wrapper {
  width: 100%;
  padding-top: 20px;
  box-sizing: border-box;

  .main {
    width: 200px;
    height: 100px;
    margin: auto;

    .title {
      font-size: 26px;
      line-height: 80px;
      font-weight: bold;
    }

    p {
      font-weight: 700;
      font-size: 18px;
    }
  }
}
</style>
