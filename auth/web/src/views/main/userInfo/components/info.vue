<template>
  <div class="myInfo_info_wrapper">
    <t-form-item v-for="(item, index) in formItems" :key="index" :label="item.label">

      <t-row :gutter="16" style="width: 100%;">
        <t-col :span="4">
          <t-input v-model="item.formName" :disabled="item.status === 'readonly'"></t-input>
        </t-col>
        <t-col :span="6">
          <t-space>
            <t-button @click="onEdit(item, index)" v-if="item.status === 'readonly'" theme="default"
              variant="outline">修改</t-button>
            <t-button @click="onUpdate(item, index)" v-else>更新</t-button>
          </t-space>
        </t-col>
      </t-row>
    </t-form-item>

    <t-form-item label="角色">
      <div style="width:700px;display: flex;flex-wrap: wrap;" v-if="userInfos.roles">
        <t-tag v-for="(item, index) in userInfos.roles" :key="index" theme="primary" variant="light" style="margin: 10px;">{{
      item.roleName
    }}</t-tag>
      </div>
      <div class="empty"></div>
    </t-form-item>
    <t-form-item label="部门">
      <div style="width:700px;display: flex;flex-wrap: wrap;" v-if="userInfos.departments">
        <t-tag v-for="(item, index) in userInfos.departments" :key="index" theme="primary" variant="light" style="margin: 10px;">{{
      item.departmentNames.join('/')
    }}</t-tag>
      </div>
      <div class="empty"></div>
    </t-form-item>

    <t-form-item label="标签">
      <div style="width:700px;display: flex;flex-wrap: wrap;" v-if="userInfos.tags">
        <t-tag v-for="(item, index) in userInfos.tags" :key="index" theme="primary" variant="light" style="margin: 10px;">{{
      item.tagName
    }}</t-tag>
      </div>
      <div class="empty"></div>
    </t-form-item>


    <h6 class="UserInfo-title">用户注销</h6>
    <div class="my-content">
      <t-button @click="onLogOff" theme="default" variant="outline">注销</t-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import {
  DialogPlugin,
  MessagePlugin,
  LoadingPlugin,
} from 'tdesign-vue-next';
import { useCookie } from "v3hooks";
import {
  getMyAppUserInfo_api,
  modifyMyAppUserInfo_api,
  logoffMyAppUser_api,
  getMyAppUserPreLogoffInfo_api
} from '@/api';
// import useRemoveRoute from '@/hooks/useRemoveRoute';
import {
  setToken,
  setRefreshToken,
  setTokenType,
  setAuthType,
  setAppId,
  setEndpointId
} from '@/utils';
import { resetRouter } from '@/router';
import { useUserStore } from '@/store/user';
const userStore = useUserStore();
const userInfos = computed(() => userStore.userGetter);

onMounted(() => {
  getMyUserInfo()
})

const getMyUserInfo = async () => {
  let res = await getMyAppUserInfo_api({});
  if (res.code == 'Success') {
    userStore.saveUser(res?.data || {});
  }
}

const formItems = ref([
  { formName: userInfos.value.nickname, status: 'readonly', label: '昵称', attr: 'nickname' }, // edit
  { formName: userInfos.value.phoneNumber, status: 'readonly', label: '联系方式', attr: 'phoneNumber' }, // edit
]);

watch(userInfos, () => {
  formItems.value = [
    { formName: userInfos.value.nickname, status: 'readonly', label: '昵称', attr: 'nickname' }, // edit
    { formName: userInfos.value.phoneNumber, status: 'readonly', label: '联系方式', attr: 'phoneNumber' }, // edit
  ];
});


const onEdit = (row, index) => {
  formItems.value[index].status = 'edit';
}

const onUpdate = async (row, index) => {
  let params = {
    [formItems.value[index].attr]: formItems.value[index].formName
  };
  let res = await modifyMyAppUserInfo_api(params)
  if (res.code === 'Success') {
    MessagePlugin.success('修改成功');
    formItems.value[index].status = 'readonly';
    getMyUserInfo();
  }
}

const onLogOff = async () => {
  let res = await getMyAppUserPreLogoffInfo_api({})
  if (res.code === "Success") {
    const confirmDia = DialogPlugin({
      header: '提示',
      body: `你的账号[${res.data.nickname}]将在${res.data.day}天后(${res.data.logoffPendingTime})完成注销，注销时间不可提前`,
      confirmBtn: '确定',
      cancelBtn: '取消',
      onConfirm: async ({ e }) => {
        LoadingPlugin(true);
        try {
          let response = await logoffMyAppUser_api({});
          if (response.code == 'Success') {
            MessagePlugin.success('注销成功');
            goLoginOut();
          }
        } finally {
          LoadingPlugin(false);
        }
        confirmDia.hide();
      },
      onClose: ({ e, trigger }) => {
        confirmDia.hide();
      },
    });
  }

}

const goLoginOut = () => {
  resetRouter(); // 重置路由
  userStore.savePermissionList([]);
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
  window.location.reload();
}

</script>

<style lang="scss" scoped>
.myInfo_info_wrapper {
  box-sizing: border-box;
  padding: 10px 20px;
  width: 100%;

  .UserInfo-title {
    font-size: 20px;
    line-height: 60px;
  }

  .my-content {
    width: 100%;
    border: 1px solid #ededed;
    padding: 10px;
    box-sizing: border-box;
  }
}
</style>