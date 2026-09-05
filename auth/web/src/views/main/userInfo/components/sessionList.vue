<script setup lang="jsx">
import {
  ref,
  onMounted,
} from 'vue';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, ellipsisColumn, copyColumn, opColumn} from '@/utils/tableColumns';

import {
  getMyAppUserAuthorizationPage_api,
  offlineMyAppUserAuthorization_api,
} from '@/api';
onMounted(() => {
  getSystemSessionList()
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
    { colKey: 'loginType', title: '登录方式' },
    { colKey: 'region', title: '地区' },
    { colKey: 'ip', title: 'IP' },
    ellipsisColumn('agent', 'Agent', {width: 220}),
    { colKey: 'os', title: '系统' },
    { colKey: 'app', title: '应用' },
    timeColumn('loginTime', '登录时间'),
    {
      colKey: 'onlineDuration', title: '在线时长(h)', cell: (h, { row }) => {
        return (
          (row?.onlineDuration / 3600).toFixed(2)
        )
      }
    },
    opColumn([
      {content: '下线', onClick: (row) => onOffline(row)},
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
});

// 会话列表
const getSystemSessionList = async () => {
  loading.value = true;
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
    };
    let res = await getMyAppUserAuthorizationPage_api(params);
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
  });
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
        let res = await offlineMyAppUserAuthorization_api(params);
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

</script>
<template>
  <div class="accountComponent">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" clearable placeholder="请输入关键字"></t-input>
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
