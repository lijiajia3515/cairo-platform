<!-- 应用 -->
<script setup lang="jsx">
import { ref, onMounted, watch, nextTick } from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn, userColumn } from '@/utils/tableColumns';

import {
  getAppList_api,
  getClientList_api,
  getEndpointList_api,
  getAppUserLoginLogPage_api,
  getSubappUserSysDictDetailInfo_api
} from '@/api';


onMounted(() => {
  getAppList();
  getLoginTypeList()
  nextTick(() => {
    // getTenantList();
  })
});

let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
let loading = ref(false);
const [configs] = useState({
  data: list,
  columns: [
    timeColumn('loginTime', '登录时间'),
    {
      colKey: 'loginType', title: '登录类型', width: 140, cell: (h, { row }) => {
        let types = loginTypeList.value.filter(item => item.value == row['loginType']);
        return types.length > 0 ? types[0].name : row['loginType'];
      }
    },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'IP' },
    userColumn({ colKey: 'user', title: '用户', recordKey: 'user', unbound: '未绑定账号' }),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId' }),
    { colKey: 'clientName', title: '客户端' },
    { colKey: 'os', title: '系统' },
    { colKey: 'app', title: '应用' },
    {
      colKey: 'success', title: '是否成功', cell: (h, { row }) => {
        return (
          <span
            style={{ color: row['success'] ? '#2ba471' : '#d54941' }}>{row['success'] ? '登录成功' : row['errMsg']}</span>
        )
      }
    }
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
    getLoginLogPage();
  }
});
const [headerData, setHeaderData] = useState({
  appId: null,
  clientId: null,
  endpointId: null,
  tenantId: null,
});
const [search, setSearch] = useState({
  keyword: null,
  times: [],
  loginType: null,
  userId: null,
  success: null,
});


// 登录类型
const [loginTypeList, setLoginTypeList] = useState([])
const getLoginTypeList = async () => {
  let params = { dictId: 'loginType' }
  let res = await getSubappUserSysDictDetailInfo_api(params)
  if (res.code === 'Success') {
    setLoginTypeList(res.data.items || [])
  }
}

// 应用登录分页
const getLoginLogPage = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      loginType: search.value.loginType,
      userId: search.value.userId,
      success: search.value.success,
    };
    if (search.value.times.length > 0) {
      params.startTime = search.value.times[0];
      params.endTime = search.value.times[1];
    }
    const headers = {
      'app-id': headerData.value.appId || '',
      'client-id': headerData.value.clientId || '',
      'endpoint-id': headerData.value.endpointId || '',
    };
    let res = await getAppUserLoginLogPage_api(params, headers);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } finally {
    loading.value = false;
  }
}
// 搜索
const onSearch = () => {
  page.value = 1;
  getLoginLogPage();
}
// 重置
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    times: [],
    loginType: null,
    userId: null,
    success: null,
  })
  getLoginLogPage();
}


// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {
    scopes: ["app"]
  };
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}
watch(() => headerData.value.appId, () => {
  setHeaderData({
    ...headerData.value,
    clientId: null,
    endpointId: null,
  })
  getClientList();
  getEndpointList();
})
// 客户端列表
const [clientList, setClientList] = useState([]);
const getClientList = async () => {
  let params = {
    appId: headerData.value.appId,
    authenticationTypes: ['app_user']
  };
  let res = await getClientList_api(params);
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}


// 终端列表 依赖应用
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: headerData.value.appId,
    scopeIds: ["app"]
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}
</script>

<template>
  <div class="app_user_component">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerData.appId" placeholder="应用">
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
      <FilterItem label="终端">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerData.endpointId" placeholder="终端">
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
        <t-select filterable v-model="headerData.clientId" placeholder="客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
            :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="用户ID">
        <t-input v-model="search.userId" clearable placeholder="用户ID"></t-input>
      </FilterItem>
      <FilterItem label="登录状态">
        <t-select v-model="search.success" clearable placeholder="登录状态">
          <t-option label="成功" :value="true"></t-option>
          <t-option label="失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="登录类型">
        <t-select filterable v-model="search.loginType" clearable placeholder="登录类型">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in loginTypeList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="search.times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" clearable placeholder="关键字"></t-input>
      </FilterItem>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>
</template>

<style lang="scss" scoped>
.app_user_component {
  width: 100%;
}
</style>
