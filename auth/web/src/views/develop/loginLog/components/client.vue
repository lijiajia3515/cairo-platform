<!-- 客户端 -->
<script setup lang="jsx">
import { ref, onMounted, watch, nextTick } from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn } from '@/utils/tableColumns';

import {
  getClientLoginLogPage_api,
  getAppList_api,
  getClientList_api,
  getAccountList_api,
  getSubappUserSysDictDetailInfo_api
} from '@/api';


onMounted(() => {
  getAppList();
  getGrantTypeList()
  nextTick(() => {
    getAccountList();
  })
});

// 授权类型
// const grantTypeList = [
//   { name: '授权码认证', value: 'authorization_code' },
//   { name: '刷新令牌', value: 'refresh_token' },
//   { name: '客户端认证', value: 'client_credentials' },
//   { name: '密码认证', value: 'password' },
// ]
const [grantTypeList, setGrantTypeList] = useState([])
let params = { dictId: 'AuthorizationGrantType' }
const getGrantTypeList = async () => {
  let res = await getSubappUserSysDictDetailInfo_api(params)
  if (res.code === "Success") {
    setGrantTypeList(res.data?.items || [])
  }

}


let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
let loading = ref(false);
const [configs] = useState({
  data: list,
  columns: [
    timeColumn('loginTime', '日志时间'),
    { colKey: 'method', title: '认证方式' },
    {
      colKey: 'grantType', title: '授权类型', width: 140, cell: (h, { row }) => {
        let grantTypes = grantTypeList.value.filter(item => item.value == row['grantType']);
        return grantTypes.length > 0 ? grantTypes[0].name : row['grantType'];
      }
    },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'IP' },
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    { colKey: 'clientName', title: '客户端名称' },
    { colKey: 'os', title: '系统' },
    { colKey: 'app', title: '应用' },
    {
      colKey: 'success', title: '是否成功', cell: (h, { row }) => {
        return (
          <span style={{ color: row['success'] ? '#2ba471' : '#d54941' }}>{row['success'] ? '登录成功' : row['errMsg']}</span>
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
});
const [search, setSearch] = useState({
  keyword: null,
  times: [],
  grantType: null,
  success: null,
});

// 账号登录分页
const getLoginLogPage = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      grantType: search.value.grantType,
      success: search.value.success,
    };
    if (search.value.times.length > 0) {
      params.startTime = search.value.times[0];
      params.endTime = search.value.times[1];
    }
    const headers = {
      'app-id': headerData.value.appId || '',
      'client-id': headerData.value.clientId || ''
    };
    let res = await getClientLoginLogPage_api(params, headers);
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
    grantType: null,
    success: null,
  })
  getLoginLogPage();
}


// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}
watch(() => headerData.value.appId, () => {
  setHeaderData({
    ...headerData.value,
    clientId: null
  })
  getClientList();
})
// 客户端列表
const [clientList, setClientList] = useState([]);
const getClientList = async () => {
  let params = {
    appId: headerData.value.appId,
    authenticationTypes: ['client']
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
  <div class="client_component">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" clearable filterable v-model="headerData.appId" placeholder="应用">
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
        <t-select clearable filterable v-model="headerData.clientId" placeholder="客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
            :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="授权类型">
        <t-select filterable clearable v-model="search.grantType" placeholder="授权类型">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in grantTypeList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="登录状态">
        <t-select v-model="search.success" clearable placeholder="登录状态">
          <t-option label="成功" :value="true"></t-option>
          <t-option label="失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="search.times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="关键字">
        <t-input clearable v-model="search.keyword" placeholder="关键字"></t-input>
      </FilterItem>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>
</template>

<style lang="scss" scoped>
.client_component {
  width: 100%;
}
</style>
