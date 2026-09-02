<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import {
  CallIcon,
  LogoGithubIcon,
  LockOnIcon,
  UserIcon,
} from 'tdesign-icons-vue-next'

import { useInterval } from "v3hooks";

import useState from '@/hooks/useState';
import useAgreementGate from '@/hooks/useAgreementGate';

import GraphicValidation from '@/components/graphicValidation';

import {
  registerAppUser_api,
  sendVerifyCodeSms_api,
} from '@/api';

const userAgreement = _this.userAgreement; // 用户协议
const privacyPolicy = _this.privacyPolicy; // 隐私政策

let isAgree = ref(false);
const agreementGate = useAgreementGate();

const rules = {
  phoneNumber: [
    { required: true, message: '手机号必填', type: 'error', trigger: 'blur' },
  ],
  verifyCode: [
    { required: true, message: '验证码必填', type: 'error', trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '昵称必填', type: 'error', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码必填', type: 'error', trigger: 'blur' },
    { min: 6, message: '输入密码字数应在6到40之间', type: 'error', trigger: 'blur' },
    { max: 40, message: '输入密码字数应在6到40之间', type: 'error', trigger: 'blur' },
  ]
}

const router = useRouter();

let formRef = ref(null);

const [show, setShow] = useState(false);

let delay = ref(1000);
const [disabled, setDisabled] = useState(false);
const [second, setSecond] = useState(60);

const [form, setForm] = useState({
  phoneNumber: null, // 手机号
  verifyCode: null, // 验证码
  password: null,
  nickname: null, // 昵称
});

onMounted(() => {
  let changeData = localStorage.getItem('changeData')
  if (changeData !== 'null') {
    isAgree.value = JSON.parse(changeData)
  }
})

const onChange = (val) => {
  localStorage.setItem('changeData', val)
}

const onRegister = async () => {
  let params = {
    appId: _this.appid,
    phoneNumber: form.value.phoneNumber,
    verifyCode: form.value.verifyCode,
    password: form.value.password,
    nickname: form.value.nickname,
  }
  const res = await registerAppUser_api(params);
  if (res.code == 'Success') {
    MessagePlugin.success('注册成功');
    router.replace('/login');
  }
}

const onSubmit = ({ validateResult, firstError, e }) => {
  e.preventDefault();
  if (validateResult === true) {
    if (isAgree.value == false) {
      agreementGate(() => {
        isAgree.value = true;
        onChange(true);
        onRegister();
      });
      return;
    }
    onRegister();
  } else {
    MessagePlugin.warning(firstError);
  }
}



// 点击验证码按钮
const onClickCode = () => {
  if (isAgree.value == false) {
    agreementGate(() => {
      isAgree.value = true;
      onChange(true);
      onSendCode();
    });
    return;
  }
  onSendCode();
}
const onSendCode = () => {
  if (!form.value.phoneNumber) {
    MessagePlugin.error('请输入手机号');
    return;
  }
  setShow(true);
}

const onCloseCode = () => {
  setShow(false);
  console.log('关闭验证码', show.value)
}

// 验证码图形验证码成功， 可以发送手机验证码
const onSubmitCode = async (captchaToken) => {
  useCode();

  let headers = {
    'Captcha-Token': captchaToken
  };
  let params = {
    phoneNumber: form.value.phoneNumber
  }
  let res = await sendVerifyCodeSms_api(params, headers);
  if (res.code == 'Success') {
    setShow(false);
    MessagePlugin.success('发送验证码成功');
  }
}

// 验证码倒计时
const useCode = () => {
  setDisabled(true);
  useInterval(() => {
    let num = second.value - 1;
    setSecond(num);
    if (second.value == 0) {
      setDisabled(false);
      delay.value = null;
    }
  }, delay);
}

</script>

<template>
  <t-form class="formBox" ref="formRef" :data="form" :rules="rules" :colon="true" label-width="25px" @submit="onSubmit">
    <t-form-item name="phoneNumber">
      <t-input v-model="form.phoneNumber" clearable placeholder="请输入手机号">
        <template #prefix-icon>
          <CallIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-form-item name="verifyCode">
      <div class="codeBox">
        <t-input v-model="form.verifyCode" placeholder="请输入验证码">
          <template #prefix-icon>
            <LogoGithubIcon />
          </template>
        </t-input>
        <t-button style="width: 100px;" :disabled="disabled" @click="onClickCode">{{ disabled ? second : '发送验证码'
          }}</t-button>
      </div>
    </t-form-item>
    <t-form-item name="nickname">
      <t-input v-model="form.nickname" clearable placeholder="请输入昵称">
        <template #prefix-icon>
          <UserIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-form-item name="password">
      <t-input v-model="form.password" clearable placeholder="请输入登录密码">
        <template #prefix-icon>
          <LockOnIcon />
        </template>
      </t-input>
    </t-form-item>
    <t-checkbox v-model="isAgree" @change="onChange">
      <p style="font-size: xx-small">已阅读并同意
        <a style="color:#333;font-size: x-small" :href="userAgreement" target="_blank"
          rel="noopener noreferrer"><span>《用户协议》</span></a>&nbsp;
        <a style="color:#333" :href="privacyPolicy" target="_blank"
          rel="noopener noreferrer"><span>《隐私政策》</span></a>
      </p>
    </t-checkbox>
    <div class="empty"></div>
    <t-form-item>
      <t-button theme="primary" type="submit" block>注册</t-button>
    </t-form-item>
    <div class="tipBox">

    </div>
  </t-form>



  <GraphicValidation @confirm="onSubmitCode" @close="onCloseCode" :show="show"></GraphicValidation>
</template>


<style lang="scss" scoped>
.formBox {
  width: 89%;
  margin: auto;
  min-width: 250px;

  // min-width: 250px;
  .codeBox {
    width: 100%;
    display: flex;
  }

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
