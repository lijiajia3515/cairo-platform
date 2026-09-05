<script setup>
import { MessagePlugin } from "tdesign-vue-next";
import {
  CallIcon,
  LogoGithubIcon,
} from 'tdesign-icons-vue-next'
import { nextTick, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import {
  useCookie
} from "v3hooks";
import {
  Base64
} from "js-base64";
import { setToken, setRefreshToken, setTokenType, setAuthType, setAppId, setEndpointId } from "@/utils";
import useState from "@/hooks/useState";
import useAgreementGate from "@/hooks/useAgreementGate";

import GraphicValidation from "@/components/graphicValidation";

import {
  sendVerifyCodeSms_api,
  getOauth2Token_api,
  getMyAppUserLogoffStatus_api,
  unlogoffMyAppUser_api
} from "@/api";

const router = useRouter();

const userAgreement = _this.userAgreement; // 用户协议
const privacyPolicy = _this.privacyPolicy; // 隐私政策


const [remember, setRemember] = useState({
  phoneNumber: "",
  isChecked: true, // 默认记住手机号(用户曾主动取消则沿用其选择)
});


onMounted(() => {
  const _rememberStr = useCookie(_this.cookie.remember1);
  if (_rememberStr.value) {
    let _remember = JSON.parse(Base64.decode(_rememberStr.value));
    setRemember({
      ...remember.value,
      phoneNumber: _remember.phoneNumber,
      isChecked: _remember.isChecked,
    });
    if (_remember.isChecked) {
      setForm({
        ...form.value,
        phoneNumber: _remember.phoneNumber,
      });
    }
  }
});

const props = defineProps({
  isAgree: {
    type: Boolean,
    default: false
  },
});

const [passIsAgree, setPassIsAgree] = useState(props.isAgree);
watch(() => props.isAgree, () => {
  setPassIsAgree(props.isAgree)
})
const agreementGate = useAgreementGate();

const emit = defineEmits(['changeData']);
const onChange = (val) => {
  localStorage.setItem('changeData', val)
  emit('changeData', val)
}

const rules = {
  phoneNumber: [
    {
      required: true,
      message: "手机号必填",
      type: "error",
      trigger: "change",
    },
  ],
  verifyCode: [
    {
      required: true,
      message: "验证码必填",
      type: "error",
      trigger: "change",
    },
  ],
};
// 验证码登录
const [form, setForm] = useState({
  phoneNumber: null,
  verifyCode: null,
});
const onSubmit = ({ validateResult, firstError, e }) => {
  e.preventDefault();
  if (validateResult != true) {
    MessagePlugin.warning(firstError);
    return;
  }
  if (passIsAgree.value == false) {
    agreementGate(() => {
      setPassIsAgree(true);
      onChange(true); // 持久化同意状态,下次登录默认勾选
      doLogin();
    });
    return;
  }
  doLogin();
};

const doLogin = async () => {
    let phoneNumber = form.value.phoneNumber;
    let _remember; // 下方两个分支必然赋值
    if (remember.value.isChecked) {
      _remember = {
        phoneNumber: phoneNumber,
        isChecked: remember.value.isChecked,
      };
    } else {
      _remember = {
        phoneNumber: "",
        isChecked: false,
      };
    }

    try {

      let _rememberStr = useCookie(_this.cookie.remember1, {
        watch: true,
        expires: 999,
        secure: true,
        sameSite: 'none'
      });
      _rememberStr.value = Base64.encode(JSON.stringify(_remember));


      let headers = {
        "Accept-Language": "zh-CH,zh;q=0.9,en;q=0.8",
        Accept: "application/json",
      };
      let formData = new FormData();
      formData.append("client_id", _this.client_id);
      formData.append("client_secret", _this.client_secret);
      formData.append("grant_type", "app_user:verify_code");
      formData.append("tenant_id", _this.tenant_id);
      formData.append("phone_number", form.value.phoneNumber);
      formData.append("verify_code", form.value.verifyCode);

      const res = await getOauth2Token_api(formData, headers);
      if (res.code == "Success") {
        MessagePlugin.success("登录成功");
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
        // router.replace("/");
      }
    } finally {
    // 收尾占位
    }
};

const getAccountStatus = async () => {
  let res = await getMyAppUserLogoffStatus_api()
  if (res.code === "Success") {
    if (res.data.logoffStatus === "No") {
      router.replace("/");
    } else {
      const confirmDia = DialogPlugin({
        header: '提示',
        body: `你的账号已提交注销申请，将于${res.data.logoffPendingTime}删除。如你想放弃注销流程，请点击“放弃注销”;如确定注销此账号，点击“了解”后可通过其他账号进行登录。`,
        confirmBtn: '了解',
        cancelBtn: '放弃注销',
        onConfirm: async ({ e }) => {
          confirmDia.hide();
          let token = setToken();
          let refresh_token = setRefreshToken();
          let token_type = setTokenType();
          let auth_type = setAuthType();
          token.value = null;
          refresh_token.value = null;
          token_type.value = null;
          auth_type.value = null;
        },
        onClose: async ({ e, trigger }) => {
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
      });
    }
  }
}



// 图形验证码
const [show, setShow] = useState(false);
const onValideCode = () => {
  if (passIsAgree.value == false) {
    MessagePlugin.error('请您同意用户条款')
    return;
  }
  if (!form.value.phoneNumber) {
    MessagePlugin.error("请输入手机号");
    return;
  }
  setShow(true);
};
const onCloseCode = () => {
  setShow(false);
};

/**
 * 验证图形验证码成功
 * @param {String} captchaToken
 */
const onSubmitCode = (captchaToken) => {
  sendVerifyCode(captchaToken);
  setShow(false);
};

/**
 * 发送手机验证码
 * @param {String} captchaToken
 */
const [second, setSecond] = useState(60);
const sendVerifyCode = async (captchaToken) => {
  let params = {
    phoneNumber: form.value.phoneNumber,
  };
  let headers = {
    "Captcha-Token": captchaToken,
  };
  let res = await sendVerifyCodeSms_api(params, headers);
  if (res.code == "Success") {
    MessagePlugin.success("发送验证码成功");
    let timer = setInterval(() => {
      let num = second.value - 1;
      setSecond(num);
      if (second.value == 0) {
        setSecond(60);
        clearInterval(timer);
      }
    }, 1000);
  }
};
</script>

<template>
  <t-form :data="form" @submit="onSubmit" :rules="rules" ref="formRef" class="formBox" :label-width="0">
    <t-form-item name="phoneNumber">
      <t-input v-model="form.phoneNumber" placeholder="请输入手机号">
        <template #prefix-icon>
          <CallIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-form-item name="verifyCode">
      <t-input v-model="form.verifyCode" placeholder="请输入验证码">
        <template #prefix-icon>
          <LogoGithubIcon />
        </template>
      </t-input>
      <t-button :disabled="second < 60" @click="onValideCode">{{
    second == 60 ? "发送验证码" : second
  }}</t-button>
    </t-form-item>
    <t-checkbox v-model="remember.isChecked"><span style="font-size: xx-small">记住手机号</span></t-checkbox>
    <div class="empty"></div>
    <t-checkbox v-model="passIsAgree" @change="onChange">
      <p style="font-size: xx-small">已阅读并同意
        <a style="color:#333;font-size: x-small" :href="userAgreement" target="_blank"
          rel="noopener noreferrer"><span>《用户协议》</span></a>&nbsp;
        <a style="color:#333" :href="privacyPolicy" target="_blank"
          rel="noopener noreferrer"><span>《隐私政策》</span></a>
      </p>
    </t-checkbox>
    <div class="empty"></div>
    <t-form-item>
      <t-button theme="primary" type="submit" block>登录</t-button>
    </t-form-item>
    <div class="empty"></div>
  </t-form>

  <!-- 图形验证码 -->
  <GraphicValidation @confirm="onSubmitCode" @close="onCloseCode" :show="show"></GraphicValidation>
</template>

<style lang="scss" scoped>
.formBox {
  width: 89%;
  margin: auto;
  min-width: 250px;
}
</style>
