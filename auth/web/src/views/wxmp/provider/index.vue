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
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import Dialog from '@/components/dialog';
import { ellipsisColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';


import {
  getWxmpProviderPageList_api,
  createWxmpProvider_api,
  modifyWxmpProvider_api,
  deleteWxmpProvider_api,
  modifyWxmpProviderStatus_api,
} from '@/api';

// 
onMounted(() => {
  getTableList();
  // getWxmpProviderList()
});

const { width } = useWindowSize(); // 监听窗口大小


let keyword = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(true);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'wxmpProviderId', title: '公众号ID' },
    { colKey: 'wxmpProviderName', title: '名称' },
    { colKey: 'wxmpAppId', title: 'appid' },
    ellipsisColumn('wxmpSecret', 'appsecret', { width: 180 }),
    ellipsisColumn('wxmpToken', 'token', { width: 180 }),
    ellipsisColumn('wxmpAesKey', 'aeskey', { width: 180 }),
    switchColumn({
      api: modifyWxmpProviderStatus_api,
      idKeys: ['wxmpProviderId'],
      label: '公众号提供商',
      perm: 'wxmp_provider.modify_status',
      refresh: () => getTableList(),
    }),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('wxmp_provider.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('wxmp_provider.delete') },
    ])
  ],
  loading: loading,
  rowKey: 'appId',
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
    }
    let res = await getWxmpProviderPageList_api(params);
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
  getTableList();
}

/**
 * 创建
 */
const rules = {
  wxmpProviderId: [
    { required: true, message: '公众号Id必填', type: 'error', trigger: 'blur' },
  ],
  wxmpProviderName: [
    { required: true, message: '公众号名称必填', type: 'error', trigger: 'blur' },
  ],
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  wxmpProviderId: '',
  wxmpProviderName: '',
  wxmpAppId: '',
  wxmpSecret: '',
  wxmpToken: '',
  wxmpAesKey: ''
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
        wxmpProviderId: form.value.wxmpProviderId,
        wxmpProviderName: form.value.wxmpProviderName,
        wxmpAppId: form.value.wxmpAppId,
        wxmpSecret: form.value.wxmpSecret,
        wxmpToken: form.value.wxmpToken,
        wxmpAesKey: form.value.wxmpAesKey,
      };
      if (type.value == 'add') {
        let res = await createWxmpProvider_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getTableList();
        }
      } else {
        let res = await modifyWxmpProvider_api(params);
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
    wxmpProviderId: '',
    wxmpProviderName: '',
    wxmpAppId: '',
    wxmpSecret: '',
    wxmpToken: '',
    wxmpAesKey: ''
  })
}


/**
 * 编辑
 */
const onEdit = async (row) => {
  setType('edit');
  setVisible(true);
  form.value.wxmpProviderId = row.wxmpProviderId
  form.value.wxmpProviderName = row.wxmpProviderName
  form.value.wxmpAppId = row.wxmpAppId
  form.value.wxmpSecret = row.wxmpSecret
  form.value.wxmpToken = row.wxmpToken
  form.value.wxmpAesKey = row.wxmpAesKey
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
          wxmpProviderId: row.wxmpProviderId,
        }
        let res = await deleteWxmpProvider_api(params);
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
  <div v-allow="'wxmp_provider.find'" class="provider__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input placeholder="关键字" v-model="keyword"></t-input>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'wxmp_provider.create'" @click="onCreate">创建</t-button>
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
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpProviderId" label="公众号ID">
            <t-input v-model="form.wxmpProviderId" :disabled="type == 'edit'"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpProviderName" label="公众号名称">
            <t-input v-model="form.wxmpProviderName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpAppId" label="appid">
            <t-input v-model="form.wxmpAppId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpSecret" label="appsecret">
            <t-input v-model="form.wxmpSecret"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpToken" label="token">
            <t-input v-model="form.wxmpToken"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="wxmpAesKey" label="aeskey">
            <t-input v-model="form.wxmpAesKey"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

</template>

<style lang="scss" scoped>
.provider__wrapper {}
</style>
