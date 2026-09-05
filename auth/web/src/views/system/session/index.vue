<script setup lang="jsx">
defineOptions({ name: 'manage-system-session' })

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
  getAppUserAuthorizationPageList_api,
  getCurrentEndpointList_api,
  getClientList_api,
  getAppUserList_api,
  offlineAppUserAuthorization_api,
  offlineAllAppUserAuthorization_api
} from '@/api';
onMounted(() => {
  getSystemSessionList()
  getEndpointList();
  nextTick(() => {
    getAppUserList();
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
    { colKey: 'endpointName', title: '终端' },
    { colKey: 'clientName', title: '客户端' },
    userColumn({ colKey: 'userName', title: '用户', nameKey: 'userName', avatarKey: 'accountAvatarUrl', idKey: 'userName', width: 140 }),
    { colKey: 'loginType', title: '登录方式' },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'IP' },
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
      {content: '下线', onClick: (row) => onOffline(row), visible: () => hasPermission('app_user_authorization.offline')},
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
    getSystemSessionList();
  }
});

const [search, setSearch] = useState({
  keyword: null,
  status: null,
  userId: null,
});

// 登录会话列表
const getSystemSessionList = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      endpointId: headerData.value.endpointId,
      clientId: headerData.value.clientId,
      keyword: search.value.keyword,
      status: search.value.status,
      userId: search.value.userId,
    };
    let res = await getAppUserAuthorizationPageList_api(params);
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
  getSystemSessionList();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    status: null,
    userId: null,
  });
  setHeaderData({
    endpointId: null,
    clientId: null,
  })
  getSystemSessionList();
}

const onOffline = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否下线该用户?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          tokenId: row.tokenId,
        }
        let res = await offlineAppUserAuthorization_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('下线成功');
          confirmDia.hide();
          getSystemSessionList();
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
    body: '是否全部下线用户?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let res = await offlineAllAppUserAuthorization_api({});
        if (res.code == 'Success') {
          MessagePlugin.success('下线成功');
          confirmDia.hide();
          getSystemSessionList();
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
  endpointId: null,
  clientId: null,
});
// 终端列表
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {};
  let res = await getCurrentEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}
watch(() => headerData.value.endpointId, () => {
  if (headerData.value.endpointId) {
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
    endpointId: headerData.value.endpointId,
  };
  let res = await getClientList_api(params);
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}

// 用户列表
const [appUserList, setAppUserList] = useState([]);
const getAppUserList = async () => {
  let params = {};
  let res = await getAppUserList_api(params);
  if (res.code == 'Success') {
    setAppUserList(res?.data || []);
  }
}

</script>
<template>
  <div class="accountComponent" v-allow="'app_user_authorization.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="终端">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerData.endpointId"
                  placeholder="请选择终端">
          <t-option v-for="(item, index) in endpointList" :key="index" :value="item.endpointId"
                    :label="item.endpointName">
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
      <FilterItem label="客户端">
        <t-select filterable clearable v-model="headerData.clientId" placeholder="请选择客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
                    :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" clearable placeholder="请输入关键字"></t-input>
      </FilterItem>
      <FilterItem label="用户">
        <t-select filterable clearable v-model="search.userId" placeholder="请选择用户">
          <t-option v-for="(item, index) in appUserList" :key="index" :value="item.userId" :label="item.nickname">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :hideOnLoadFailed="true" :image="item.accountAvatarUrl"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.nickname }}</span>
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
        <t-button @click="onAllOffline" v-allow="'app_user_authorization.offline'">全部下线</t-button>
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
