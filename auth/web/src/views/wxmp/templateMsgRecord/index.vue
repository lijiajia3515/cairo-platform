<script setup lang="jsx">
import {ref, onMounted, watch} from 'vue';
import {DialogPlugin, MessagePlugin, LoadingPlugin} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import {timeColumn, ellipsisColumn, opColumn} from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';

import {
  getAppList_api,
  getWxmpTemplateMsgList_api,
  getWxmpTemplateMsgRecordPageList_api,
  retryWxmpTemplateMsgRecord_api
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
    {colKey: 'bizId', title: '业务ID'},
    {colKey: 'wxmpProviderId', title: '公众号ID'},
    // {
    //   colKey: 'appId', title: '应用', cell: (h, { row }) => {
    //     return (
    //       <t-space size="small">
    //         {
    //           row?.appIcon ? <t-avatar shape="round" hideOnLoadFailed={true} alt={row?.appName?.slice(0, 2)} size="medium" image={row?.appIcon} /> : null
    //         }
    //         <div style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.appName || null}</div>
    //         <i onClick={() => useCopy(row['appId'])} className={'iconfont icon-fuzhi pick copyIcon'}></i>
    //       </t-space>
    //     )
    //   }
    // },
    ellipsisColumn('openId', 'openId', {width: 200}),
    ellipsisColumn('text', '内容', {width: 240}),
    {
      colKey: '', title: '结果', width: 140, cell: (h, {row}) => {
        return (
            <span
                style={{color: row['success'] ? '#2ba471' : '#d54941'}}>{row['success'] ? '发送成功' : row['reason']}</span>
        )
      }
    },
    {colKey: 'source', title: '来源'},
    opColumn([
      {content: '详情', onClick: (row) => onDetail(row), visible: () => hasPermission('wxmp_template_msg_record.find')},
      {content: '重试', onClick: (row) => onResetFunc(row), visible: () => hasPermission('wxmp_template_msg_record.retry')},
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
      bizId: search.value.bizId,
      success: search.value.success,
    };
    let headers = {
      'app-id': headerData.value.appId
    };
    let res = await getWxmpTemplateMsgRecordPageList_api(params, headers);
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
    onConfirm: async ({e}) => {
      LoadingPlugin(true);
      try {
        let params = {
          msgId: row?.msgId
        };
        let headers = {
          'app-id': headerData.value.appId
        };
        let res = await retryWxmpTemplateMsgRecord_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('操作成功');
          confirmDia.hide();
        }
      } finally {
        LoadingPlugin(false);
      }
    },
    onClose: ({e, trigger}) => {
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
  let res = await getWxmpTemplateMsgList_api(params, headers);
  if (res.code == 'Success') {
    setTemplateList(res?.data || []);
  }
}
</script>


<template>
  <div v-allow="'wxmp_template_msg_record.find'" class="templateMsgRecord__wrapper">
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
      <FilterItem label="公众号模板">
        <t-select filterable v-model="search.bizId" placeholder="请选择公众号模板">
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
    <List @page-change="configs.onPageChange" :configs="configs"/>
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
      <p><span class="label">消息内容：</span>{{ detail?.text }}</p>
      <p><span class="label">跳转链接：</span>{{ detail?.jumpUrl }}</p>
      <p><span class="label">业务参数：</span>{{ detail?.bizArgs }}</p>
      <p><span class="label">openId：</span>{{ detail?.openId }}</p>
      <p><span class="label">模板消息编码：</span>{{ detail?.providerTemplateCode }}</p>
      <p><span class="label">模板消息参数：</span>{{ detail?.providerArgs }}</p>
      <p><span class="label">微信消息ID：</span>{{ detail?.providerMsgId }}</p>
      <p><span class="label">是否成功：</span>{{ detail?.success }}</p>
      <p><span class="label">发送次数：</span>{{ detail?.version }}</p>
      <p v-if="detail?.success == false"><span class="label">失败原因：</span>{{ detail?.reason }}</p>
      <p><span class="label">发送来源：</span>{{ detail?.source }}</p>

    </div>
  </Dialog>
</template>

<style lang="scss" scoped>
.templateMsgRecord__wrapper {
}

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
