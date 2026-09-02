<!-- 第三方账号 -->
<script setup lang="jsx">
import { ref, onMounted } from 'vue';
import { useWindowSize } from '@vueuse/core';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import Dialog from '@/components/dialog';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { ellipsisColumn, avatarCopyColumn, timeColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getSnsProviderPageList_api,
  createSnsProvider_api,
  modifySnsProvider_api,
  deleteSnsProvider_api,
  modifySnsProviderStatus_api,
  getProviderTypeList,
  getProviderPartnerList,
  getAppList_api
} from '@/api';

const providerTypeList = ref([]);
const providerPartnerList = ref([])
onMounted(() => {
  getTableList();
  getTypeList()
  getPartnerList()
  getAppList()
});




const getTypeList = async () => {
  let res = await getProviderTypeList({})
  if (res.code === 'Success') {
    providerTypeList.value = res.data
  }
}

const getPartnerList = async () => {
  let res = await getProviderPartnerList({})
  if (res.code === 'Success') {
    providerPartnerList.value = res.data
  }
}


const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {}
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}

const { width } = useWindowSize(); // 监听窗口大小


let keyword = ref(null);
let appId = ref(null);
let snsTypes = ref([]);
let snsPartners = ref([])
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    {
      colKey: 'snsProviderId', title: '第三方账号认证ID'
    },
    { colKey: 'snsProviderName', title: '名称' },
    avatarCopyColumn({ colKey: 'snsProviderPartnerName', title: '厂商', iconKey: 'snsProviderPartnerIcon' }),
    {
      colKey: 'snsProviderTypeName', title: '类型',
    },

    ellipsisColumn('clientId', 'clientId', { width: 180 }),
    ellipsisColumn('clientSecret', 'clientSecret', { width: 180 }),
    switchColumn({
      api: modifySnsProviderStatus_api,
      idKeys: ['snsProviderId'],
      label: 'SNS提供商',
      perm: 'sns_provider.modify_status',
      refresh: () => getTableList(),
    }),
    { colKey: 'isAutoRegister', title: '开通自动注册', width: 110, cell: (h, { row }) => row['isAutoRegister'] == true ? '是' : (row['isAutoRegister'] == false ? '否' : '') },
    avatarCopyColumn({ colKey: 'metadata.updateUser.nickname', title: '更新人', iconKey: 'metadata.updateUser.accountAvatarUrl', width: 160 }),
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('sns_provider.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('sns_provider.delete') },
    ], { width: 160 }),
  ],
  rowKey: 'appId',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getTableList();
  }
});

/**
 ******************************************************* 列表
 */
const getTableList = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      snsTypes: snsTypes.value,
      snsPartners: snsPartners.value
    }
    if (appId.value) {
      params['appId'] = appId.value;
    }
    let res = await getSnsProviderPageList_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getTableList();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  snsTypes.value = [];
  snsPartners.value = []
  getTableList();
}

const [fileList, setFileList] = useState([])

/**
 * 创建
 */
const rules = {
  snsProviderId: [
    { required: true, message: 'ID必填', type: 'error', trigger: 'blur' },
  ],
  snsProviderName: [
    { required: true, message: '名称必填', type: 'error', trigger: 'blur' },
  ],
  snsProviderType: [
    { required: true, message: '类型必填', type: 'error', trigger: 'change' },
  ],
  snsProviderPartner: [
    { required: true, message: '厂商必填', type: 'error', trigger: 'change' },
  ],
  appId: [
    { required: true, message: 'appId必填', type: 'error', trigger: 'change' },
  ],
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  snsProviderId: '',
  snsProviderName: '',
  snsProviderType: '',
  snsProviderPartner: '',
  appId: '',
  clientId: '',
  clientSecret: '',
  isAutoRegister: '',
  icon: ''
})
const onCreate = () => {
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}

const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        ...form.value
      };
      if (fileList.value.length) {
        params['icon'] = fileList.value[0].url;
      }
      if (type.value == 'add') {
        let res = await createSnsProvider_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getTableList();
        }
      } else {
        let res = await modifySnsProvider_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('修改成功');
          onClose();
          getTableList();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

const onClose = () => {
  setType('add');
  setVisible(false);
  setForm({
    snsProviderId: '',
    snsProviderName: '',
    snsProviderType: '',
    snsProviderPartner: '',
    appId: '',
    clientId: '',
    clientSecret: '',
    isAutoRegister: '',
    icon: '',
  })
  setFileList([])
}


/**
 * 编辑
 */
const onEdit = async (row) => {
  setType('edit');
  setVisible(true);
  form.value.snsProviderId = row.snsProviderId
  form.value.snsProviderName = row.snsProviderName
  form.value.snsProviderType = row.snsProviderTypeId
  form.value.snsProviderPartner = row.snsProviderPartnerId
  form.value.appId = row.appId
  form.value.clientId = row.clientId
  form.value.clientSecret = row.clientSecret
  form.value.isAutoRegister = row.isAutoRegister
  if (row.icon) {
    setFileList([
      {
        url: row.icon,
        name: row.icon.split('/')[row.icon.split('/').length - 1]
      }
    ])
  }
}

// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          snsProviderId: row.snsProviderId,
        }
        let res = await deleteSnsProvider_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          getTableList();
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
  <div v-allow="'sns_provider.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="名称">
        <t-input placeholder="请输入名称" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="应用">
        <t-select clearable filterable v-model="appId">
          <t-option :style="{ height: '40px', width: '100%' }" :disabled="type == 'edit'"
            v-for="(item, index) in appList" :key="index" :label="item.appName" :value="item.appId">
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
      <FilterItem label="类型">
        <t-select v-model="snsTypes" multiple>
          <t-option :label="item.providerTypeName" :value="item.providerTypeId"
            v-for="(item, index) in providerTypeList" :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="厂商">
        <t-select v-model="snsPartners" multiple>
          <t-option :label="item.providerPartnerName" :value="item.providerPartnerId"
            v-for="(item, index) in providerPartnerList" :key="index"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'sns_provider.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"></List>
  </div>


  <!-- 添加 编辑 -->
  <Dialog :width="width < 750 ? '100%' : '45%'" @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="appId" label="应用">
            <t-select clearable filterable v-model="form.appId">
              <t-option
                :style="{ backgroundColor: item.enabled ? 'initial' : '#Ededed', height: '40px', width: '100%' }"
                :disabled="type == 'edit'" v-for="(item, index) in appList" :key="index" :label="item.appName"
                :value="item.appId">
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
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="snsProviderId" label="ID">
            <t-input v-model="form.snsProviderId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="snsProviderName" label="名称">
            <t-input v-model="form.snsProviderName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="snsProviderPartner" label="厂商">
            <t-select filterable v-model="form.snsProviderPartner">
              <t-option :label="item.providerPartnerName" :value="item.providerPartnerId"
                        v-for="(item, index) in providerPartnerList" :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="snsProviderType" label="类型">
            <t-select clearable v-model="form.snsProviderType">
              <t-option :label="item.providerTypeName" :value="item.providerTypeId"
                v-for="(item, index) in providerTypeList" :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="clientId" label="clientId">
            <t-input v-model="form.clientId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="clientSecret" label="clientSecret">
            <t-input v-model="form.clientSecret"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="isAutoRegister" label="是否自动注册">
            <t-radio-group :allowUncheck="true" v-model="form.isAutoRegister">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
      </t-row>
    </t-form>
  </Dialog>


</template>

<style lang="scss" scoped>
.list__wrapper {
  header {
    box-sizing: border-box;
  }
}
</style>
