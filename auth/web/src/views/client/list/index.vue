<!-- 客户端 -->
<script setup lang="jsx">
defineOptions({ name: 'manage-develop-client' })

import Vue3Jsoneditor from 'v3-jsoneditor/src/Vue3Jsoneditor.vue'
import { ref, reactive, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';

import {
  cloneDeep,
} from 'lodash';

import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';

import { useWindowSize } from '@vueuse/core';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, copyColumn, avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';

import useDict from '@/hooks/useDict';
import useState from '@/hooks/useState';


import {
  getClientPageList_api,
  getClientList_api,
  createClient_api,
  modifyClientInfo_api,
  modifyClientStatus_api,
  deleteClient_api,
  modifyClientSecret_api,

  getAppList_api,
  getEndpointList_api,
} from '@/api';

const allAuthorizationGrantTypes = ref([]);

onMounted(() => {
  getClientPage();

  nextTick(async () => {
    getAppList();
    getEndpointList();

    allAuthorizationGrantTypes.value = await useDict('AuthorizationGrantType');
  })
});

const { width } = useWindowSize(); // 监听窗口大小(Dialog 宽度自适应)
let page = ref(1);
let size = ref(10);
let total = ref(0);
let keyword = ref(null); // 关键字
let authorizationGrantTypes = ref([]);
let appId = ref(null); // 应用id
let endpointId = ref(null); // 终端id
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('clientId', '客户端ID'),
    { colKey: 'clientName', title: '客户端名称' },
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId' }),
    switchColumn({
      api: modifyClientStatus_api,
      idKeys: ['id', 'clientId'],
      label: '客户端',
      perm: 'client.modify_status',
      refresh: () => getClientPage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 140, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ?
                <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)}
                  hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)}
                  size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                  row?.metadata?.updateUser?.nickname ?
                    <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)}
                      size="medium">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center'
            }}>{row?.metadata?.updateUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('client.modify') },
      { content: '修改秘钥', onClick: (row) => onEditKey(row), visible: () => hasPermission('client.modify_client_secret') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('client.delete') },
    ], { width: 200 }),
  ],
  rowKey: 'id',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getClientPage();
  }
});
/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null);
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
const onCloseUserDetail = () => {
  setUserDetail({});
}
// 客户端 分页
const getClientPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      authorizationGrantTypes: authorizationGrantTypes.value,
      appId: appId.value,
      endpointId: endpointId.value,
    }
    let res = await getClientPageList_api(params);
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
  getClientPage();
}
const onReset = () => {
  page.value = 1;
  keyword.value = null;
  authorizationGrantTypes.value = []
  appId.value = null;
  endpointId.value = null;
  getClientPage();
}


/**
 * 创建
 */
let editorRef = ref(null);
let [content, setContent] = useState({});
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const onCreate = () => {
  setType('add');
  setVisible(true);
  setContent({
    "appId": "test1",
    "endpointId": "web1",
    "clientId": "test1_web1_client1",
    "clientSecret": "test1_web1_client1",
    "clientName": "test1_web1_client1",
    "clientAuthenticationMethods": [
      "client_secret_basic",
      "client_secret_post",
      "client_secret_jwt",
      "private_key_jwt"
    ],
    "authorizationGrantTypes": [
      "authorization_code",
      "refresh_token",
      "account:password",
      "account:verify_code",
      "user:password",
      "user:verify_code",
      "user:connect",
      "user:account_access_token",
      "endpoint_user:password",
      "endpoint_user:verify_code",
      "endpoint_user:connect"
    ],
    "scopes": [
      "sms:send",
      "file:upload_temporary",
      "file:upload_tenant",
      "file:upload_public",
      "dict:read"
    ],
    "redirectUris": [
      "http://localhost",
      "http://localhost/"
    ],
    "clientSettings": {
      "requireProofKey": false,
      "requireUserConsent": false,
      "jwkSetUrl": "http://127.0.0.1:10010/oauth2/jwks",
      "tokenEndpointAuthenticationSigningAlgorithm": "RS256"
    },
    "tokenSettings": {
      "accessTokenTimeToLive": "PT24H",
      "reuseRefreshTokens": true,
      "refreshTokenTimeToLive": "PT720H",
      "idTokenSignatureAlgorithm": "RS256"
    }
  })
}
const onSubmit = async () => {
  let validateArr = await editorRef.value.editor.validate();
  if (!validateArr.length) {
    LoadingPlugin(true);
    try {
      if (type.value == 'add') {
        let res = await createClient_api(content.value);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getClientPage();
        }
      } else {
        let res = await modifyClientInfo_api(content.value);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          onClose();
          getClientPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  } else {
    MessagePlugin.error('json格式不正确');
  }
}

const onClose = () => {
  setVisible(false);
  setType('add');
}


// 编辑
const onEdit = (row) => {
  setType('edit');
  setContent(row);
  setVisible(true);
}


/**
 * 删除
 */
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
          id: row.id,
          clientId: row.clientId,
        }
        let res = await deleteClient_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getClientPage();
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


/**
 ************************************************************ 修改秘钥 ***************************************************
 */
const [visibleEditKey, setVisibleEditKey] = useState(false);
const [formKey, setFormKey] = useState({
  id: null,
  clientSecret: null,
  clientId: null,
})
/**
 * @param {*} row
 */
const onEditKey = (row) => {
  setVisibleEditKey(true);
  setFormKey({
    id: row.id,
    clientId: row.clientId,
  })
}
const onCloseEditKey = () => {
  setVisibleEditKey(false);
  setFormKey({
    id: null,
    clientSecret: null,
    clientId: null,
  })
}
const onSubmitEditKey = async () => {
  let params = {
    ...formKey.value,
  };
  let res = await modifyClientSecret_api(params);
  if (res.code == 'Success') {
    MessagePlugin.success('修改成功');
    onCloseEditKey();
  }
}


// 应用 列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let res = await getAppList_api({});
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}

watch(appId, () => {
  getEndpointList();
})

// 终端列表
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: appId.value,
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}
</script>


<template>
  <div v-allow="'client.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="认证授权类型">
        <t-select multiple clearable filterable v-model="authorizationGrantTypes" placeholder="请选择认证授权类型">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in allAuthorizationGrantTypes"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="appId" placeholder="请选择应用">
          <t-option :style="{ height: '40px', width: '100%' }" :label="item.appName" :value="item.appId"
            v-for="(item, index) in appList" :key="index">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="appList.filter(item => item.appId == value)[0]?.icon" shape="round"></t-avatar>
                {{ appList.filter(item => item.appId == value)[0]?.appName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="终端">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="endpointId" placeholder="请选择终端">
          <t-option :style="{ height: '40px', width: '100%' }" :label="item.endpointName"
            :value="item.endpointId" v-for="(item, index) in endpointList" :key="index">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="endpointList.filter(item => item.endpointId == value)[0]?.icon"
                  shape="round"></t-avatar>
                {{ endpointList.filter(item => item.endpointId == value)[0]?.endpointName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="关键字">
        <t-input clearable v-model="keyword" placeholder="请输入关键字"></t-input>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'client.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>

    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <Dialog :width="width < 750 ? '100%' : '70%'" top="20" @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <Vue3Jsoneditor height="70vh" ref="editorRef" v-model="content"></Vue3Jsoneditor>
    <!-- <Editor :content="content" ref="editorRef" v-if="visible"></Editor> -->
  </Dialog>


  <!-- 修改秘钥 -->
  <Dialog @confirm="onSubmitEditKey" @close="onCloseEditKey" :visible="visibleEditKey">
    <template #title>修改秘钥</template>
    <t-textarea v-model="formKey.clientSecret" :autosize="{ minRows: 2, maxRows: 10 }" placeholder="请输入内容" />
  </Dialog>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.list__wrapper {}
</style>
