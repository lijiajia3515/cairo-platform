<template>
  <div class="userInfo__wrapper">
    <t-card style="height:100%" :bordered="false" hover-shadow>
      <main>
        <t-layout>
          <t-aside class="asideBox">
            <div class="menuBox">
              <!-- <RollbackIcon @click="goBack" class="pick" size="24px" /> -->
              <t-menu style="width: 100%;" v-model="acitve" theme="light" width="200px" height="550px">
                <div class="empty"></div>
                <div class="logoBox">
                  <t-image :src="userInfos?.accountAvatarUrl"
                    :style="{ width: '60px', height: '60px', borderRadius: '50%' }" fit="cover" overlayTrigger="hover">
                    <template #overlayContent>
                      <div @click="onUpdateAvatarUrl"
                        style="width: 100%;height: 100%;background: rgba(0,0,0,0.7);color:#fff;font-size: 12px;display: flex;align-items: center;justify-content: center;">
                        更换头像</div>
                    </template>
                  </t-image>
                  <span style="display: inline-block;font-size: 18px;margin-left:10px;">{{ userInfos?.nickname }}</span>
                </div>
                <div class="empty"></div>
                <div style="box-sizing: border-box;padding:0 5px 0 5px;">
                  <t-menu-item v-for="(item, index) in menuList" :key="index" :value="item?.value">{{ item?.name
                    }}</t-menu-item>
                </div>
              </t-menu>
            </div>
          </t-aside>
          <t-content class="contentBox">
            <div class="empty"></div>
            <template v-if="acitve == 'info'">
              <Info></Info>
            </template>
            <template v-if="acitve == 'account'">
              <Account></Account>
            </template>
            <template v-if="acitve == 'log_login'">
              <LoginLog />
            </template>
            <template v-if="acitve == 'log_service'">
              <LogBiz />
            </template>
            <template v-if="acitve == 'session_list'">
              <sessionList />
            </template>
          </t-content>
        </t-layout>
      </main>
    </t-card>
  </div>

  <!-- 剪裁图片 -->
  <CutImage v-if="flag" @close="closeAvatarData" :accountId="userInfos.accountId" type="public" picType="avatar"
    @confirm="getAvatarData" ref="cutImageRef"></CutImage>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, onBeforeUnmount } from 'vue';
import {
  DialogPlugin,
  MessagePlugin,
  LoadingPlugin,
} from 'tdesign-vue-next';

import CutImage from '@/components/cutImage';
import axios from 'axios';
import {
  getMyAppUserInfo_api,
  modifyMyAccountAvatar_api
} from '@/api';
import {
  RollbackIcon
} from 'tdesign-icons-vue-next';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import useState from '@/hooks/useState';
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
import { getToken, getTokenType } from '@/utils';

const token = getToken()
let token_type = getTokenType();


import Info from './components/info.vue';
import Account from './components/account.vue';
import LogBiz from './components/logBiz.vue';
import LoginLog from './components/loginLog.vue';
import sessionList from './components/sessionList.vue';

onMounted(() => {
  let type = localStorage.getItem('infoType')
  if (type == 'account') {
    setActive('account')
  }
  if (route.query.code) {
    setActive('account')
  }
  getMyUserInfo()
})
const [acitve, setActive] = useState('info');
const [menuList, setMenuList] = useState([
  { name: '个人资料', value: 'info' },
  { name: '账号管理', value: 'account' },
  { name: '业务日志', value: 'log_service' },
  { name: '登录日志', value: 'log_login' },
  { name: '会话列表', value: 'session_list' },
]);

const userInfos = computed(() => userStore.userGetter);
const getMyUserInfo = async () => {
  let res = await getMyAppUserInfo_api({});
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}

const goBack = () => {
  router.go(-1)
}


let cutImageRef = ref(null);
const [flag, setFlag] = useState(false);
const onUpdateAvatarUrl = () => {
  setFlag(true);
  nextTick(() => {
    cutImageRef.value.open();
  })
}
const closeAvatarData = () => {
  setFlag(false);
}
/**
 * 修改头像
 * @param {Object} data
 * @param {Object} data.url
 * @param {Object} data.s3
 */
const getAvatarData = async (data) => {
  console.log(data, 'data===');
  let headers = {
    "Content-Type": 'image/png'
  }
  let res = await modifyMyAccountAvatar_api(data, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('修改成功');
    cutImageRef.value.close();
    setFlag(false);
    getMyUserInfo();
  }

}

</script>

<style lang="scss" scoped>
.userInfo__wrapper {
  width: 100%;
  height: calc(100vh - 70px);

  main {

    .asideBox {
      width: 220px;
      overflow: hidden;
      height: calc(100vh - 100px);

      // overflow-y: auto;
      .menuBox {
        border: 1px solid #ededed;
        box-sizing: border-box;
        height: calc(100vh - 100px);
      }

      .logoBox {
        width: 100%;
        border-bottom: 1px solid #ededed;
        padding: 5px 20px 20px 20px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
      }
    }

    .contentBox {
      width: calc(100vh - 220px);
      overflow-y: auto;
      height: calc(100vh - 100px);
      background: #fff;
    }
  }
}
</style>
