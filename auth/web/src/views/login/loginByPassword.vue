<script setup>
import {
  ref, onMounted, computed, watch,
  nextTick
} from "vue";
import {
  useRouter
} from "vue-router";
import {
  useCookie
} from "v3hooks";
import {
  MessagePlugin,
  LoadingPlugin
} from "tdesign-vue-next";
import {
  UserIcon,
  LockOnIcon,
} from 'tdesign-icons-vue-next'
import {
  Base64
} from "js-base64";

import {
  usePageStore
} from "@/store/page";

import {
  setToken,
  setRefreshToken,
  setTokenType,
  setAuthType,
  setAppId,
  setEndpointId
} from "@/utils";
import useState from "@/hooks/useState";
import useAgreementGate from "@/hooks/useAgreementGate";

import Dialog from "@/components/dialog";
import GraphicValidation from "@/components/graphicValidation";

import {
  getOauth2Token_api,
  sendVerifyCodeSms_api,
  resetAccountPasswordByPhone_api,
  getMyAppUserLogoffStatus_api,
  unlogoffMyAppUser_api
} from "@/api";


const pageStore = usePageStore();
const router = useRouter();

const lastPath = computed(() => pageStore.lastPathGetter);

const userAgreement = _this.userAgreement; // 用户协议
const privacyPolicy = _this.privacyPolicy; // 隐私政策


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
  username: [{
    required: true,
    message: "用户名必填",
    type: "error",
  }],
  password: [{
    required: true,
    message: "密码必填",
    type: "error",
  }],
};
let formRef = ref(null);
const [form, setForm] = useState({
  username: "",
  password: "",
});

// 记住 用户名 密码
const [remember, setRemember] = useState({
  username: "",
  password: "",
  isChecked: true, // 默认记住用户名和密码(用户曾主动取消则沿用其选择)
});

onMounted(() => {
  const _rememberStr = useCookie(_this.cookie.remember);
  if (_rememberStr.value) {
    let _remember = JSON.parse(Base64.decode(_rememberStr.value));
    setRemember({
      ...remember.value,
      username: _remember.username,
      password: _remember.password,
      isChecked: _remember.isChecked,
    });
    if (_remember.isChecked) {
      setForm({
        ...form.value,
        username: _remember.username,
        password: _remember.password,
      });
    }
  }
});

const onSubmit = ({
  validateResult,
  firstError,
  e
}) => {
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
    let username = form.value.username;
    let password = form.value.password;
    let _remember; // 下方两个分支必然赋值
    if (remember.value.isChecked) {
      _remember = {
        username: username,
        password: password,
        isChecked: remember.value.isChecked,
      };
    } else {
      _remember = {
        username: "",
        password: "",
        isChecked: false,
      };
    }

    LoadingPlugin(true);
    try {
      let _rememberStr = useCookie(_this.cookie.remember, {
        watch: true,
        expires: 999,
      });
      _rememberStr.value = Base64.encode(JSON.stringify(_remember));
      let headers = {
        "Accept-Language": "zh-CH,zh;q=0.9,en;q=0.8",
        Accept: "application/json",
      };
      let formData = new FormData();
      formData.append("client_id", _this.client_id);
      formData.append("client_secret", _this.client_secret);
      formData.append("grant_type", "app_user:password");
      formData.append("username", username);
      formData.append("password", password);

      const res = await getOauth2Token_api(formData, headers);
      if (res.code == "Success") {
        console.log(res.data, 'data======');
        // MessagePlugin.success("登录成功");
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
      LoadingPlugin(false);
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

// 修改密码
const pass_rules = {
  phoneNumber: [{
    required: true,
    message: "手机号必填",
    type: "error",
    trigger: "change"
  },],
  verifyCode: [{
    required: true,
    message: "验证码必填",
    type: "error",
    trigger: "change"
  },],
  password: [{
    required: true,
    message: "新密码必填",
    type: "error",
    trigger: "change"
  }],
}; let passFormRef = ref(null);
const [visible, setVisible] = useState(false);
let [passForm, setPassForm] = useState({
  phoneNumber: null,
  verifyCode: null,
  password: null,
});
const onSubmitPassword = async () => {
  const validate = await passFormRef.value.validate();
  if (validate == true) {
    let { phoneNumber, verifyCode, password } = passForm.value;
    let params = {
      phoneNumber, verifyCode, password
    };
    let res = await resetAccountPasswordByPhone_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success("修改密码成功");
      onClosePassword();
    }
  }
};
const modifyPassword = () => {
  setVisible(true);
  passFormRef.value.clearValidate();
};
const onClosePassword = () => {
  setVisible(false);
  setPassForm({
    phoneNumber: null,
    verifyCode: null,
    password: null,
  });
};

// 图形验证码
const [show, setShow] = useState(false);
const onValideCode = () => {
  if (!passForm.value.phoneNumber) {
    MessagePlugin.error('请填写手机号');
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
    phoneNumber: passForm.value.phoneNumber,
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
  <t-form :rules="rules" class="formBox" ref="formRef" :data="form" :label-width="0" @submit="onSubmit">
    <t-form-item name="username">
      <t-input v-model="form.username" clearable placeholder="请输入用户名">
        <template #prefix-icon>
          <UserIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-form-item name="password">
      <t-input v-model="form.password" type="password" clearable placeholder="请输入密码">
        <template #prefix-icon>
          <LockOnIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-checkbox v-model="remember.isChecked"><span style="font-size: xx-small">记住用户名和密码</span></t-checkbox>
    <div class="empty"></div>
    <t-checkbox v-model="passIsAgree" @change='onChange'>
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
    <div class="tipBox">
      <span @click="modifyPassword" class="forget">忘记密码</span>
    </div>
  </t-form>

  <!-- 重置密码 -->
  <Dialog :zIndex="100" @confirm="onSubmitPassword" @close="onClosePassword" width="25%" attach="body"
    :visible="visible">
    <template #title>修改密码</template>
    <t-form :rules="pass_rules" ref="passFormRef" :data="passForm">
      <t-row>
        <t-col :span="11">
          <t-form-item name="phoneNumber" label="手机号">
            <t-input v-model="passForm.phoneNumber"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="verifyCode" label="验证码">
            <t-input v-model="passForm.verifyCode"></t-input>
            <t-button :disabled="second < 60" @click="onValideCode">{{
    second == 60 ? "发送验证码" : second
  }}</t-button>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="password" label="新密码">
            <t-input v-model="passForm.password"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 图形验证码 -->
  <GraphicValidation @confirm="onSubmitCode" @close="onCloseCode" :show="show"></GraphicValidation>
</template>

<style lang="scss" scoped>
.formBox {
  width: 89%;
  margin: auto;
  min-width: 250px;

  .tipBox {
    display: flex;
    justify-content: flex-end;

    .forget {
      color: rgb(153, 153, 153);
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
