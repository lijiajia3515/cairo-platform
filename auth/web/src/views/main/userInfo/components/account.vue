<template>
  <div class="account_wrapper">
    <div v-if="!flag">
      <t-form-item label="登录用户名">
        <t-row style="width: 100%;">
          <t-col :span="3">
            <t-input :disabled="true" :value="userInfos?.accountUsername"></t-input>
          </t-col>
          <t-col :span="2">
            <t-button @click="updateUsername" theme="default" variant="base" style="margin-left:10px;">修改用户名</t-button>
          </t-col>
        </t-row>
      </t-form-item>
      <t-form-item label="登录手机号">
        <t-row style="width: 100%;">
          <t-col :span="3">
            <t-input :disabled="true" :value="userInfos?.accountPhoneNumber"></t-input>
          </t-col>
          <t-col :span="2">
            <t-button @click="updatePhone" theme="default" variant="base" style="margin-left:10px;">修改手机号</t-button>
          </t-col>
        </t-row>
      </t-form-item>
      <t-form-item label="登录密码">
        <t-row style="width: 100%;">
          <t-col :span="3">
            <t-input :disabled="true" value="********"></t-input>
          </t-col>
          <t-col :span="2">
            <t-button @click="updatePassword" theme="default" variant="base" style="margin-left:10px;">修改密码</t-button>
          </t-col>
        </t-row>
      </t-form-item>

      <div style="width:60%;margin-bottom: 20px">
        <div style="margin-bottom: 10px">
          <h6 style="font-size: 18px;line-height: 30px;">第三方账号绑定</h6>
          <div class="empty"></div>
          <p>使用以下任一方式都可以登录到您的Cairo账号，避免由于某个帐号失效导致无法登录</p>
        </div>
        <div>
          <t-table table-layout="auto" row-key="index" size="small" :data="snsProviderList" :columns="columns" bordered>
            <template #operation="{ row }">

              <t-button variant="outline" theme="warning" ghost @click='handleClick(row)'>解除绑定</t-button>
            </template>
          </t-table>
        </div>
        <div class="empty"></div>
        <div v-if="unBindSnsProviderList.length > 0">
          <p style="margin-bottom: 10px">你还可以绑定以下第三方账号</p>
          <div style="display:flex;align-items:center;" @click="() => { flag = true }">
            <div style="display:flex;flex-direction: column;align-items:center;margin-right:10px"
              v-for="( item, index ) in  unBindSnsProviderList " :key="index">
              <img :src="item.snsPartnerIcon" alt="" style="width: 30px;height: 30px;">
              <span>{{ item.snsPartnerName }}</span>
            </div>
            <t-icon name="chevron-right" size="40px" />
          </div>
        </div>
      </div>

      <h6 class="UserInfo-title">账号注销</h6>
      <div class="my-content">
        <t-button :disabled="true" theme="default" variant="outline">注销</t-button>
      </div>
    </div>

    <div v-if="flag">
      <div style="display:flex;align-items:center;">
        <t-icon name="rollback" size="25px" @click="() => { flag = false }" />
        <h6 style="font-size: 18px;line-height: 30px;margin-left: 10px;">第三方账号</h6>
      </div>
      <t-list :split="true" v-for="( item, index ) in unBindSnsProviderList " :key="index">
        <t-list-item
          style="width: 60%;display:flex;align-items:center;justify-content: space-between;padding: 15px;box-sizing: border-box;">
          <div style="display:flex;align-items:center;">
            <img :src="item.snsPartnerIcon" alt="" style="width: 40px;height: 40px;margin-right:20px;">
            <!-- <span>{{ item.snsProviderName }}</span> -->
          </div>
          <div>
            <!-- <span style="margin-right:20px;">{{ item.bindTime }}</span> -->
            <t-button theme="default" variant="outline" @click='handleBind(item)'>绑定</t-button>
          </div>

        </t-list-item>
      </t-list>
    </div>

    <t-dialog v-model:visible="bindSureVisible" :cancelBtn="null" :confirmBtn="null" :closeBtn="false"
      :closeOnOverlayClick="false" width="22%" @close="handleClose">
      <div style="padding:0 20px;box-sizing: border-box;">
        <div style="display:flex;flex-direction: column;align-items: center;justify-content: center;">
          <t-avatar size="100px" :image="bindInfo.avatarUrl" style="margin-bottom: 10px"> </t-avatar>
          <p style="margin-bottom: 10px">{{ bindInfo.nickName }}</p>
          <p>您可以用微信快捷登录Cairo账号</p>
        </div>
        <div class="empty"></div>
        <t-button block theme="primary" variant="base" size="large" @click="getBindAccount">确认关联Cairo账号</t-button>
        <div class="empty"></div>
        <t-button block variant="outline" size="large" @click="handleClose">取消</t-button>
      </div>
    </t-dialog>


  </div>

  <!-- 修改用户名 -->
  <Dialog @confirm="onConfirmUsername" @close="onCloseUsername" :visible="visibleUsername">
    <template #title>修改用户名</template>
    <t-form ref="formUsernameRef" :rules="rulesUsername" :data="formUsername">
      <t-form-item name="username" label="用户名">
        <t-input v-model="formUsername.username" placeholder="请输入用户名"></t-input>
      </t-form-item>
    </t-form>
  </Dialog>

  <!-- 修改密码 -->
  <Dialog @confirm="onConfirmPassword" @close="onClosePassword" :visible="visiblePassword">
    <template #title>修改密码</template>
    <t-form ref="formPasswordRef" :rules="rulesPassword" :data="formPassword">
      <t-form-item name="password" v-if="passwordStatus !== false && passwordStatus !== null" label="原密码">
        <t-input v-model="formPassword.password" type="password" placeholder="请输入原密码"></t-input>
      </t-form-item>
      <t-form-item name="newPassword" label="新密码">
        <t-input v-model="formPassword.newPassword" type="password" placeholder="请输入新密码"></t-input>
      </t-form-item>
      <t-form-item name="confirmNewPassword" label="确认密码">
        <t-input v-model="formPassword.confirmNewPassword" type="password" placeholder="请输入确认密码"></t-input>
      </t-form-item>
      <div class="empty"></div>
    </t-form>
  </Dialog>

  <!-- 修改登录手机号 -->
  <Dialog @confirm="onConfirmPhone" @close="onClosePhone" :visible="visiblePhone">
    <template #title>修改登录手机号</template>
    <t-form ref="phoneFormRef" :rules="rulesPhone" :data="phoneForm" :labelWidth="25">
      <template v-if="userInfos?.accountPhoneNumber">
        <t-form-item :labelWidth="25">
          <t-input :defaultValue="userInfos?.accountPhoneNumber" readonly label="原手机号："></t-input>
        </t-form-item>
        <t-form-item name="sourceVerifyCode">
          <div style="width: 100%;display: flex;">
            <t-input v-model="phoneForm.sourceVerifyCode" placeholder="请输入验证码"></t-input>
            <t-button :disabled="oldSecond < 60" @click="onSendMyPhoneCode">{{ oldSecond == 60 ? "发送验证码" : oldSecond
              }}</t-button>
          </div>
        </t-form-item>
      </template>
      <t-form-item name="phoneNumber">
        <t-input v-model="phoneForm.phoneNumber" placeholder="请输入新手机号"></t-input>
      </t-form-item>
      <t-form-item name="verifyCode">
        <div style="width: 100%;display: flex;">
          <t-input v-model="phoneForm.verifyCode" placeholder="请输入验证码"></t-input>
          <t-button :disabled="!phoneForm.phoneNumber || phoneForm.phoneNumber.length != 11 || second < 60"
            @click="onGetNewCode">{{
            second == 60 ? "发送验证码" : second
            }}</t-button>
        </div>
      </t-form-item>
    </t-form>
    <div class="empty"></div>
  </Dialog>

  <!-- 图形验证码 -->
  <GraphicValidation @confirm="onSubmitCode" @close="onCloseCode" :show="show"></GraphicValidation>
  <!-- 原手机验证码 -->
  <GraphicValidation @confirm="onSubmitOldCode" @close="onCloseOldCode" :show="showOld"></GraphicValidation>
</template>

<script setup lang="jsx">
import { ref, onMounted, computed, nextTick } from 'vue';

import {
  MessagePlugin
} from 'tdesign-vue-next';

import { useUserStore } from '@/store/user';

import useState from '@/hooks/useState';

import GraphicValidation from "@/components/graphicValidation";
import { randomString } from '@/utils/tips';
import {
  getMyAppUserInfo_api,
  getMyAccountPasswordStatus_api,
  modifyMyAccountPassword_api,
  sendVerifyCodeSms_api,
  modifyMyAccountPhoneNumber_api,
  sendMyAccountPhoneNumberVerifyCode_api,
  modifyMyAccountUsername_api,
  getMyAccountSnsList_api,
  getSnsToken_api,
  unbindAccountSns_api,
  bindAccountSns_api
} from '@/api';

const userStore = useUserStore();

const userInfos = computed(() => userStore.userGetter);

const [snsProviderList] = useState([])
const [unBindSnsProviderList] = useState([])
const [flag] = useState(false)
const [bindInfo, setBindInfo] = useState({})
const [bindSureVisible, setBindSureVisible] = useState(false)
const [visibleUsername, setVisibleUsername] = useState(false)


const columns = ref([
  { colKey: 'serial-number', title: '序号', width: 80, },

  { colKey: 'snsPartnerName', title: '绑定账号信息', ellipsis: true, minWidth: 140 },
  {
    colKey: 'detail', title: '详情', minWidth: 140, cell: (h, { row }) => {
      return (
        <t-space size="small">
          {
            row?.avatarUrl ? <t-avatar imageProps={{ lazy: true }} hideOnLoadFailed={true} alt={row?.nickname?.slice(0, 2)} size="16px" image={row?.avatarUrl} /> : null
          }
          <div style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.nickname || null}</div>
        </t-space>
      )
    }
  },
  { colKey: 'bindTime', title: '绑定时间', ellipsis: true, minWidth: 170 },
  {
    colKey: 'isBind',
    title: '状态',
    minWidth: 90,
    cell: (h, { row }) => {
      return (
        <div>
          {
            row['isBind'] == true ? <span>使用中</span> : row['isBind'] == false ? <span>未绑定</span> : null
          }
        </div>
      );
    },
  },
  { colKey: 'operation', title: '操作', fixed: 'right', width: 120 },
]);


// 修改密码
const rePassword = (val) => {
  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      resolve(formPassword.value.newPassword === val);
      clearTimeout(timer);
    });
  });
}
const rulesPassword = {
  password: [
    { required: true, message: '原密码必填', type: 'error', trigger: 'blur' },
    { required: true, message: '原密码必填', type: 'error', trigger: 'change' },
  ],
  newPassword: [
    { required: true, message: '新密码必填', type: 'error', trigger: 'blur' },
    { required: true, message: '新密码必填', type: 'error', trigger: 'change' },
  ],
  confirmNewPassword: [
    { required: true, message: '确认密码必填', type: 'error', trigger: 'blur' },
    { required: true, message: '确认密码必填', type: 'error', trigger: 'change' },
    { validator: rePassword, message: '两次密码不一致' },
  ]
};
const rulesUsername = {
  username: [
    { required: true, message: '用户名必填', type: 'error', trigger: 'blur' },
  ],
};
let formPasswordRef = ref(null);
let formUsernameRef = ref(null);
let passwordStatus = ref(null);
const [visiblePassword, setVisiblePassword] = useState(false);
const [formPassword, setFormPassword] = useState({
  password: null, // 旧密码
  newPassword: null, // 新密码
  confirmNewPassword: null, // 确认新密码
});
const [formUsername, setFormUsername] = useState({
  username: null, // 旧密码
});
const updatePassword = () => {
  setVisiblePassword(true);
  getMyAccountPasswordStatus();
  formPasswordRef.value.clearValidate()
}

let sns_provider_id = ref('')
let sns_provider_access_key = ref('')
let snsToken = ref('')
const params = new URLSearchParams(window.location.search);
let code = localStorage.getItem('wxCode') || params.get('code')
let snsProviderId = localStorage.getItem('snsProviderId')
onMounted(() => {
  console.log(code, 'code.value===');
  if (code) {
    getSnsToken()
  }
  getSnsProviderList();
})

const getSnsToken = async () => {
  try {
    let params = {
      snsType: 'wx_web',
      snsProviderId: snsProviderId,
      snsCode: code
    }
    let res = await getSnsToken_api(params)
    if (res.code == "Success") {
      snsToken.value = res.data.token
      setBindInfo(res.data)
      setBindSureVisible(true)
      // getBindAccount(snsToken.value)
    }
  } finally {
    localStorage.removeItem('wxCode')
    localStorage.removeItem('infoType')
  }
}

const getBindAccount = async () => {
  let params = {
    snsType: 'wx_web',
    snsProviderId: snsProviderId,
    snsToken: snsToken.value
  }
  let res = await bindAccountSns_api(params)
  if (res.code === "Success") {
    MessagePlugin.success('绑定账号成功');
    getSnsProviderList();
    handleClose()


  }
}

const handleClose = () => {
  setBindSureVisible(false)
  setBindInfo({})
  const newUrl = window.location.origin + window.location.pathname;
  history.replaceState({}, '', newUrl);
}

const updateUsername = ()=>{
  setVisibleUsername(true);
}

const onConfirmUsername = async () => {
  let validate = await formUsernameRef.value.validate();
  if (validate == true) {
    let params = {
      username: formUsername.value.username,
    };
    let res = await modifyMyAccountUsername_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success('修改用户名成功');
      getMyUserInfo();
      onCloseUsername();
    }
  }
}

const onCloseUsername = () => {
  setVisibleUsername(false);
  setFormUsername({
    username: null,
  })
}

const onConfirmPassword = async () => {
  let validate = await formPasswordRef.value.validate();
  if (validate == true) {
    let params = {
      password: formPassword.value.password,
      newPassword: formPassword.value.newPassword,
    };
    let res = await modifyMyAccountPassword_api(params);
    if (res.code == 'Success') {
      onClosePassword();
      MessagePlugin.success('修改密码成功');
    }
  }
}
// 获取当前账号密码状态  判断密码存不存在
const getMyAccountPasswordStatus = async () => {
  let res = await getMyAccountPasswordStatus_api({});
  if (res.code == 'Success') {
    passwordStatus.value = res?.data || null;
  }
}

const onClosePassword = () => {
  setVisiblePassword(false);
  setFormPassword({
    password: null, // 旧密码
    newPassword: null, // 新密码
    confirmNewPassword: null, // 确认新密码
  })
}

// 修改手机号
const rulesPhone = {
  sourceVerifyCode: [
    { required: true, message: '请输入原手机验证码', type: 'error', trigger: 'change' },
  ],
  phoneNumber: [
    { required: true, message: '请输入新手机号', type: 'error', trigger: 'change' },
  ],
  verifyCode: [
    { required: true, message: '请输入新手机号验证码', type: 'error', trigger: 'change' },
  ],
};
let phoneFormRef = ref(null);
const [visiblePhone, setVisiblePhone] = useState(false);
const [phoneForm, setPhoneForm] = useState({
  sourceVerifyCode: null,
  phoneNumber: null,
  verifyCode: null,
})
const updatePhone = () => {
  setVisiblePhone(true);
}

const onConfirmPhone = async () => {
  let validate = await phoneFormRef.value.validate();
  if (validate == true) {
    let params = {
      phoneNumber: phoneForm.value.phoneNumber,
      verifyCode: phoneForm.value.verifyCode,
    };
    if (userInfos?.value.accountPhoneNumber) {
      params['sourceVerifyCode'] = phoneForm.value.sourceVerifyCode;
    } else {
      params['sourceVerifyCode'] = null;
    }
    let res = await modifyMyAccountPhoneNumber_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success("修改登录手机号成功");
      getMyUserInfo();
      onClosePhone();
    }
  }
}

const onClosePhone = () => {
  setVisiblePhone(false);
  setPhoneForm({
    sourceVerifyCode: null,
    phoneNumber: null,
    verifyCode: null,
  })
}

// 图形验证码
const [show, setShow] = useState(false);
const onGetNewCode = () => {
  if (!phoneForm.value.phoneNumber) {
    MessagePlugin.error("请输入手机号");
    return;
  }
  let checkRule = /^1[3456789]\d{9}$/;
  let flag = checkRule.test(phoneForm.value.phoneNumber);
  if (flag) {
    setShow(true);
  } else {
    MessagePlugin.error("手机号格式错误");
  }
}
/**
 * 验证图形验证码成功
 * @param {String} captchaToken
 */
const onSubmitCode = (captchaToken) => {
  sendVerifyCode(captchaToken);
  setShow(false);
};

const onCloseCode = () => {
  setShow(false);
};
/**
 * 发送新手机验证码
 * @param {String} captchaToken
 */
const [second, setSecond] = useState(60);
const sendVerifyCode = async (captchaToken) => {
  let params = {
    phoneNumber: phoneForm.value.phoneNumber,
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

// 获取用户信息 用户级别
const getMyUserInfo = async () => {
  let res = await getMyAppUserInfo_api({});
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}

// 图形验证码
const [showOld, setShowOld] = useState(false);
// 发送原手机验证码
const onSendMyPhoneCode = () => {
  setShowOld(true);
}

/**
 * 验证图形验证码成功
 * @param {String} captchaToken
 */
const onSubmitOldCode = (captchaToken) => {
  sendOldVerifyCode(captchaToken);
  setShowOld(false);
};

const onCloseOldCode = () => {
  setShowOld(false);
};
/**
 * 发送新手机验证码
 * @param {String} captchaToken
 */
const [oldSecond, setOldSecond] = useState(60);
const sendOldVerifyCode = async (captchaToken) => {
  let headers = {
    "Captcha-Token": captchaToken,
  };
  let res = await sendMyAccountPhoneNumberVerifyCode_api({}, headers);
  if (res.code == "Success") {
    MessagePlugin.success("发送验证码成功");
    let timer = setInterval(() => {
      let num = oldSecond.value - 1;
      setOldSecond(num);
      if (oldSecond.value == 0) {
        setOldSecond(60);
        clearInterval(timer);
      }
    }, 1000);
  }
};


const getSnsProviderList = async () => {
  let res = await getMyAccountSnsList_api({ snsTypes: ['wx_web'] })
  if (res.code === 'Success') {
    if (res.data && res.data.length > 0) {
      snsProviderList.value = res.data.filter(item => item.isBind == true) || []
      unBindSnsProviderList.value = res.data.filter(item => item.isBind == false) || []
    }
  }
}
let getCode = () => {
  let local = encodeURIComponent(window.location.href + '?sns_provider_id=' + sns_provider_id.value + '&type=account'); //获取当前页面地址作为回调地址
  let path = encodeURIComponent(_this.callbackUrl + local);
  console.log(path, 'path====');
  window.location.href =
    "https://open.weixin.qq.com/connect/qrconnect?"
    + "appid=" + sns_provider_access_key.value
    + "&redirect_uri=" + path
    + "&response_type=code&scope=snsapi_login&state=" + randomString(6) + "#wechat_redirect";
}

const handleClick = async (val) => {

  const confirmDia = DialogPlugin({
    header: '解绑',
    body: '是否继续解绑?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      let params = {
        snsPartnerId: val.snsPartnerId,
      }
      let res = await unbindAccountSns_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('解绑成功');
        confirmDia.hide();
        getSnsProviderList();
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}

const handleBind = (val) => {
  sns_provider_id.value = val.snsProviderId
  sns_provider_access_key.value = val.clientId
  nextTick(() => {
    getCode()
  })
}


</script>

<style lang="scss" scoped>
.account_wrapper {
  box-sizing: border-box;
  padding: 10px 20px;

  .UserInfo-title {
    font-size: 20px;
    line-height: 60px;
  }

  .my-content {
    width: 60%;
    border: 1px solid var(--td-component-stroke);
    border-radius: 5px;
    padding: 10px;
    box-sizing: border-box;
  }
}
</style>