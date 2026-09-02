<!-- 子应用版本 -->
<script setup lang="jsx">
import { ref, watch, onMounted, nextTick } from 'vue';
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
import UserInfo from '@/components/userInfo';
import { timeColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getSubappVersionPageList_api,
  createSubappVersion_api,
  modifySubappVersion_api,
  modifySubappVersionStatus_api,
  syncSubappVersion_api,
  deleteSubappVersion_api,
  getSubappVersionList_api,
  getAppList_api, getEndpointList_api, getSubappList_api,
} from '@/api';

import {
  debounce,
} from 'lodash';

// 头部参数
const [headerParams, setHeaderParams] = useState({
  appId: null,
  endpointId: null,
  subappId: null
})

watch(() => headerParams.value.appId, () => {
  if (headerParams.value.appId) {
    getEndpointList();
  } else {
    setSubappList([]);
    setHeaderParams({
      ...headerParams.value,
      endpointId: null,
      subappId: null
    });
  }
});
watch(() => headerParams.value.endpointId, () => {
  if (headerParams.value.endpointId) {
    getSubappList();
  } else {
    setSubappList([]);
    setHeaderParams({
      ...headerParams.value,
      subappId: null
    });
  }

});

watch(() => headerParams.value.subappId, () => {
  if (headerParams.value.subappId) {
    initData();
  }
})

const initData = debounce(() => {
  if (headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId) {
    getSubappVersionList();
  }
})

onMounted(() => {
  getAppList();
});


/**
 * 子应用列表
 */
const [subappList, setSubappList] = useState([]);
const getSubappList = async () => {
  let headers = {};
  headers['app-id'] = headerParams.value.appId;
  headers['endpoint-id'] = headerParams.value.endpointId;
  let res = await getSubappList_api({}, headers);
  if (res.code == 'Success' && res.data) {
    setSubappList(res?.data);
    setHeaderParams({
      ...headerParams.value,
      subappId: res?.data[0]?.subappId || null
    });
  } else {
    setSubappList([]);
    setHeaderParams({
      ...headerParams.value,
      subappId: null
    });
  }
}


/**
 * 终端列表
 */
const [terminalList, setTerminalList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: headerParams.value.appId
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success' && res.data) {
    setTerminalList(res?.data);
    setHeaderParams({
      ...headerParams.value,
      endpointId: res?.data[0]?.endpointId || null
    });
  } else {
    setTerminalList([]);
    setHeaderParams({
      ...headerParams.value,
      endpointId: null,
    })
  }
}

/**
 * app列表
 */
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let res = await getAppList_api({});
  if (res.code == 'Success' && res.data) {
    setAppList(res?.data);
    setHeaderParams({
      ...headerParams.value,
      appId: res?.data[0]?.appId || null
    });
  } else {
    setAppList([]);
    setHeaderParams({
      ...headerParams.value,
      appId: null
    })
  }
}


let keyword = ref(null);
let enabled = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    {
      colKey: 'subappId', title: '子应用ID',
    },
    {
      colKey: 'subappName', title: '子应用名称',
    },
    {
      colKey: 'subappVersion', title: '子应用版本号',
    },
    {
      colKey: 'subappRemark', title: '子应用备注',
    },
    switchColumn({
      api: modifySubappVersionStatus_api,
      idKeys: ['subappId', 'subappVersion'],
      label: '子应用版本',
      perm: 'subapp_version.modify_subapp_version_status',
      refresh: () => getSubappVersionList(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ?
                <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)}
                  hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)}
                  size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                  row?.metadata?.updateUser?.nickname ?
                    <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)}
                      size="medium">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center'
            }}>{row?.metadata?.updateUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('subapp_version.modify_subapp_version_info') },
      { content: '同步', onClick: (row) => onSync(row), visible: () => hasPermission('subapp_version.sync_subapp_version') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('subapp_version.delete_subapp_version') },
    ], { width: 200 }),
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
    getSubappVersionList();
  }
});

/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null); // 用户详情
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
const onCloseUserDetail = () => {
  setUserDetail({});
}

/**
 ******************************************************* 应用列表
 */
const getSubappVersionList = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      enabled: enabled.value,
      subappId: headerParams.value.subappId
    }
    let headers = {};
    if (headerParams.value.appId) {
      headers['app-id'] = headerParams.value.appId;
    }
    if (headerParams.value.endpointId) {
      headers['endpoint-id'] = headerParams.value.endpointId;
    }
    let res = await getSubappVersionPageList_api(params, headers);
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
  getSubappVersionList();
}

const onReset = () => {
  page.value = 1;
  keyword.value = null;
  enabled.value = null;
  setHeaderParams({
    ...headerParams.value,
    appId: appList.value[0]?.appId || null,
    endpointId: terminalList.value[0]?.endpointId || null
  });
  getSubappVersionList();
}

/**
 * 创建
 */
const rules = {
  subappVersion: [
    { required: true, message: '子应用版本必填', type: 'error', trigger: 'blur' },
  ],
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  subappId: null,
  subappVersion: null,
  subappRemark: null,
})
const onCreate = () => {
  if (!headerParams.value.endpointId || !headerParams.value.subappId) {
    MessagePlugin.error('终端和子应用不能为空');
    return
  }
  setType('add');
  setVisible(true);
  form.value.subappId = headerParams.value.subappId
  formRef.value.clearValidate();
}

const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        subappId: form.value.subappId,
        subappVersion: form.value.subappVersion,
        subappRemark: form.value.subappRemark,
      };

      let headers = {
        'app-id': headerParams.value.appId,
        'endpoint-id': headerParams.value.endpointId,
      };
      if (type.value == 'add') {
        let res = await createSubappVersion_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getSubappVersionList();
        }
      } else {
        let res = await modifySubappVersion_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('修改成功');
          onClose();
          getSubappVersionList();
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
    subappVersion: null,
    subappRemark: null,
  })
}

/**
 * 编辑
 */
const onEdit = async (row) => {
  setType('edit');
  setVisible(true);
  setForm({
    subappId: row.subappId,
    subappVersion: row.subappVersion,
    subappRemark: row.subappRemark,
  });
  formRef.value.clearValidate();
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
          subappId: row.subappId,
          subappVersion: row.subappVersion,
        }
        let headers = {};
        if (headerParams.value.appId) {
          headers['app-id'] = headerParams.value.appId;
        }
        if (headerParams.value.endpointId) {
          headers['endpoint-id'] = headerParams.value.endpointId;
        }
        let res = await deleteSubappVersion_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getSubappVersionList();
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

//同步
const [syncAppId, setSyncAppId] = useState(null); // 应用
const [syncEndpointId, setSyncEndpointId] = useState(null); // 终端
const [syncId, setSyncId] = useState(null); // 当前同步的子应用ID
const [syncName, setSyncName] = useState(null); // 当前同步的子应用名称
const [syncVersion, setSyncVersion] = useState(null); // 当前同步的子应用版本
const [newSyncSubappId, setNewSyncSubappId] = useState(null); // 新选择的子应用ID
const [newSyncSubappVersion, setNewSyncSubappVersion] = useState(null); // 新选择的子应用ID
const [visibleSync, setVisibleSync] = useState(false);
const onSync = (row) => {
  getSyncAppList()
  nextTick(() => {
    setVisibleSync(true);
    setSyncName(row.subappName);
    setSyncId(row.subappId);
    setSyncVersion(row.subappVersion);
  })

}
const onCloseSync = () => {
  setVisibleSync(false);
  setSyncName(null);
  setSyncId(null);
  setSyncVersion(null);
  setNewSyncSubappId(null);
  setNewSyncSubappVersion(null)
  setSyncAppId(null);
  setSyncEndpointId(null);
}
const onSubmitSync = async () => {
  if (!newSyncSubappVersion.value) {
    MessagePlugin.error('请选择子应用版本');
    return;
  }
  LoadingPlugin(true);
  try {
    let params = {
      changeSubappId: syncId.value,
      changeSubappVersion: syncVersion.value,
      sourceSubappId: newSyncSubappId.value,
      sourceSubappVersion: newSyncSubappVersion.value,
    };
    let headers = {};
    headers['app-id'] = syncAppId.value;
    headers['endpoint-id'] = syncEndpointId.value;
    let res = await syncSubappVersion_api(params, headers);
    if (res.code == 'Success') {
      MessagePlugin.success('同步成功');
      onCloseSync();
      getSubappVersionList();
    }
  } finally {
    LoadingPlugin(false);
  }
}

/**
 * app列表
 */
const [syncAppList, setSyncAppList] = useState([]);
const getSyncAppList = async () => {
  let res = await getAppList_api({});
  if (res.code == 'Success' && res.data) {
    setSyncAppList(res?.data);
  } else {
    setSyncAppList([]);
  }
}

/**
 * 终端列表
 */
const [syncTerminalList, setSyncTerminalList] = useState([]);
const getSyncTerminalList = async () => {
  let params = {
    appId: syncAppId.value
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success' && res.data) {
    setSyncTerminalList(res?.data);
  } else {
    setSyncTerminalList([]);

  }
}

/**
 * 子应用列表
 */
const [syncSubappList, setSyncSubappList] = useState([]);
const getSyncSubappList = async () => {
  let headers = {};
  headers['app-id'] = syncAppId.value;
  headers['endpoint-id'] = syncEndpointId.value;
  let res = await getSubappList_api({}, headers);
  if (res.code == 'Success' && res.data) {
    setSyncSubappList(res?.data);
  } else {
    setSyncSubappList([]);
  }
}

/**
 * 子应用版本列表
 */
const [syncSubappVersionList, setSyncSubappVersionList] = useState([]);
const getSyncSubappVersionList = async () => {
  let headers = {};
  headers['app-id'] = syncAppId.value;
  headers['endpoint-id'] = syncEndpointId.value;
  let res = await getSubappVersionList_api({ subappId: newSyncSubappId.value }, headers);
  if (res.code == 'Success' && res.data) {
    setSyncSubappVersionList(res?.data);
  } else {
    setSyncSubappVersionList([]);
  }
}

watch(syncAppId, () => {
  setSyncEndpointId(null);
  setNewSyncSubappId(null);
  if (syncAppId.value) {
    // 获取终端
    getSyncTerminalList();
  } else {
    setSyncTerminalList([]);
  }
});

watch(syncEndpointId, () => {
  setNewSyncSubappId(null);
  setNewSyncSubappVersion(null)
  if (syncEndpointId.value) {
    // 获取子应用
    getSyncSubappList();
  } else {
    setSyncSubappList([]);
  }
});

watch(newSyncSubappId, () => {
  setNewSyncSubappVersion(null)
  if (newSyncSubappId.value) {
    // 获取子应用
    getSyncSubappVersionList();
  } else {
    setSyncSubappVersionList([]);
  }
});
</script>


<template>
  <div class="subappVersion__wrapper" v-allow="'subapp_version.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input clearable placeholder="请输入备注/版本号" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="enabled">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerParams.appId">
          <t-option :label="item.appName" :value="item.appId" v-for="(item, index) in appList" :key="index"
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
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="headerParams.endpointId">
          <t-option :label="item.endpointName" :value="item.endpointId" v-for="(item, index) in terminalList"
            :key="index" :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="terminalList.filter(item => item.endpointId == value)[0]?.icon"
                  shape="round"></t-avatar>
                {{ terminalList.filter(item => item.endpointId == value)[0]?.endpointName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <FilterItem label="子应用">
        <t-select v-model="headerParams.subappId">
          <t-option :label="item.subappName" :value="item.subappId" v-for="(item, index) in subappList"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button @click="onCreate" v-allow="'subapp_version.create'">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"></List>
  </div>


  <!-- 添加 编辑 -->
  <Dialog width="30%" @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="subappVersion" label="子应用版本">
            <t-input v-model="form.subappVersion" :disabled="type == 'edit'"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="subappRemark" label="备注">
            <t-textarea v-model="form.subappRemark" />
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>


  <!-- 同步 -->
  <Dialog @confirm="onSubmitSync" @close="onCloseSync" :visible="visibleSync">
    <template #title>子应用版本同步：{{ syncName }}</template>
    <t-form-item label="应用">
      <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="syncAppId" placeholder="请选择应用">
        <t-option :style="{ height: '40px', width: '100%' }" :label="item.appName" :value="item.appId"
          v-for="(item, index) in syncAppList" :key="index">
          <div style="display: flex;align-items: center;width: 100%;">
            <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
            <span style="display: inline-block;marginLeft:10px;">{{ item.appName }}</span>
          </div>
        </t-option>
        <template #valueDisplay="{ value }">
          <template v-if="value">
            <t-space>
              <t-avatar :imageProps="{ lazy: true }" size="20px"
                :image="syncAppList.filter(item => item.appId == value)[0]?.icon" shape="round"></t-avatar>
              {{ syncAppList.filter(item => item.appId == value)[0]?.appName }}
            </t-space>
          </template>
        </template>
      </t-select>
    </t-form-item>
    <t-form-item label="终端">
      <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="syncEndpointId" placeholder="请选择终端">
        <t-option :style="{ height: '40px', width: '100%' }" :label="item.endpointName" :value="item.endpointId"
          v-for="(item, index) in syncTerminalList" :key="index">
          <div style="display: flex;align-items: center;width: 100%;">
            <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
            <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
          </div>
        </t-option>
        <template #valueDisplay="{ value }">
          <template v-if="value">
            <t-space>
              <t-avatar :imageProps="{ lazy: true }" size="20px"
                :image="syncTerminalList.filter(item => item.endpointId == value)[0]?.icon" shape="round"></t-avatar>
              {{ syncTerminalList.filter(item => item.endpointId == value)[0]?.endpointName }}
            </t-space>
          </template>
        </template>
      </t-select>
    </t-form-item>
    <t-form-item label="子应用">
      <t-select v-model="newSyncSubappId">
        <t-option :label="item.subappName" :value="item.subappId" v-for="(item, index) in syncSubappList"
          :key="index"></t-option>
      </t-select>
    </t-form-item>
    <t-form-item label="子应用版本">
      <t-select v-model="newSyncSubappVersion">
        <t-option :label="item.subappVersion" :value="item.subappVersion"
          v-for="(item, index) in syncSubappVersionList" :key="index"></t-option>
      </t-select>
    </t-form-item>
  </Dialog>


  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.subappVersion__wrapper {
  header {
    box-sizing: border-box;
  }
}
</style>
