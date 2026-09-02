<!-- 登录日志 -->
<script setup lang="jsx">
import { ref, onMounted, nextTick, watch, } from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn } from '@/utils/tableColumns';

import {
  getMyAppUserLoginLogPage_api,

  getCurrentEndpointList_api,
  getCurrentClientList_api,
} from '@/api';
onMounted(() => {
  getMyEndpointUserLoginLogPage();

  nextTick(() => {
    getEndpointList();
  })
});

let keyword = ref(null); // 关键字
let endpointId = ref(null); // 终端标识
let clientId = ref(null); // 客户端ID
let loginType = ref(null); // 登录方式
let success = ref(null); // 登录状态
let times = ref([]); //
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    timeColumn('loginTime', '登录时间'),
    { colKey: 'loginType', title: '登录类型' },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'ip', width: 140 },
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId' }),
    avatarCopyColumn({ colKey: 'clientName', title: '客户端', iconKey: 'icon', copyKey: 'clientId' }),
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
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getMyEndpointUserLoginLogPage();
  }
});

/**
 * 登录日志 分页
 */
const getMyEndpointUserLoginLogPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      success: success.value,
    }
    if (loginType.value) {
      params['loginType'] = loginType.value;
    }
    if (times.value.length) {
      params['startTime'] = times.value[0];
      params['endTime'] = times.value[1];
    }
    if (endpointId.value) {
      params['endpointId'] = endpointId.value;
    }
    if (clientId.value) {
      params['clientId'] = clientId.value;
    }
    let res = await getMyAppUserLoginLogPage_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = res?.data?.total;
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getMyEndpointUserLoginLogPage();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  endpointId.value = null;
  clientId.value = null;
  loginType.value = null;
  success.value = null;
  times.value = [];
  getMyEndpointUserLoginLogPage();
}


// 当前 终端 列表
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {};
  let res = await getCurrentEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}

// 选择终端
watch(endpointId, () => {
  clientId.value = null;
  getClientList();
})

// 当前 客户端列表
const [clientList, setClientList] = useState([]);
const getClientList = async () => {
  let params = {
    endpointId: endpointId.value
  }
  let res = await getCurrentClientList_api(params);
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}
</script>


<template>
  <div class="loginLog__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input placeholder="请输入关键字" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="登录类型">
        <t-input placeholder="请输入登录类型" v-model="loginType"></t-input>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="终端">
        <t-select clearable v-model="endpointId" placeholder="请选择终端标识">
          <t-option :label="item.endpointName" :value="item.endpointId" v-for="(item, index) in endpointList"
            :key="index">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.endpointIcon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="endpointList.filter(item => item.endpointId == value)[0]?.endpointIcon"
                  shape="round"></t-avatar>
                {{ endpointList.filter(item => item.endpointId == value)[0]?.endpointName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="客户端">
        <t-select clearable v-model="clientId" placeholder="请选择客户端">
          <t-option :label="item.clientName" :value="item.clientId" v-for="(item, index) in clientList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="登录状态">
        <t-select clearable v-model="success" placeholder="请选择登录状态">
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
.loginLog__wrapper {}
</style>
