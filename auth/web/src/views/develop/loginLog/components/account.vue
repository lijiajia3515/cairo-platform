<!-- 账号 -->
<script setup lang="jsx">
import { ref, onMounted, watch, nextTick } from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import AccountInfo from '@/components/accountInfo';
import { timeColumn, avatarCopyColumn, userColumn } from '@/utils/tableColumns';

import {
  getAccountLoginLogPage_api,
  getAppList_api,
  getClientList_api,
  getAccountList_api,
  getSubappUserSysDictDetailInfo_api
} from '@/api';


onMounted(() => {
  getAppList();
  getLoginTypeList()
  getAuthTypeList()
  nextTick(() => {
    getAccountList();
  })
});

// 登录类型
const [loginTypeList, setLoginTypeList] = useState([])
const getLoginTypeList = async () => {
  let params = { dictId: 'LoginType' }
  let res = await getSubappUserSysDictDetailInfo_api(params)
  if (res.code === 'Success') {
    setLoginTypeList(res.data?.items || [])
  }
}

// 认证方式
const [authTypeList, setAuthTypeList] = useState([])

const getAuthTypeList = async () => {
  let params = { dictId: 'AccountAuthType' }
  let res = await getSubappUserSysDictDetailInfo_api(params)
  if (res.code === 'Success') {
    setAuthTypeList(res.data?.items || [])
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
    timeColumn('loginTime', '登录时间'),
    { colKey: 'authType', title: '认证方式' },
    {
      colKey: 'loginType', title: '登录类型', width: 140, cell: (h, { row }) => {
        let types = loginTypeList.value.filter(item => item.value == row['loginType']);
        return types.length > 0 ? types[0].name : row['loginType'];
      }
    },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'IP' },
    userColumn({ colKey: 'account', title: '账号', recordKey: 'account', nameKey: 'nickname', avatarKey: 'avatarUrl', onClick: (src) => onWatchAccountInfo(src) }),
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    { colKey: 'clientName', title: '客户端' },
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
  loginType: null,
  authType: null,
  success: null,
  accountId: null,
});

// 账号登录分页
const getLoginLogPage = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      loginType: search.value.loginType,
      authType: search.value.authType,
      success: search.value.success,
      accountId: search.value.accountId,
    };
    if (search.value.times.length > 0) {
      params.startTime = search.value.times[0];
      params.endTime = search.value.times[1];
    }
    const headers = {
      'app-id': headerData.value.appId || '',
      'client-id': headerData.value.clientId || ''
    };
    let res = await getAccountLoginLogPage_api(params, headers);
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
    authType: null,
    success: null,
    accountId: null,
  })
  getLoginLogPage();
}


/**
 **************************************************** 用户详情
 */
let accountInfoRef = ref(null); // 账号详情
const [accountDetail, setAccountDetail] = useState({});
const onWatchAccountInfo = (data) => {
  accountInfoRef.value.open();
  setAccountDetail(data)
}

const onCloseAccountInfo = () => {
  setAccountDetail({});
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
  <div class="account_component">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerData.appId" placeholder="应用">
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
        <t-select filterable clearable v-model="headerData.clientId" placeholder="客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
            :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="登录类型">
        <t-select filterable clearable v-model="search.loginType" placeholder="登录类型">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in loginTypeList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="登录状态">
        <t-select v-model="search.success" clearable placeholder="登录状态">
          <t-option label="成功" :value="true"></t-option>
          <t-option label="失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="认证方式">
        <t-select filterable clearable v-model="search.authType" placeholder="认证方式">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in authTypeList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="search.times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" clearable placeholder="关键字"></t-input>
      </FilterItem>
      <FilterItem label="账号">
        <t-select v-model="search.accountId" clearable filterable placeholder="账号">
          <t-option v-for="(item, index) in accountList" :key="index" :value="item.accountId" :label="item.nickname">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :hideOnLoadFailed="true" v-if="item.avatarUrl" :image="item.avatarUrl"
                :alt="item.nickname.substring(0, 2)"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.nickname + ' (' + (item?.phoneNumber ||
                item?.username || item?.accountId) + ')' }}</span>
            </div>
          </t-option>
        </t-select>
      </FilterItem>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <!-- 账号详情 -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>
</template>

<style lang="scss" scoped>
.account_component {
  width: 100%;
}
</style>
