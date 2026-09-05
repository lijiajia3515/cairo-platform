<script setup lang="jsx">
defineOptions({ name: 'manage-account-session' })

import {
  ref,
  onMounted,
  watch,
  nextTick,
} from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, ellipsisColumn, copyColumn, opColumn, userColumn } from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';

import {
  getAccountAuthorizationPageList_api,
  getAppList_api,
  getClientList_api,
  getAccountList_api,
  offlineAccountAuthorization_api,
  offlineAllAccountAuthorization_api
} from '@/api';
onMounted(() => {
  getAccountSessionList()
  getAppList();
  nextTick(() => {
    getAccountList();
  })
});


let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
let loading = ref(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('tokenId', '会话ID', {width: 190}),
    { colKey: 'clientName', title: '客户端' },
    userColumn({ colKey: 'accountName', title: '账号', nameKey: 'accountName', avatarKey: 'avatarUrl', idKey: 'accountName', width: 140 }),
    { colKey: 'loginType', title: '登录方式' },
    { colKey: 'ip', title: 'IP' },
    { colKey: 'region', title: '地区' },
    ellipsisColumn('agent', 'Agent', {width: 220}),
    { colKey: 'os', title: '系统' },
    { colKey: 'app', title: '应用' },
    timeColumn('accessTokenExpiresAt', '访问令牌过期时间'),
    timeColumn('refreshTokenExpiresAt', '刷新令牌过期时间'),
    {
      colKey: 'status', title: '状态', cell: (h, { row }) => {
        let statusList = {
          'ok': '使用中',
          'expired': '已过期',
          'blacklist': '下线',
          'logout': '登出'
        }
        return statusList[row?.status]

      }
    },
    timeColumn('loginTime', '登录时间'),
    timeColumn('logoutTime', '下线时间'),
    {
      colKey: 'onlineDuration', title: '在线时长(h)', cell: (h, { row }) => {
        return (
          (row?.onlineDuration / 3600).toFixed(2)
        )
      }
    },
    opColumn([
      {content: '下线', onClick: (row) => onOffline(row), visible: () => hasPermission('account_authorization.offline')},
    ], {width: 100})
  ],
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getAccountSessionList();
  }
});

const [search, setSearch] = useState({
  keyword: null,
  status: null,
  accountId: null,
});

// 账号会话列表
const getAccountSessionList = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      clientId: headerData.value.clientId,
      keyword: search.value.keyword,
      status: search.value.status,
      accountId: search.value.accountId,
    };
    let res = await getAccountAuthorizationPageList_api(params);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } catch (err) {
    console.log(err)
  }
  finally {
    loading.value = false;
  }
}

const onSearch = () => {
  page.value = 1;
  getAccountSessionList();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    status: null,
    accountId: null,
  });
  setHeaderData({
    appId: null,
    clientId: null,
  })
  getAccountSessionList();
}

const onOffline = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否下线该账号?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          tokenId: row.tokenId,
        }
        let res = await offlineAccountAuthorization_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('下线成功');
          confirmDia.hide();
          getAccountSessionList();
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

const onAllOffline = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否全部下线账号?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let res = await offlineAllAccountAuthorization_api({});
        if (res.code == 'Success') {
          MessagePlugin.success('下线成功');
          confirmDia.hide();
          getAccountSessionList();
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



const [headerData, setHeaderData] = useState({
  appId: null,
  clientId: null,
});
// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
    // if (appList.value.length > 0) {
    //   setHeaderData({
    //     ...headerData.value,
    //     appId: appList.value[0].appId
    //   });
    // }
  }
}
watch(() => headerData.value.appId, () => {
  if (headerData.value.appId) {
    setHeaderData({
      ...headerData.value,
      clientId: null
    })
    getClientList();
  }
});


// 客户端列表
const [clientList, setClientList] = useState([]);
const getClientList = async () => {
  let params = {
    appId: headerData.value.appId,
    authenticationTypes: ['account']
  };
  let res = await getClientList_api(params);
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}

// 账号列表
const [accountList, setAccountList] = useState([]);
const getAccountList = async () => {
  let params = {};
  let res = await getAccountList_api(params);
  if (res.code == 'Success') {
    setAccountList(res?.data || []);
  }
}

</script>
<template>
  <div class="accountComponent" v-allow="'account_authorization.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerData.appId" placeholder="请选择应用">
          <t-option v-for="(item, index) in appList" :key="index" :value="item.appId" :label="item.appName">
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
      <FilterItem label="客户端">
        <t-select filterable clearable v-model="headerData.clientId" placeholder="请选择客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
                    :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" clearable placeholder="请输入关键字"></t-input>
      </FilterItem>
      <FilterItem label="账号">
        <t-select filterable clearable v-model="search.accountId" placeholder="请选择账号">
          <t-option v-for="(item, index) in accountList" :key="index" :value="item.accountId"
                    :label="item.nickname">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :hideOnLoadFailed="true" :image="item.avatarUrl"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.nickname + ' (' + (item?.phoneNumber ||
                item?.username || item?.accountId) + ')' }}</span>
            </div>
          </t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="search.status" placeholder="请选择状态">
          <t-option label="使用中" value="ok"></t-option>
          <t-option label="已过期" value="expired"></t-option>
          <t-option label="下线" value="blacklist"></t-option>
          <t-option label="登出" value="logout"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button @click="onAllOffline" v-allow="'account_authorization.offline'">全部下线</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>
</template>
<style lang="scss" scoped>
.accountComponent {
  width: 100%;
}
</style>
