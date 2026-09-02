<script setup>
import {
  ref, reactive, onMounted, onUnmounted,
  computed, nextTick,
} from 'vue';
import { useRouter } from 'vue-router';
import {
  MessagePlugin,
  LoadingPlugin,
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';
import { useUserStore } from '@/store/user';

import Dialog from '@/components/dialog';
import CutImage from '@/components/cutImage';

import {
  modifyMyAppUserInfo_api,
  modifyMyAccountAvatar_api,
  getMyAppUserInfo_api,
  getMyAccountPasswordStatus_api,
  modifyMyAccountPassword_api,
} from '@/api';
onMounted(() => {

});
const userStore = useUserStore();

const user = computed(() => userStore.userGetter);

let cutImageRef = ref(null); // 裁剪图片实例
const [visible, setVisible] = useState(false);
const [type, setType] = useState(null);



// 修改用户信息
const validatePhone = (val) => {
  console.log(val)
  if (val) {
    console.log(!/^1[34578]\d{9}$/.test(val))
    if (!/^1[34578]\d{9}$/.test(val)) {
      return false;
    }
    return true;
  } else {
    return true;
  }
}
const rulesUpdateInfo = {
  phoneNumber: [
    { required: true, message: '手机号必填', type: 'error' },
    { validator: validatePhone, message: '格式错误' },
  ],
  nickname: [
    { required: true, message: '昵称必填', type: 'error', trigger: 'blur' },
    { required: true, message: '昵称必填', type: 'error', trigger: 'change' },
    { whitespace: true, message: '昵称不能为空' },
  ]
};
let formUpdateInfoRef = ref(null);
const [form, setForm] = useState({
  nickname: null,
  phoneNumber: null,
});
/**
 * 修改用户信息
 * @param {String} type
 */
const updateInfo = (type) => {
  setForm({
    nickname: user.value.nickname,
    phoneNumber: user.value.phoneNumber,
  })
  setType(type);
  setVisible(true);
}

const onSubmitUpdate = async () => {
  let validate = await formUpdateInfoRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {};
      params[type.value] = form.value[type.value];
      // params['userId'] = user.value.userId;
      let res = await modifyMyAppUserInfo_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('修改成功');
        onCloseUpdate();
        getUser();
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

const onCloseUpdate = () => {
  setType(null);
  setVisible(false);
  setForm({
    nickname: null,
    phoneNumber: null,
  })
}


/**
 * 获取用户信息
 */
const getUser = async () => {
  let res = await getMyAppUserInfo_api();
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}


/**
 * 裁切头像
 */
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
 * 修改头像（CutImage confirm 直接回传裁切后的图片文件，二进制上传）
 * @param {File} data
 */
const getAvatarData = async (data) => {
  let headers = {
    'Content-Type': 'image/jpeg'
  }
  let res = await modifyMyAccountAvatar_api(data, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('修改成功');
    cutImageRef.value.close();
    setFlag(false);
    getUser();
  }
}



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
let formPasswordRef = ref(null);
let passwordStatus = ref(null);
const [visiblePassword, setVisiblePassword] = useState(false);
const [formPassword, setFormPassword] = useState({
  password: null, // 旧密码
  newPassword: null, // 新密码
  confirmNewPassword: null, // 确认新密码
});
const updatePassword = () => {
  setVisiblePassword(true);
  getMyAccountPasswordStatus();
  formPasswordRef.value.clearValidate()
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


onUnmounted(() => {

})
</script>


<template>
  <div class="info__wrapper">
    <main>
      <div class="item">
        <span>联系方式：</span>
        <span>{{ user.phoneNumber }}</span>
        <span @click="updateInfo('phoneNumber')" class="iconfont icon-bianji"></span>
      </div>
      <div class="item">
        <span>账号登录名：</span>
        <span>{{ user.accountUsername }}</span>
        <span></span>
      </div>
      <div class="item">
        <span style="float:left">头像：</span>
        <t-image overlayTrigger="hover" :lazy="true" fit="cover" error="加载失败"
          :src="user.accountAvatarUrl + '?v=' + Math.random()" style="width: 60px;height:60px;float:left">
          <template #overlayContent>
            <div @click="onUpdateAvatarUrl" class="coverBox">点击修改</div>
          </template>
        </t-image>
        <span></span>
      </div>
      <div class="item">
        <span>密码：</span>
        <span>******</span>
        <span @click="updatePassword('phoneNumber')" class="iconfont icon-bianji"></span>
      </div>
      <div class="item">
        <span>昵称：</span>
        <span>{{ user.nickname }}</span>
        <span @click="updateInfo('nickname')" class="iconfont icon-bianji"></span>
      </div>
      <div class="item"></div>
      <!-- <div class="item">
        <span>邮箱：</span>
        <span>{{ user?.accountEmail || '无' }}</span>
        <span></span>
      </div> -->
    </main>
  </div>

  <!-- 编辑 -->
  <Dialog @confirm="onSubmitUpdate" @close="onCloseUpdate" width="30%" :visible="visible">
    <template #title>修改{{ type == 'phoneNumber' ? '联系方式' : (type == 'nickname' ? '昵称' : '') }}</template>
    <t-form ref="formUpdateInfoRef" :rules="rulesUpdateInfo" :data="form" labelWidth="0">
      <t-form-item name="phoneNumber" v-if="type == 'phoneNumber'">
        <t-input placeholder="请输入联系方式" v-model="form.phoneNumber"></t-input>
      </t-form-item>
      <t-form-item name="nickname" v-if="type == 'nickname'">
        <t-input placeholder="请输入昵称" v-model="form.nickname"></t-input>
      </t-form-item>
      <div class="empty"></div>
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

  <!-- 裁剪图片 -->
  <CutImage v-if="flag" @close="closeAvatarData" :accountId="user.accountId" type="public" picType="avatar"
    @confirm="getAvatarData" ref="cutImageRef"></CutImage>
</template>

<style lang="scss" scoped>
.info__wrapper {
  main {
    width: 100%;
    background-color: var(--td-bg-color-container);
    color: var(--td-text-color-primary);
    padding: 20px 40px;
    box-sizing: border-box;
    display: flex;
    flex-wrap: wrap;

    .item {
      width: calc(100% / 3);
      margin-bottom: 20px;

      .iconfont {
        margin-left: 10px;
        cursor: pointer;
        opacity: 0.8;

        &:hover {
          opacity: 1;
        }
      }
    }
  }

  .coverBox {
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.7);
    color: #ededed;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
}
</style>
