<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  MoonIcon,
  SunnyIcon
} from 'tdesign-icons-vue-next';

import { resetRouter } from '@/router';

import { useUserStore } from '@/store/user';
import useState from '@/hooks/useState';

import {
  setToken,
  setRefreshToken,
  setTokenType,
  setAuthType,
  setAppId,
  setEndpointId,
} from '@/utils';

import {
  getMyAppUserInfo_api,
  logoutAppUserAuthorization_api
} from '@/api';

const router = useRouter();

const userStore = useUserStore();

const user = computed(() => userStore.userGetter);

const options = [
  { content: '个人信息', value: 2 },
  { content: '退出登录', value: 1 },
];
const clickHandler = (data) => {
  switch (data.content) {
    case '退出登录':
      loginOff()
      // router.replace('/login');
      break;
    case '个人信息':
      router.push('/main/userInfo');
      break;
    default:
      break;
  }
  // MessagePlugin.success(`选中【${data.content}】`);
};

const loginOff = async () => {
  try {
    await logoutAppUserAuthorization_api({})
  } catch (err) {
    // 服务端登出失败（token 已失效 / 网络异常等）不阻断本地凭证清理
    console.log(err)
  } finally {
    resetRouter(); // 重置路由
    userStore.savePermissionList([]);
    // 无论服务端登出结果如何，强制清除本地 token 与登录上下文，避免死循环卡死
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
    sessionStorage.removeItem('zhgd_manager_lastPath')
    window.location.href = window.location.origin + '/login'
  }
}


let theme = ref('white'); // black
const onChangeTheme = () => {
  if (theme.value == 'white') {
    theme.value = 'black';
    document.documentElement.setAttribute('theme-mode', 'dark');
  } else {
    theme.value = 'white';
    document.documentElement.removeAttribute('theme-mode');
  }
}

// 获取当前用户信息
const getUser = async () => {
  let res = await getMyAppUserInfo_api();
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}


onMounted(() => {
  getUser();
});



onUnmounted(() => {

})
</script>


<template>
  <div class="header__wrapper">
    <div class="headerContainer">
      <div class="left" style="display:flex">
        <div class="item">
          <img style="width: 40px;height: 40px;z-index: 999;"
            src="/images/icon-40x40.png" />
        </div>
        <div class="item">
          <div class="title">Cairo运营平台</div>
        </div>
      </div>
      <div class="right">
        <div class="item">
          <t-image lazy error="加载失败" class="headImage" :src="user.accountAvatarUrl" fit="cover" />
        </div>
        <div class="item">
          <t-dropdown :options="options" trigger="click" @click="clickHandler">
            <t-space>
              <t-button variant="text">
                {{ user.nickname || user.accountNickname }}
                <template #suffix> <t-icon name="chevron-down" size="16" /></template>
              </t-button>
            </t-space>
          </t-dropdown>
        </div>
        <div @click="onChangeTheme" class="item pick">
          <MoonIcon v-if="theme == 'white'" />
          <SunnyIcon v-if="theme == 'black'" />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.header__wrapper {
  width: 100%;
  height: 100%;
  border-bottom: 1px solid #ededed;
  box-sizing: border-box;

  .headerContainer {
    width: 95%;
    height: 100%;
    margin: auto;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      .item {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        margin-right: 5px;

        .title {
          font-weight: 700;
          font-size: 18px;
        }
      }
    }

    .right {
      display: flex;
      justify-content: flex-end;
      align-items: center;

      .item {
        margin-left: 5px;

        .headImage {
          width: 40px;
          height: 40px;
        }
      }
    }
  }
}
</style>
