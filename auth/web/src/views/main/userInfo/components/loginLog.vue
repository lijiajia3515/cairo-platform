<script setup lang="jsx">
import {
  onMounted,
  ref,
} from 'vue';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, avatarCopyColumn} from '@/utils/tableColumns';

import useState from '@/hooks/useState';

import {
  getMyAppUserLoginLogPage_api,
} from '@/api';

onMounted(() => {
  getMyEndpointUserLoginLogPage()
})

const [active, setactive] = useState(2);

/**
 * 应用级登录日志
 */
let endpointPage = ref(1);
let endpointSize = ref(10);
let endpointTotal = ref(0);
let list_endpoint = ref([]);
let loading = ref(false);
const [search_endpoint, setSearchEndpoint] = useState({
  keyword: null,
  loginType: null,
  times: [],
  clientId: null,
  success: null,
})
const [configs_endpoint, setConfigs_endpoint] = useState({
  data: list_endpoint,
  columns: [
    timeColumn('loginTime', '登录时间'),
    { colKey: 'loginType', title: '登录类型' },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'ip' },
    avatarCopyColumn({colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId'}),
    avatarCopyColumn({colKey: 'clientName', title: '客户端', iconKey: 'icon', copyKey: 'clientId'}),
    { colKey: 'os', title: '系统' },
    { colKey: 'app', title: '应用' },
    {
      colKey: 'success', title: '登录状态', cell: (h, { row }) => {
        return (
          <span style={{ color: row['success'] ? '#2ba471' : '#d54941' }}>{row['success'] ? '登录成功' : row['errMsg']}</span>
        )
      }
    },
  ],
  loading: loading,
  pagination: {
    current: endpointPage,
    pageSize: endpointSize,
    total: endpointTotal,
  },
  onPageChange: (pageInfo) => {
    endpointPage.value = pageInfo.current;
    endpointSize.value = pageInfo.pageSize;
    getMyEndpointUserLoginLogPage();
  }
});
const getMyEndpointUserLoginLogPage = async () => {
  loading.value = true;
  try {
    let params = {
      page: endpointPage.value - 1,
      size: endpointSize.value,
      keyword: search_endpoint.value.keyword,
      loginType: search_endpoint.value.loginType,
      success: search_endpoint.value.success,
    };
    if (search_endpoint.value.times && search_endpoint.value.times.length) {
      params['startTime'] = search_endpoint.value.times[0];
      params['endTime'] = search_endpoint.value.times[1];
    }
    let res = await getMyAppUserLoginLogPage_api(params);
    if (res.code == 'Success') {
      list_endpoint.value = res?.data?.contents || [];
      endpointTotal.value = Number(res?.data?.total) || 0;
    }
  } finally {
    loading.value = false;
  }
}

const onSearchEndpoint = () => {
  endpointPage.value = 1;
  list_endpoint.value = [];
  endpointTotal.value = 0;
  getMyEndpointUserLoginLogPage();
}
const onResetEndpoint = () => {
  endpointPage.value = 1;
  list_endpoint.value = [];
  endpointTotal.value = 0;
  setSearchEndpoint({
    keyword: null,
    loginType: null,
    times: [],
    clientId: null,
    success: null,
  })
  getMyEndpointUserLoginLogPage();
}

</script>

<template>
  <div class="log_service_wrapper">
    <t-tabs v-model="active">
      <t-tab-panel :value="2" label="应用">
        <div class="empty"></div>
        <FilterBar @search="onSearchEndpoint" @reset="onResetEndpoint">
          <FilterItem label="关键字">
            <t-input clearable v-model="search_endpoint.keyword" placeholder="请输入关键字"></t-input>
          </FilterItem>
          <FilterItem label="登录类型">
            <t-input clearable v-model="search_endpoint.loginType" placeholder="请输入登录类型"></t-input>
          </FilterItem>
          <FilterItem label="时间" wide>
            <t-date-range-picker v-model="search_endpoint.times" enable-time-picker allow-input clearable />
          </FilterItem>
          <FilterItem label="登录状态">
            <t-select clearable v-model="search_endpoint.success" placeholder="请选择登录状态">
              <t-option label="成功" :value="true"></t-option>
              <t-option label="失败" :value="false"></t-option>
            </t-select>
          </FilterItem>
        </FilterBar>
        <div class="empty"></div>
        <List @page-change="configs_endpoint.onPageChange" :configs="configs_endpoint" />
      </t-tab-panel>
    </t-tabs>
  </div>
</template>

<style lang="scss" scoped>
.log_service_wrapper {
  box-sizing: border-box;
  padding: 10px 20px;
}
</style>
