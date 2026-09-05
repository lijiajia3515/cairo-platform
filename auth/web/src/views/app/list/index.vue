<script setup lang="jsx">
defineOptions({ name: 'manage-develop-app' })

import { ref, reactive, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useWindowSize } from '@vueuse/core';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, copyColumn, avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';


import {
  getAppPageList_api,
  createApp_api,
  modifyAppInfo_api,
  modifyAppStatus_api,
  deleteApp_api,
  getAccountList_api,
} from '@/api';
import Transfer from "@/components/transfer/index.js";
import useDict from '@/hooks/useDict';

const endpointScopes = ref([]);

onMounted(() => {
  getAppPage();
  nextTick(async () => {
    endpointScopes.value = await useDict('AccessScope');
  })
});

const { width } = useWindowSize(); // 监听窗口大小(Dialog 宽度自适应)


let keyword = ref(null); // 关键字(应用名称)
let enabled = ref(null); // 状态
let scopes = ref([]) // 范围
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(true);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('appId', '应用ID'),
    avatarCopyColumn({ colKey: 'appName', title: '应用名称', iconKey: 'icon' }),
    {
      colKey: 'privateApp', title: '是否内部应用', width: 110, cell: (h, { row }) => {

        return row['privateApp'] == true ? '是' : (row['privateApp'] == false ? '否' : '');
      }
    },
    {
      colKey: 'scopes', title: '范围', width: 100, cell: (h, { row }) => {
        let arr = []
        endpointScopes.value?.forEach(item => {
          if (row?.scopes && row?.scopes.length > 0) {
            row?.scopes.forEach(val => {
              if (item.itemId === val) {
                arr.push(item.itemName)
              }
            })
          }
        });
        return arr.join('、')
      }
    },
    switchColumn({
      api: modifyAppStatus_api,
      idKeys: ['appId'],
      label: '应用',
      perm: 'app.modify_status',
      refresh: () => getAppPage(),
    }),
    {
      colKey: 'adminAccounts', title: '管理员账号', width: 100, ellipsis: true, cell: (h, { row }) => {
        return (
          <t-space size="small" >
            {
              row['adminAccounts'] ? row['adminAccounts'].map(item => {
                return (
                  <t-space size="small">
                    {
                      item?.avatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchAccountInfo(item)} hideOnLoadFailed={true} size="medium" image={item.avatarUrl || null} /> : (
                        item.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchAccountInfo(item)} size="medium" >{item.nickname?.slice(0, 2)}</t-avatar> : null
                      )
                    }
                    <div onClick={() => onWatchAccountInfo(item || {})} style={{ height: '100%', display: 'flex', alignItems: 'center', cursor: 'pointer' }}>{item?.nickname || null}</div>
                  </t-space>
                )
              }) : ''
            }
          </t-space>
        )
      }
    },
    { colKey: 'autoRegister', title: '开通自动注册', width: 120, cell: (h, { row }) => row['autoRegister'] == true ? '是' : (row['autoRegister'] == false ? '否' : '') },
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 140, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                row?.metadata?.updateUser?.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} size="medium" >{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('app.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('app.delete') },
    ], { width: 160 }),
  ],
  rowKey: 'appId',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getAppPage();
  }
});

/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null); // 用户详情
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
// 管理员账号点击查看用户详情(账号字段名与用户详情字段对齐)
const onWatchAccountInfo = (item) => {
  userInfoRef.value.open();
  setUserDetail({
    userId: item?.accountId,
    nickname: item?.nickname,
    accountAvatarUrl: item?.avatarUrl,
    joinTime: item?.joinTime,
  })
}
const onCloseUserDetail = () => {
  setUserDetail({});
}

/**
 ******************************************************* 应用列表
 */
const getAppPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      enabled: enabled.value,
      scopes: scopes.value
    }
    let res = await getAppPageList_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getAppPage();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  enabled.value = null;
  scopes.value = []
  getAppPage();
}

/**
 * 账号列表
 */
const [accountList, setAccountList] = useState([]);
const getAccountList = async () => {
  let res = await getAccountList_api({});
  if (res.code == 'Success') {
    setAccountList(res?.data || []);
  }
}


/**
 * 创建
 */
const rules = {
  appId: [
    { required: true, message: '应用id必填', type: 'error', trigger: 'change' },
  ],
  appName: [
    { required: true, message: '应用名称必填', type: 'error', trigger: 'change' },
  ],
  scopes: [
    { required: true, message: '应用名称必填', type: 'error', trigger: 'change' },
  ]
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  appId: null,
  appName: null,
  scopes: [],
  privateApp: null,
})
const onCreate = () => {
  getAccountList();
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}

const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      if (type.value == 'add') {
        let params = {
          appId: form.value.appId,
          appName: form.value.appName,
          scopes: form.value.scopes,
          privateApp: form.value.privateApp,
          adminAccountIds: form.value.adminAccountIds,
          autoRegister: form.value.autoRegister,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await createApp_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getAppPage();
        }
      } else {
        let params = {
          appId: form.value.appId,
          appName: form.value.appName,
          scopes: form.value.scopes,
          privateApp: form.value.privateApp,
          adminAccountIds: form.value.adminAccountIds,
          autoRegister: form.value.autoRegister,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifyAppInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('修改成功');
          onClose();
          getAppPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

const onClose = () => {
  setType('add');
  setVisible(false);
  setEditFileList([]);
  setForm({
    appId: null,
    appName: null,
    adminAccountIds: [],
  })
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}



/**
 * 编辑
 */
const onEdit = async (row) => {
  getAccountList();
  setType('edit');
  setVisible(true);
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
  }
  let adminAccounts = [];
  if (row.adminAccounts && row.adminAccounts.length) {
    row.adminAccounts.forEach(item => {
      adminAccounts.push(item.accountId);
    })
  }
  setForm({
    ...form.value,
    appId: row.appId,
    appName: row.appName,
    scopes: row.scopes,
    privateApp: row.privateApp,
    adminAccountIds: adminAccounts,
    autoRegister: row.autoRegister,
  });
  formRef.value.clearValidate();
}

// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          appId: row.appId,
        }
        let res = await deleteApp_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getAppPage();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}
</script>


<template>
  <div v-allow="'app.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用名称">
        <t-input clearable placeholder="请输入应用名称" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="enabled">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="范围">
        <t-select multiple filterable v-model="scopes">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointScopes"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'app.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>

    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <!-- 添加 编辑 -->
  <Dialog :width="width < 750 ? '100%' : '45%'" @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="appId" label="应用ID">
            <t-input :disabled="type == 'edit'" v-model="form.appId"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="appName" label="应用名称">
            <t-input v-model="form.appName"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="scopes" label="范围">
            <t-select multiple filterable v-model="form.scopes">
              <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointScopes"
                :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="是否内部应用">
            <t-radio-group v-model="form.privateApp">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="11">
          <t-form-item label="管理员账号">
            <Transfer v-model="form.adminAccountIds" :list="accountList" :actives="form.adminAccountIds"
              v-if="accountList.length"></Transfer>
            <!-- <t-select :scroll="{ type: 'virtual' }" multiple v-model="form.adminAccountIds">
              <t-option :label="item.nickname" :value="item.accountId" v-for="(item, index) in accountList"
                :key="index"></t-option>
            </t-select> -->
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="11">
          <t-form-item label="开启自动注册">
            <t-radio-group v-model="form.autoRegister">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="图标">
            <UploadImage :disabled="!form.appId" :appId="form.appId" type="public" picType="app-icon"
              @change="onChangeFiles" :limit="1" :fileList="editFileList">
            </UploadImage>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>


  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.list__wrapper {}
</style>
