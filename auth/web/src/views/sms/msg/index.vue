<script setup lang="jsx">
defineOptions({ name: 'manage-sms-msg' })

import { ref, onMounted, watch } from 'vue';
import { DialogPlugin, MessagePlugin, LoadingPlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import { timeColumn, ellipsisColumn, avatarCopyColumn, opColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getAppList_api,
  getSmsMsgPageList_api,
  getSmsTemplateList_api,
  retrySmsMsg_api
} from '@/api';

onMounted(() => {
  getAppList();

});

let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [search, setSearch] = useState({
  keyword: null,
  phoneNumber: null,
  bizId: null,
  success: null,
})
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    timeColumn('time', '时间'),
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    { colKey: 'bizId', title: '业务ID' },
    { colKey: 'phoneNumber', title: '手机号' },
    ellipsisColumn('text', '内容', { width: 240 }),
    {
      colKey: 'sendResult', title: '结果', width: 140, cell: (h, { row }) => {
        return (
          <span style={{ color: row['success'] ? '#2ba471' : '#d54941' }}>{row['success'] ? '发送成功' : row['reason']}</span>
        )
      }
    },
    opColumn([
      { content: '详情', onClick: (row) => onDetail(row), visible: () => hasPermission('sms_msg.read') },
      { content: '重试', onClick: (row) => onResetFunc(row), visible: () => hasPermission('sms_msg.retry_sms_msg') },
    ])
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
    getSmsMsgPage();
  }
});

// 短信消息分页列表
const getSmsMsgPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: search.value.keyword,
      phoneNumber: search.value.phoneNumber,
      bizId: search.value.bizId,
      success: search.value.success,
    };
    let headers = {
      'app-id': headerData.value.appId
    };
    let res = await getSmsMsgPageList_api(params, headers);
    if (res.code === 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getSmsMsgPage();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    keyword: null,
    phoneNumber: null,
    bizId: null,
    success: null,
  })
  getSmsMsgPage();
}


/**
 * 重试
 */
const onResetFunc = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否继续?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          msgId: row?.msgId
        };
        let headers = {
          'app-id': headerData.value.appId
        };
        let res = await retrySmsMsg_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('操作成功');
          confirmDia.hide();
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



const [headerData, setHeaderData] = useState({
  appId: null,
});
// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
    if (appList.value.length > 0) {
      setHeaderData({
        ...headerData.value,
        appId: appList.value[0].appId
      });
    }
  }
}
watch(() => headerData.value.appId, () => {
  if (headerData.value.appId) {
    getSmsMsgPage();
    getSmsTemplateList();
  }
})




// 详情
const [visibleDetail, setVisibleDetail] = useState(false);
const [detail, setDetail] = useState({});
const onDetail = (row) => {
  setVisibleDetail(true);
  setDetail(row)
}
const onCloseDetail = () => {
  setVisibleDetail(false);
  setDetail({});
}




// 短信模板列表
const [templateList, setTemplateList] = useState([]);
const getSmsTemplateList = async () => {
  let params = {};
  let headers = {
    'app-id': headerData.value.appId
  };
  let res = await getSmsTemplateList_api(params, headers);
  if (res.code == 'Success') {
    setTemplateList(res?.data || []);
  }
}
</script>


<template>
  <div v-allow="'sms_msg.read'" class="msg__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select filterable v-model="headerData.appId" placeholder="应用">
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
      <FilterItem label="关键字">
        <t-input v-model="search.keyword" placeholder="请输入关键字查询"></t-input>
      </FilterItem>
      <FilterItem label="手机号">
        <t-input v-model="search.phoneNumber" placeholder="请输入手机号查询"></t-input>
      </FilterItem>
      <FilterItem label="短信模板">
        <t-select filterable v-model="search.bizId" placeholder="请选择短信模板">
          <t-option v-for="(item, index) in templateList" :key="index" :value="item.bizId"
            :label="item.templateName"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="结果">
        <t-select filterable v-model="search.success" placeholder="请选择结果">
          <t-option :value="true" label="发送成功"></t-option>
          <t-option :value="false" label="发送失败"></t-option>
        </t-select>
      </FilterItem>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <!-- 详情 -->
  <Dialog @close="onCloseDetail" :visible="visibleDetail" :confirmBtn="null" :cancelBtn="null">
    <template #title>详情</template>
    <div class="detailBox">
      <p><span class="label">消息ID：</span>{{ detail?.msgId }}</p>
      <p><span class="label">时间：</span>{{ detail?.time }}</p>
      <p><span class="label">应用ID：</span>{{ detail?.appId }}</p>
      <p><span class="label">应用名称：</span>{{ detail?.appName }}</p>
      <p><span class="label">业务ID：</span>{{ detail?.bizId }}</p>
      <p><span class="label">手机号：</span>{{ detail?.phoneNumber }}</p>
      <p><span class="label">消息内容：</span>{{ detail?.text }}</p>
      <p><span class="label">业务参数：</span>{{ detail?.bizArgs }}</p>
      <p><span class="label">短信厂商签名：</span>{{ detail?.providerSign }}</p>
      <p><span class="label">短信厂商模板：</span>{{ detail?.providerTemplateCode }}</p>
      <p><span class="label">短信厂商参数：</span>{{ detail?.providerArgs }}</p>
      <p><span class="label">短信厂商消息ID：</span>{{ detail?.providerMsgId }}</p>
      <p><span class="label">是否成功：</span>{{ detail?.success }}</p>
      <p><span class="label">发送次数：</span>{{ detail?.version }}</p>
      <p v-if="detail?.success == false"><span class="label">失败原因：</span>{{ detail?.reason }}</p>
    </div>
  </Dialog>
</template>

<style lang="scss" scoped>
.msg__wrapper {}

.detailBox {

  p {
    padding: 5px 0;
    font-weight: 700;
    color: rgba(0, 0, 0, 0.9);
    letter-spacing: 1px;
    display: flex;

    .label {
      display: inline-block;
      min-width: 100px;
      text-align: right;
    }
  }
}
</style>
