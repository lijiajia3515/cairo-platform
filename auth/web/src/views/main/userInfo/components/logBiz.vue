<!-- 我的业务日志（应用级 / 子应用级） -->
<script setup lang="jsx">
import {
  onMounted,
  watch,
  ref,
} from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, ellipsisColumn, avatarCopyColumn } from '@/utils/tableColumns';

import {
  getMyAppBizLogPage_api,
  getMySubappBizLogPage_api
} from '@/api';
import {
  getCurrentClientList_api,
  getCurrentEndpointList_api,
  getCurrentSubappList_api,
  getCurrentSubappVersionList_api
} from "@/api/index.js";

onMounted(() => {
  setactive(2);
})
const [active, setactive] = useState(null);

watch(active, () => {
  if (active.value) {
    if (active.value == 2) {
      endpointList.value = [];
      endpointPage.value = 1;
      endpointTotal.value = 0;
      getMyAppBizLogPageList();
      getEndpointEndpointList()
      getEndpointClientList();
    }else if (active.value == 3) {
      subappList.value = [];
      subappPage.value = 1;
      subappTotal.value = 0;
      getMySubappBizLogPageList();
      getSubappEndpointList()
      getSubappSubappList()
    }
  }
});


// 应用业务日志列表
let endpointPage = ref(1);
let endpointSize = ref(10);
let endpointTotal = ref(0);
let endpointList = ref([]);
let endpointLoading = ref(false);
const [endpointSearch, setEndpointSearch] = useState({
  times: [],
  clientId: null,
  endpointId: null,
  success: null,
  keyword: null,
})
const [endpointConfigs, setEndpointConfigs] = useState({
  data: endpointList,
  columns: [
    timeColumn('startTime', '开始时间'),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId', width: 200 }),
    { colKey: 'clientName', title: '客户端', width: 150, },
    { colKey: 'ip', title: 'IP', width: 140, },
    ellipsisColumn('bizId', '业务ID', { width: 180 }),
    { colKey: 'scope', title: '范围' },
    ellipsisColumn('params', '参数', { width: 300 }),
    { colKey: 'mills', title: '耗时(毫秒)' },
    {
      colKey: 'success', title: '结果', width: 100, ellipsis: true, cell: (h, {row}) => {
        return (
            <span style={{color: row['success'] ? '#2ba471' : '#d54941'}}>{row['success'] ? '操作成功' : row['errorMessage']}</span>
        )
      }
    },
  ],
  loading: endpointLoading,
  pagination: {
    current: endpointPage,
    pageSize: endpointSize,
    total: endpointTotal,
  },
  onPageChange: (pageInfo) => {
    endpointPage.value = pageInfo.current;
    endpointSize.value = pageInfo.pageSize;
    getMyAppBizLogPageList();
  }
});

const getMyAppBizLogPageList = async () => {
  endpointLoading.value = true;
  try {
    let params = {
      page: endpointPage.value - 1,
      size: endpointSize.value,
      endpointId: endpointSearch.value.endpointId,
      clientId: endpointSearch.value.clientId,
      success: endpointSearch.value.success,
      keyword: endpointSearch.value.keyword,
    };
    if (endpointSearch.value.times && endpointSearch.value.times.length) {
      params['startTime'] = endpointSearch.value.times[0];
      params['endTime'] = endpointSearch.value.times[1];
    }
    let res = await getMyAppBizLogPage_api(params);
    if (res.code == 'Success') {
      endpointList.value = res?.data?.contents || [];
      endpointTotal.value = Number(res?.data?.total) || 0;
    }
  } finally {
    endpointLoading.value = false;
  }
}

const onEndpointSearch = () => {
  endpointPage.value = 1;
  getMyAppBizLogPageList();
}
const onEndpointReset = () => {
  endpointPage.value = 1;
  setEndpointSearch({
    times: [],
    clientId: null,
    endpointId: null,
    success: null,
    keyword: null,
  })
  getMyAppBizLogPageList();
}
// 终端列表
const [endpointEndpointList, setEndpointEndpointList] = useState([]);
const getEndpointEndpointList = async () => {
  let params = {
    scopeIds: ["app"]
  };
  let res = await getCurrentEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointEndpointList(res?.data || []);
  }
}

// 终端下拉框联动
watch(() => endpointSearch.value.endpointId, () => {
  setEndpointSearch({
    ...endpointSearch.value,
    clientId: null
  });
  if (endpointSearch.value.endpointId) {
    getEndpointClientList();
  }
});

// 客户端列表
const [endpointClientList, setEndpointClientList] = useState([]);
const getEndpointClientList = async () => {
  let params = {
    endpointId: endpointSearch.value.endpointId,
    authenticationTypes: ['app_user']
  };
  let res = await getCurrentClientList_api(params);
  if (res.code == 'Success') {
    setEndpointClientList(res?.data || []);
  }
}



//子应用业务日志列表
let subappPage = ref(1);
let subappSize = ref(10);
let subappTotal = ref(0);
let subappList = ref([]);
let subappLoading = ref(false);
const [subappSearch, setSubappSearch] = useState({
  times: [],
  endpointId: null,
  subappId: null,
  subappVersion: null,
  success: null,
  keyword: null,
});

const [subappConfigs, setSubappConfigs] = useState({
  data: subappList,
  columns: [
    timeColumn('startTime', '开始时间'),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId', width: 200 }),
    {
      colKey: 'subapp', title: '子应用', width: 220, cell: (h, { row }) => {
        return (
            <t-space size="small">
              {
                row?.subappIcon ? <t-avatar shape="round" hideOnLoadFailed={true} alt={row?.subappName?.slice(0, 2)} size="16px" image={row?.subappIcon} /> : null
              }
              <div style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.subappName || "unknown"}/{row?.subappVersion || "unknown"}</div>
            </t-space>
        )
      }
    },
    { colKey: 'ip', title: 'IP', width: 140, },
    ellipsisColumn('bizId', '业务ID', { width: 180 }),
    { colKey: 'scope', title: '范围' },
    ellipsisColumn('params', '参数', { width: 300 }),
    { colKey: 'mills', title: '耗时(毫秒)' },
    {
      colKey: 'success', title: '结果', width: 100, ellipsis: true, cell: (h, {row}) => {
        return (
            <span style={{color: row['success'] ? '#2ba471' : '#d54941'}}>{row['success'] ? '操作成功' : row['errorMessage']}</span>
        )
      }
    },
  ],
  loading: subappLoading,
  pagination: {
    current: subappPage,
    pageSize: subappSize,
    total: subappTotal,
  },
  onPageChange: (pageInfo) => {
    subappPage.value = pageInfo.current;
    subappSize.value = pageInfo.pageSize;
    getMySubappBizLogPageList();
  }
});

const getMySubappBizLogPageList = async () => {
  subappLoading.value = true;
  try {
    let params = {
      page: subappPage.value - 1,
      size: subappSize.value,
      endpointId: subappSearch.value.endpointId,
      subappId: subappSearch.value.subappId,
      subappVersion: subappSearch.value.subappVersion,
      success: subappSearch.value.success,
      keyword: subappSearch.value.keyword,
    };
    if (subappSearch.value.times && subappSearch.value.times.length) {
      params['startTime'] = subappSearch.value.times[0];
      params['endTime'] = subappSearch.value.times[1];
    }
    let res = await getMySubappBizLogPage_api(params);
    if (res.code == 'Success') {
      subappList.value = res?.data?.contents || [];
      subappTotal.value = Number(res?.data?.total) || 0;
    }
  } finally {
    subappLoading.value = false;
  }
}

const onSubappSearch = () => {
  subappPage.value = 1;
  getMySubappBizLogPageList();
}
const onSubappReset = () => {
  subappPage.value = 1;
  setSubappSearch({
    times: [],
    endpointId: null,
    subappId: null,
    subappVersion: null,
    success: null,
    keyword: null,
  })
  getMySubappBizLogPageList();
}

// 子应用
// 终端列表
const [subappEndpointList, setSubappEndpointList] = useState([]);
const getSubappEndpointList = async () => {
  let params = {
    scopeIds: ["app"]
  };
  let res = await getCurrentEndpointList_api(params);
  if (res.code == 'Success') {
    setSubappEndpointList(res?.data || []);
  }
}

// 终端下拉框联动
watch(() => subappSearch.value.endpointId, () => {
  setSubappSearch({
    ...subappSearch.value,
    subappId: null
  });
  if (subappSearch.value.endpointId) {
    getSubappSubappList();
  }
});

const [subappSubappList, setSubappSubappList] = useState([]);
const getSubappSubappList = async () => {
  let params = {
    scopeIds: ["app"]
  };
  let res = await getCurrentSubappList_api(params);
  if (res.code == 'Success') {
    setSubappSubappList(res?.data || []);
  }
}

// 终端下拉框联动
watch(() => subappSearch.value.subappId, () => {
  setSubappSearch({
    ...subappSearch.value,
    subappVersion: null
  });
  if (subappSearch.value.subappId) {
    getSubappSubappVersionList();
  }
});

const [subappSubappVersionList, setSubappSubappVersionList] = useState([]);
const getSubappSubappVersionList = async () => {
  let params = {
    subappId: subappSearch.value.subappId
  };
  let res = await getCurrentSubappVersionList_api(params);
  if (res.code == 'Success') {
    setSubappSubappVersionList(res?.data || []);
  }
}
</script>

<template>
  <div class="log_service_wrapper">
    <t-tabs v-model="active">
      <t-tab-panel :value="2" label="应用">
        <div class="empty"></div>
        <FilterBar @search="onEndpointSearch" @reset="onEndpointReset">
          <FilterItem label="终端">
            <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="endpointSearch.endpointId"
                      placeholder="请选择终端">
              <t-option v-for="(item, index) in endpointEndpointList" :key="index" :value="item.endpointId"
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
                              :image="endpointEndpointList.filter(item => item.endpointId == value)[0]?.icon"
                              shape="round"></t-avatar>
                    {{ endpointEndpointList.filter(item => item.endpointId == value)[0]?.endpointName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </FilterItem>
          <FilterItem label="客户端">
            <t-select filterable clearable v-model="endpointSearch.clientId" placeholder="请选择客户端">
              <t-option v-for="(item, index) in endpointClientList" :key="index" :value="item.clientId"
                        :label="item.clientName"></t-option>
            </t-select>
          </FilterItem>
          <FilterItem label="关键字">
            <t-input v-model="endpointSearch.keyword" clearable placeholder="请输入关键字"></t-input>
          </FilterItem>
          <FilterItem label="时间" wide>
            <t-date-range-picker v-model="endpointSearch.times" enable-time-picker allow-input clearable />
          </FilterItem>
          <FilterItem label="结果">
            <t-select clearable v-model="endpointSearch.success" placeholder="请选择结果">
              <t-option label="操作成功" :value="true"></t-option>
              <t-option label="操作失败" :value="false"></t-option>
            </t-select>
          </FilterItem>
        </FilterBar>
        <div class="empty"></div>
        <List @page-change="endpointConfigs.onPageChange" :configs="endpointConfigs"/>
      </t-tab-panel>
      <t-tab-panel :value="3" label="子应用">
        <div class="empty"></div>
        <FilterBar @search="onSubappSearch" @reset="onSubappReset">
          <FilterItem label="终端">
            <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="subappSearch.endpointId"
                      placeholder="请选择终端">
              <t-option v-for="(item, index) in subappEndpointList" :key="index" :value="item.endpointId"
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
                              :image="subappEndpointList.filter(item => item.endpointId == value)[0]?.icon"
                              shape="round"></t-avatar>
                    {{ subappEndpointList.filter(item => item.endpointId == value)[0]?.endpointName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </FilterItem>
          <FilterItem label="子应用">
            <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="subappSearch.subappId"
                      placeholder="请选择子应用">
              <t-option v-for="(item, index) in subappSubappList" :key="index" :value="item.subappId"
                        :label="item.subappName">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.subappIcon" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.subappName }}</span>
                </div>
              </t-option>
              <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                              :image="subappSubappList.filter(item => item.subappId == value)[0]?.subappIcon"
                              shape="round"></t-avatar>
                    {{ subappSubappList.filter(item => item.subappId == value)[0]?.subappName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </FilterItem>
          <FilterItem label="版本">
            <t-select filterable clearable v-model="subappSearch.subappVersion" placeholder="请选择子应用版本">
              <t-option v-for="(item, index) in subappSubappVersionList" :key="index" :value="item.subappVersion"
                        :label="item.subappVersion"></t-option>
            </t-select>
          </FilterItem>
          <FilterItem label="关键字">
            <t-input v-model="subappSearch.keyword" clearable placeholder="请输入关键字"></t-input>
          </FilterItem>
          <FilterItem label="时间" wide>
            <t-date-range-picker v-model="subappSearch.times" enable-time-picker allow-input clearable />
          </FilterItem>
          <FilterItem label="结果">
            <t-select clearable v-model="subappSearch.success" placeholder="请选择结果">
              <t-option label="操作成功" :value="true"></t-option>
              <t-option label="操作失败" :value="false"></t-option>
            </t-select>
          </FilterItem>
        </FilterBar>
        <div class="empty"></div>
        <List @page-change="subappConfigs.onPageChange" :configs="subappConfigs"/>
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
