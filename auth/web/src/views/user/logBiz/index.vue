<!-- 业务日志 -->
<script setup lang="jsx">
import { ref, onMounted } from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, ellipsisColumn, avatarCopyColumn} from '@/utils/tableColumns';

import {
  getCurrentClientList_api,
  getCurrentEndpointList_api,
} from '@/api';
import {
  getMyAppBizLogPage_api
} from '@/api'

onMounted(() => {
  getMyAppBizLogPage();
  getClientList();
  getCurrentEndpointList();
});


let page = ref(1);
let size = ref(10);
let total = ref(0);
let keyword = ref(null); // 关键字
let times = ref([]);
let endpointId = ref(null); // 终端id
let clientId = ref(null); // 客户端id
let success = ref(null); // 结果

const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    timeColumn('startTime', '时间'),
    avatarCopyColumn({colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId'}),
    { colKey: 'clientName', title: '客户端名称', width: 130, },
    { colKey: 'ip', title: 'IP', width: 120, },
    { colKey: 'scope', title: '范围' },
    ellipsisColumn('bizId', '业务id', {width: 250}),
    ellipsisColumn('params', '参数', {width: 400}),
    { colKey: 'mills', title: '耗时（毫秒）' },
    {
      colKey: '', title: '结果', cell: (h, { row }) => {
        return (
          <span style={{ color: row['success'] == true ? '#2ba471' : '#d54941', fontWeight: 700 }}>
            {
              row['success'] == true ? '操作成功' : row['errorMessage']
            }
          </span>
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
    getMyAppBizLogPage();
  }
});


/**
 * 业务日志列表 分页
 */
const getMyAppBizLogPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      clientId: clientId.value,
      endpointId: endpointId.value,
      success: success.value,
    };
    if (times.value.length) {
      params['startTime'] = times.value[0];
      params['endTime'] = times.value[1];
    }
    let res = await getMyAppBizLogPage_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = res?.data?.total || 0;
    }
  } finally {
    setLoading(false);
  }
}

/**
 * 查询
 */
const onSearch = () => {
  page.value = 1;
  getMyAppBizLogPage();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  times.value = [];
  clientId.value = null;
  success.value = null;
  endpointId.value = null;
  getMyAppBizLogPage();
}


/**
 * 客户端列表
 */
const [clientList, setClientList] = useState([]);
const getClientList = async () => {
  let res = await getCurrentClientList_api({});
  if (res.code == 'Success') {
    setClientList(res?.data || []);
  }
}

/**
 * 终端列表
 */
const [endpointList, setEndpointList] = useState([])
const getCurrentEndpointList = async () => {
  let res = await getCurrentEndpointList_api({});
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}
</script>


<template>
  <div class="logBiz__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input clearable placeholder="请输入关键字" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="时间" wide>
        <t-date-range-picker v-model="times" enable-time-picker allow-input clearable />
      </FilterItem>
      <FilterItem label="结果">
        <t-select clearable v-model="success" placeholder="请选择结果">
          <t-option label="操作成功" :value="true"></t-option>
          <t-option label="操作失败" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="终端">
        <t-select clearable v-model="endpointId" placeholder="请选择终端">
          <t-option :label="item.endpointName" :value="item.endpointId" v-for="(item, index) in endpointList"
            :key="index">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.endpointIcon"
                shape="round"></t-avatar>
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
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>
</template>

<style lang="scss" scoped>
.logBiz__wrapper {}
</style>
