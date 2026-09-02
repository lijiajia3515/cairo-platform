<script setup lang="jsx">
import { ref, onMounted, watch, nextTick } from 'vue';
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import Dialog from '@/components/dialog';
import AccountInfo from '@/components/accountInfo';
import {
  getAppList_api,
  getTenantList_api,
  getTenantAppList_api,
  getEndpointList_api,
  getSubappList_api,
  getTenantSubappPageList_api,
  createTenantSubapp_api,
  deleteTenantSubapp_api,
  modifyTenantSubappStatus_api
} from '@/api';

onMounted(() => {
  getTenantSubappPage();
  getAppList()
  nextTick(() => {
    getTenantList();
  })
});

let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [loading, setLoading] = useState(false);
const [search, setSearch] = useState({
  enabled: null,
  tenantId: null,
  appId: null,
  endpointId: null,
  subappId: null
})
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    avatarCopyColumn({ colKey: 'tenantName', title: '企业', iconKey: 'tenantIcon', copyKey: 'tenantId' }),
    avatarCopyColumn({ colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId' }),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId' }),
    {
      colKey: 'subappName', title: '子应用名',
    },
    switchColumn({
      api: modifyTenantSubappStatus_api,
      idKeys: ['tenantId', 'appId', 'endpointId', 'subappId'],
      label: '企业子应用',
      perm: 'tenant_subapp.modify_tenant_subapp_status',
      refresh: () => getTenantSubappPage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新账号', width: 200, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" imageProps={{ lazy: true }} hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} class="pick" imageProps={{ lazy: true }} size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div onClick={() => onWatchAccountInfo(row?.metadata?.updateAccount || {})} style={{ height: '100%', display: 'flex', alignItems: 'center' }} class="pick">{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('tenant_subapp.delete_tenant_subapp') },
    ], { width: 160 })
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
    getTenantSubappPage();
  }
});

const tenantAppList_search = ref([]);
const endpointList_search = ref([]); // 终端
const subappList_search = ref([]); // 子应用

const getAppList = async () => {
  let res = await getAppList_api({ Scopes: ["tenant"] });
  if (res.code == 'Success') {
    tenantAppList_search.value = res?.data || [];
  }
}


watch(() => search.value.appId, async () => {
  if (search.value.appId) {
    let params = {
      appId: search.value.appId,
      scopeIds: ["tenant"]
    };
    let res = await getEndpointList_api(params);
    if (res.code == 'Success') {
      endpointList_search.value = res?.data || [];
      setSearch({
        ...search.value,
        endpointId: null,
        subappId: null,
      })
    }
  } else {
    endpointList_search.value = [];
    setSearch({
      ...search.value,
      endpointId: null,
      subappId: null,
    })
  }
})

watch(() => search.value.endpointId, async () => {
  if (search.value.endpointId) {
    let headers = {};
    if (search.value.appId) {
      headers['app-id'] = search.value.appId;
    }
    if (search.value.endpointId) {
      headers['endpoint-id'] = search.value.endpointId;
    }
    let res = await getSubappList_api({}, headers);
    if (res.code == 'Success') {
      subappList_search.value = res?.data || [];
      setSearch({
        ...search.value,
        subappId: null,
      })
    }
  } else {
    subappList_search.value = [];
    setSearch({
      ...search.value,
      subappId: null,
    })
  }
})



// 企业子应用分页
const getTenantSubappPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      enabled: search.value.enabled,
      tenantId: search.value.tenantId,
      appId: search.value.appId,
      endpointId: search.value.endpointId,
      subappId: search.value.subappId,
    };
    let res = await getTenantSubappPageList_api(params);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total) || 0;
    }
  } finally {
    setLoading(false);
  }
}
const onSearch = () => {
  page.value = 1;
  getTenantSubappPage();
}
const onReset = () => {
  page.value = 1;
  setSearch({
    enabled: null,
    tenantId: null,
    appId: null,
    endpointId: null,
    subappId: null
  })
  getTenantSubappPage();
}


// 创建
const rules = {
  tenantId: [
    { required: true, message: '企业必填', type: 'error', trigger: 'blur' },
    { required: true, message: '企业必填', type: 'error', trigger: 'change' },
  ],
  appId: [
    { required: true, message: '应用必填', type: 'error', trigger: 'blur' },
    { required: true, message: '应用必填', type: 'error', trigger: 'change' },
  ],
  endpointId: [
    { required: true, message: '终端必填', type: 'error', trigger: 'blur' },
    { required: true, message: '终端必填', type: 'error', trigger: 'change' },
  ],
  subappId: [
    { required: true, message: '子应用必填', type: 'error', trigger: 'blur' },
    { required: true, message: '子应用必填', type: 'error', trigger: 'change' },
  ],
  enabled: [
    { required: true, message: '状态必填', type: 'error', trigger: 'blur' },
    { required: true, message: '状态必填', type: 'error', trigger: 'change' },
  ]
};
let formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [form, setForm] = useState({
  tenantId: null,
  appId: null,
  endpointId: null,
  subappId: null,
  enabled: true,
})
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onConfirm = async () => {
  let validate = await formRef.value.validate();
  if (validate == true) {
    let params = {
      ...form.value
    };
    if (type.value == 'add') {
      let res = await createTenantSubapp_api(params);
      if (res.code == 'Success') {
        onClose();
        MessagePlugin.success('创建成功');
        getTenantSubappPage();
      }
    }
  }
}

const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    tenantId: null,
    appId: null,
    endpointId: null,
    subappId: null,
    enabled: true,
  })
  setTenantAppList([])
  setEndpointList([])
  setSubappList([])
}



// 企业列表
const [tenantList, setTenantList] = useState([]);
const getTenantList = async () => {
  let res = await getTenantList_api({});
  if (res.code == 'Success') {
    setTenantList(res?.data || []);
  }
}

watch(() => form.value.tenantId, () => {
  setForm({
    ...form.value,
    appId: null,
  })
  setEndpointList([])
  setSubappList([])
  if (form.value.tenantId) {
    getTenantAppList();
  }
})

// 企业应用
const [tenantAppList, setTenantAppList] = useState([]);
const getTenantAppList = async () => {
  let params = {
    tenantId: form.value.tenantId
  }
  let res = await getTenantAppList_api(params);
  if (res.code == 'Success') {
    setTenantAppList(res?.data || []);
  }
}

// 终端列表
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: form.value.appId,
    scopeIds: ["tenant"]
  }
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}


// 子应用列表
const [subappList, setSubappList] = useState([]);
const getSubappList = async () => {
  let headers = {};
  if (form.value.appId) {
    headers['app-id'] = form.value.appId;
  }
  if (form.value.endpointId) {
    headers['endpoint-id'] = form.value.endpointId;
  }
  let res = await getSubappList_api({}, headers);
  if (res.code == 'Success') {
    setSubappList(res?.data || []);
  }
}

watch(() => form.value.appId, () => {
  setForm({
    ...form.value,
    endpointId: null,
    subappId: null
  })
  setSubappList([])
  if (form.value.appId) {
    getEndpointList();
  }
})

watch(() => form.value.endpointId, () => {
  setForm({
    ...form.value,
    subappId: null
  })
  if (form.value.endpointId) {
    getSubappList();
  }
})



// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '提示',
    body: '是否继续删除?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      let params = {
        tenantId: row?.tenantId,
        appId: row?.appId,
        endpointId: row?.endpointId,
        subappId: row?.subappId
      };
      let res = await deleteTenantSubapp_api(params);
      if (res.code == 'Success') {
        confirmDia.hide();
        MessagePlugin.success('删除成功');
        // 删除的时候判断
        if (list.value && list.value.length == 1 && page.value > 1) {
          page.value = page.value - 1;
        }
        getTenantSubappPage();
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}

let accountInfoRef = ref(null); // 账号详情
const [accountDetail, setAccountDetail] = useState({});
const onWatchAccountInfo = (data) => {
  accountInfoRef.value.open();
  setAccountDetail(data)
}
const onCloseAccountInfo = () => {
  setAccountDetail({});
}
</script>


<template>
  <div class="tenantSubapp__wrapper" v-allow="'tenant_subapp.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="企业">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="search.tenantId" placeholder="请选择企业">
          <t-option v-for="(item, index) in tenantList" :label="item.tenantName" :value="item.tenantId" :key="index"
            :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.tenantName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="tenantList.filter(item => item.tenantId == value)[0]?.icon" shape="round"></t-avatar>
                {{ tenantList.filter(item => item.tenantId == value)[0]?.tenantName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="search.appId" placeholder="请选择应用">
          <t-option v-for="(item, index) in tenantAppList_search" :label="item.appName" :value="item.appId"
            :key="index" :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.appIcon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="tenantAppList_search.filter(item => item.appId == value)[0]?.appIcon"
                  shape="round"></t-avatar>
                {{ tenantAppList_search.filter(item => item.appId == value)[0]?.appName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="终端">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="search.endpointId"
          placeholder="请选择终端">
          <t-option v-for="(item, index) in endpointList_search" :label="item.endpointName"
            :value="item.endpointId" :key="index" :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="endpointList_search.filter(item => item.endpointId == value)[0]?.icon"
                  shape="round"></t-avatar>
                {{ endpointList_search.filter(item => item.endpointId == value)[0]?.endpointName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="子应用">
        <t-select v-model="search.subappId" filterable clearable placeholder="请选择子应用">
          <t-option :label="item.subappName" :value="item.subappId" v-for="(item, index) in subappList_search"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="search.enabled" placeholder="请选择状态">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'tenant_subapp.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <!-- 创建 -->
  <Dialog @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <t-form ref="formRef" :rules="rules" :data="form" labelAlign="left" labelWidth="80px">
      <t-row>
        <t-col :span="12">
          <t-form-item name="tenantId" label="企业">
            <t-select v-model="form.tenantId" clearable style="width: 100%;">
              <t-option v-for="(item, index) in tenantList" :label="item.tenantName" :value="item.tenantId" :key="index"
                :style="{ height: '40px', width: '100%' }">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.tenantName }}</span>
                </div>
              </t-option>
              <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                      :image="tenantList.filter(item => item.tenantId == value)[0]?.icon" shape="round"></t-avatar>
                    {{ tenantList.filter(item => item.tenantId == value)[0]?.tenantName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="appId" label="企业应用">
            <t-select v-model="form.appId" clearable style="width: 100%;">
              <t-option v-for="(item, index) in tenantAppList" :label="item.appName" :value="item.appId" :key="index"
                :style="{ height: '40px', width: '100%' }">
                <div style="display: flex;align-items: center;width: 100%;">
                  <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.appIcon" shape="round"></t-avatar>
                  <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
                </div>
              </t-option>
              <template #valueDisplay="{ value }">
                <template v-if="value">
                  <t-space>
                    <t-avatar :imageProps="{ lazy: true }" size="20px"
                      :image="tenantAppList.filter(item => item.appId == value)[0]?.appIcon" shape="round"></t-avatar>
                    {{ tenantAppList.filter(item => item.appId == value)[0]?.appName }}
                  </t-space>
                </template>
              </template>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="endpointId" label="终端">
            <t-select v-model="form.endpointId" clearable style="width: 100%;">
              <t-option v-for="(item, index) in endpointList" :label="item.endpointName"
                :value="item.endpointId" :key="index" :style="{ height: '40px', width: '100%' }">
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
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="subappId" label="子应用">
            <t-select v-model="form.subappId" clearable style="width: 100%;">
              <t-option v-for="(item, index) in subappList" :key="index" :label="item.subappName"
                :value="item.subappId">
              </t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>

      <t-row>
        <t-col :span="12">
          <t-form-item name="enabled" label="状态">
            <t-radio-group v-model="form.enabled">
              <t-radio :value="true">启用</t-radio>
              <t-radio :value="false">禁用</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
    </t-form>
  </Dialog>

  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>
</template>

<style lang="scss" scoped>
.tenantSubapp__wrapper {}
</style>
