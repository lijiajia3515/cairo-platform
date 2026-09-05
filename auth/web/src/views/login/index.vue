<script setup>
import {
  onMounted, ref, nextTick
} from 'vue';
import { useRouter, useRoute } from 'vue-router';

import { useUserStore } from '@/store/user';
import { useTagsViewStore } from '@/store/tagsView';
import { randomString } from '@/utils/tips';
import { setToken, setRefreshToken, setTokenType, setAuthType, setAppId, setEndpointId } from "@/utils";
import LoginByPassword from './loginByPassword.vue';
import LoginByCode from './loginByCode.vue';

import {
  getOauth2Token_api,
  getEnabledSnsProviderList_api,
  getMyAppUserLogoffStatus_api,
  unlogoffMyAppUser_api
} from "@/api";

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

let isAgree = ref(false);

onMounted(() => {
  if (route.query.code) {
    getWvLogin()
  } else {
    userStore.saveMenuList([]);
    getSnsProviderList()
  }

  let changeData = localStorage.getItem('changeData')
  if (changeData !== null) {
    isAgree.value = JSON.parse(changeData)
  } else {
    isAgree.value = false
  }
})

const handleData = (data) => {
  isAgree.value = data
}


const getWvLogin = async () => {
  // LoadingPlugin(true);
  try {
    let code = route.query.code;
    let sns_provider_id = route.query.sns_provider_id;
    if (code) {
      let formData = new FormData();
      formData.append("client_id", _this.client_id);
      formData.append("client_secret", _this.client_secret);
      formData.append('grant_type', 'app_user:account_sns_code');
      formData.append('sns_provider_id', sns_provider_id);
      formData.append('sns_type', 'wx_web');
      formData.append('sns_code', code);
      let res = await getOauth2Token_api(formData);
      if (res.code == 'Success') {
        // 微信授权登录成功
        let token = setToken();
        let refresh_token = setRefreshToken();
        let token_type = setTokenType();
        let auth_type = setAuthType();
        let appId = setAppId();
        let endpointId = setEndpointId();
        token.value = res.data.access_token;
        refresh_token.value = res.data.refresh_token;
        token_type.value = res.data.token_type;
        auth_type.value = res.data.auth_type;
        appId.value = res.data.app_id;
        endpointId.value = res.data.endpoint_id;
        nextTick(() => {
          getAccountStatus()
        })
      } else {
        setTimeout(() => {
          window.location.href = window.location.origin + '/login'
        }, 2000)
      }
    } else {
      getCode();
    }
  } finally {
    // LoadingPlugin(false);
  }
}

const getAccountStatus = async () => {
  let res = await getMyAppUserLogoffStatus_api()
  if (res.code === "Success") {
    if (res.data.logoffStatus === "No") {
      MessagePlugin.success("登录成功");
      router.replace("/");
    } else {
      const confirmDia = DialogPlugin({
        header: '提示',
        body: `你的账号已提交注销申请，将于${res.data.logoffPendingTime}删除。如你想放弃注销流程，请点击“放弃注销”;如确定注销此账号，点击“了解”后可通过其他账号进行登录。`,
        confirmBtn: '放弃注销',
        cancelBtn: '了解',
        closeOnOverlayClick: false,
        onConfirm: async ({ e }) => {
          LoadingPlugin(true);
          try {
            let res = await unlogoffMyAppUser_api({});
            if (res.code == 'Success') {
              MessagePlugin.success('登录成功');
              confirmDia.hide();
              router.replace("/");
            }
          } finally {
            LoadingPlugin(false);
          }

        },
        onClose: async ({ e, trigger }) => {
          confirmDia.hide();
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
          useTagsViewStore().resetTags() // 清空多标签(会话级缓存)
          window.location.href = window.location.origin + '/login'
        },
      });
    }
  }
}



let snsProviderList = ref([])
let sns_provider_id = ref('')
let sns_provider_access_key = ref('')
const getSnsProviderList = async () => {
  let params = {
    appId: 'cairo',
    snsTypes: ['wx_web'],
    enabled: true,
  }
  let res = await getEnabledSnsProviderList_api(params)
  if (res.code === "Success") {
    if (res.data) {
      snsProviderList.value = res.data
    } else {
      snsProviderList.value = []
    }

  }
}

const goWxLogOn = (val) => {
  if (!isAgree.value) {
    MessagePlugin.error('请您同意用户条款')
    return
  }
  console.log(val, 'val====');
  sns_provider_id.value = val.snsProviderId
  sns_provider_access_key.value = val.clientId
  nextTick(() => {
    getCode()
  })
}

let getCode = () => {
  let local = encodeURIComponent(window.location.href + '?sns_provider_id=' + sns_provider_id.value); //获取当前页面地址作为回调地址
  let path = encodeURIComponent(_this.callbackUrl + local);
  window.location.href =
    "https://open.weixin.qq.com/connect/qrconnect?"
    + "appid=" + sns_provider_access_key.value
    + "&redirect_uri=" + path
    + "&response_type=code&scope=snsapi_login&state=" + randomString(6) + "#wechat_redirect";
}


const goRoute = (path) => {
  router.replace(path)
}
</script>

<template>
  <div class="login_page">
    <div class="logoItem">
      <div class="item">
        <img style="width: 36px;height: 36px;display: block;" src="/logo.svg" alt="CAIRO" />
      </div>
      <div class="item">
        <div class="title">CAIRO</div>
      </div>
    </div>
    <div class="loginContainer">
      <div class="leftBox"></div>
      <div class="rightBox">
        <div @click="goRoute('/register')" class="registerBox">
        </div>
        <div @click="goRoute('/register')" class="register-text">注册</div>
        <h5 class="loginTitle">登录</h5>
        <t-tabs :default-value="1">
          <t-tab-panel :value="1" label="密码登录">
            <div class="empty"></div>
            <LoginByPassword :isAgree='isAgree' @changeData='handleData' />
          </t-tab-panel>
          <t-tab-panel :value="2" label="验证码登录">
            <div class="empty"></div>
            <LoginByCode :isAgree='isAgree' @changeData='handleData'></LoginByCode>
          </t-tab-panel>
        </t-tabs>

        <div class="logon" v-if="snsProviderList.length > 0">
          <p style="margin-bottom: 10px"><span style="color: #ccc;">————</span><span
              style="font-size: 14px;">&nbsp;其他方式登录&nbsp;</span><span style=" color: #ccc;">————</span>
          </p>
          <div style="display: flex;">
            <img v-for="(item, index) in snsProviderList" :key="index" :src="item.snsProviderPartnerIcon" alt=""
              style="width: 30px;height: 30px;" @click="goWxLogOn(item)">
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use './index.scss';

.logoItem {
  display: flex;
  align-items: center;
  margin-top: 20px;
  margin-left: 40px;

  .item {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    margin-right: 10px;

    .title {
      font-family: Inter, var(--td-font-family);
      font-weight: 600;
      font-size: 20px;
      letter-spacing: 0.08em;
    }
  }
}

.logon {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
