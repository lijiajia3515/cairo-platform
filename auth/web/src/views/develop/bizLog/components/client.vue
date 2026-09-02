<script setup lang="jsx">
import {
  ref,
  onMounted,
  watch,
} from 'vue';
import {
  MessagePlugin
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, ellipsisColumn} from '@/utils/tableColumns';

import {
  getAppList_api,
  getClientList_api,
  getClientBizLogPage_api
} from '@/api';
onMounted(() => {
  getAppList();
});


let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
let loading = ref(false);
const [configs] = useState({
  data: list,
  columns: [
    timeColumn('startTime', '时间'),
    { colKey: 'clientName', title: '客户端', width: 200 },
    { colKey: 'ip', title: 'IP' },
    ellipsisColumn('bizId', '业务ID', {width: 250}),
    { colKey: 'scope', title: '范围' },
    ellipsisColumn('params', '参数', {width: 400}),
    { colKey: 'mills', title: '耗时(毫秒)' },
    {
      colKey: 'success', title: '结果', cell: (h, { row }) => {
        return (
          <span style={{ color: row['success'] ? '#2ba471' : '#d54941' }}>{row['success'] ? '成功' : row['errorMessage']}</span>
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
    getServiceLog();
  }
});

const [search, setSearch] = useState({
  keyword: null,
  times: [],
  success: null,
});

// 业务日志
const getServiceLog = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      success: search.value.success,
    };
    if (search.value.times.length > 0) {
      params.startTime = search.value.times[0];
      params.endTime = search.value.times[1];
    }
    let headers = {};
    if (headerData.value.appId) {
      headers['app-id'] = headerData.value.appId || ''
    }
    headers['client-id'] = headerData.value.clientId || '';
    let res = await getClientBizLogPage_api(params, headers);
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
  if (!headerData.value.appId) {
    MessagePlugin.error('请选择应用')
    return;
  }
  page.value = 1;
  getServiceLog();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    times: [],
    success: null,
  });
  setHeaderData({
    appId: null,
    clientId: null
  });
  if (!headerData.value.appId) {
    MessagePlugin.error('请选择应用');
    list.value = [];
    return;
  }
  getServiceLog();
}



const [headerData, setHeaderData] = useState({
  appId: null,
  clientId: null
});
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
    authenticationTypes: ['client']
  };
  let res = await getClientList_api(params);
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}

</script>
<template>
  <div class="accountComponent">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="*应用">
        <t-select :scroll="{ type: 'virtual' }" v-model="headerData.appId" placeholder="应用">
          <t-option v-for="(item, index) in appList" :key="index" :value="item.appId" :label="item.appName"
            :style="{ height: '40px', width: '100%' }">
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
        <t-select v-model="headerData.clientId" clearable placeholder="客户端">
          <t-option v-for="(item, index) in clientList" :key="index" :value="item.clientId"
            :label="item.clientName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="search.times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="关键字">
        <t-input clearable v-model="search.keyword" placeholder="请输入关键字"></t-input>
      </FilterItem>
      <FilterItem label="结果">
        <t-select clearable v-model="search.success" placeholder="请选择结果">
          <t-option label="成功" :value="true"></t-option>
          <t-option label="失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
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
