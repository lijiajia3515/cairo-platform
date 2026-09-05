<!-- 企业子应用级业务日志 -->
<script setup lang="jsx">
import {
  ref,
  onMounted,
  watch,
} from 'vue';
import {debounce} from 'lodash';
import {
  MessagePlugin
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, ellipsisColumn, avatarCopyColumn, userColumn } from '@/utils/tableColumns';

import {
  getAppList_api,
  getTenantList_api,
  getEndpointList_api,
  getSubappVersionList_api,
  getSubappList_api,
  getTenantSubappBizLogPage_api
} from '@/api';

onMounted(() => {
  getTenantList();
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
    userColumn({ colKey: 'user', title: '用户', recordKey: 'user', unbound: '未绑定账号' }),
    avatarCopyColumn({colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId', width: 130}),
    {
      colKey: 'subapp', title: '子应用', width: '130', cell: (h, {row}) => {
        return (
            <t-space size="small">
              {
                row?.subappIcon ?
                    <t-avatar shape="round" hideOnLoadFailed={true} alt={row?.subappName?.slice(0, 2)} size="16px"
                              image={row?.subappIcon}/> : null
              }
              <div style={{
                height: '100%',
                display: 'flex',
                alignItems: 'center'
              }}>{row?.subappName || "unknown"}/{row?.subappVersion || "unknown"}</div>
            </t-space>
        )
      }
    },
    {colKey: 'ip', title: 'IP'},
    ellipsisColumn('bizId', '业务ID', {width: 250}),
    {colKey: 'scope', title: '范围'},
    ellipsisColumn('params', '参数', {width: 400}),
    {colKey: 'mills', title: '耗时(毫秒)', width: '100'},
    {
      colKey: 'success', title: '结果', cell: (h, {row}) => {
        return (
            <span
                style={{color: row['success'] ? '#2ba471' : '#d54941'}}>{row['success'] ? '成功' : row['errorMessage']}</span>
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
  subappId: null,
  subappVersion: null,
  times: [],
  success: null,
  userId: null,
  keyword: null,
});

// 业务日志
const getServiceLog = debounce(async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      subappId: search.value.subappId,
      subappVersion: search.value.subappVersion,
      userId: search.value.userId,
      success: search.value.success
    };
    if (search.value.times.length > 0) {
      params.startTime = search.value.times[0];
      params.endTime = search.value.times[1];
    }
    let headers = {
      'tenant-id': headerData.value.tenantId || '',
      'app-id': headerData.value.appId || '',
      'endpoint-id': headerData.value.endpointId || ''
    };
    let res = await getTenantSubappBizLogPage_api(params, headers);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } catch (err) {
    console.log(err)
  } finally {
    loading.value = false;
  }
})

const onSearch = () => {
  if (!headerData.value.tenantId) {
    MessagePlugin.error('请选择企业');
    return;
  }
  if (!headerData.value.appId) {
    MessagePlugin.error('请选择应用');
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
    userId: null,
    subappId: null,
    subappVersion:null,
  });
  setHeaderData({
    tenantId: null,
    appId: null,
    endpointId: null,
  });
  if (!headerData.value.tenantId) {
    MessagePlugin.error('请选择企业');
    list.value = [];
    return;
  }
  if (!headerData.value.appId) {
    MessagePlugin.error('请选择应用');
    list.value = [];
    return;
  }
  getServiceLog();
}


const [headerData, setHeaderData] = useState({
  tenantId: null,
  appId: null,
  endpointId: null,
});

// 企业列表
const [tenantList, setTenantList] = useState([]);
const getTenantList = async () => {
  let params = {};
  let res = await getTenantList_api(params);
  if (res.code == 'Success') {
    setTenantList(res?.data || []);
  }
}

// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {
    scopes: ["tenant"]
  };
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}

// 终端列表 依赖应用
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: headerData.value.appId,
    scopeIds: ["tenant"]
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}


// 子应用列表
const [subappList, setSubappList] = useState([]);
const getSubappList = async () => {
  let headers = {
    'app-id': headerData.value.appId,
    'endpoint-id': headerData.value.endpointId,
  };
  let params = {};
  let res = await getSubappList_api(params, headers);
  if (res.code == 'Success') {
    setSubappList(res?.data || []);
  }
}

//子应用版本列表
const [subappVersionList, setSubappVersionList] = useState([]);
const getSubappVersionList = async () => {
  let headers = {};
  let params = {
    subappId: search.value.subappId
  };
  let res = await getSubappVersionList_api(params, headers);
  if (res.code == 'Success') {
    setSubappVersionList(res?.data || []);
  }
}

watch(() => headerData.value.appId, () => {
  if (headerData.value.appId) {
    setHeaderData({
      ...headerData.value,
      endpointId: null,
    });
    setSearch({
      ...search.value,
      subappId: null,
      subappVersion: null,
    });

    getEndpointList();
    getSubappList();
  }
});

watch(() => headerData.value.endpointId, () => {
  if (headerData.value.endpointId) {
    setHeaderData({
      ...headerData.value,
    })
    setSearch({
      ...search.value,
      subappId: null,
      subappVersion: null,
    });
    getSubappList();
  }
});

watch(() => search.value.subappId, () => {
  if (search.value.subappId) {
    setSearch({
      ...search.value,
      subappVersion: null,
    });
    getSubappVersionList();
  }
});

</script>
<template>
  <div class="tenantSubappComponent">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="*企业">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerData.tenantId" placeholder="企业">
          <t-option v-for="(item, index) in tenantList" :key="index" :value="item.tenantId"
                    :label="item.tenantName" :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.tenantName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                          :image="tenantList.filter(item => item.tenantId == value)[0]?.icon"
                          shape="round"></t-avatar>
                {{ tenantList.filter(item => item.tenantId == value)[0]?.tenantName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="*应用">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerData.appId" placeholder="应用">
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
      <FilterItem label="终端">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerData.endpointId"
                  placeholder="终端">
          <t-option v-for="(item, index) in endpointList" :key="index" :value="item.endpointId"
                    :label="item.endpointName" :style="{ height: '40px', width: '100%' }">
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
      <FilterItem label="子应用">
        <t-select filterable clearable v-model="search.subappId" placeholder="子应用">
          <t-option v-for="(item, index) in subappList" :key="index" :value="item.subappId"
                    :label="item.subappName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="子应用版本">
        <t-select filterable clearable v-model="search.subappVersion" placeholder="子应用版本">
          <t-option v-for="(item, index) in subappVersionList" :key="index" :value="item.subappId"
                    :label="item.subappVersion"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="search.times" enable-time-picker allow-input clearable/>
      </FilterItem>
      <FilterItem label="关键字">
        <t-input clearable v-model="search.keyword" placeholder="请输入关键字"></t-input>
      </FilterItem>
      <FilterItem label="用户ID">
        <t-input clearable v-model="search.userId" placeholder="请输入用户ID"></t-input>
      </FilterItem>
      <FilterItem label="结果">
        <t-select clearable v-model="search.success" placeholder="请选择结果">
          <t-option label="成功" :value="true"></t-option>
          <t-option label="失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"/>
  </div>
</template>
<style lang="scss" scoped>
.tenantSubappComponent {
  width: 100%;
}
</style>
