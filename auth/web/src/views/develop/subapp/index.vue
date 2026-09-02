<!-- 子应用 -->
<script setup lang="jsx">
import { ref, onMounted, watch, } from 'vue';

import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';

import { useWindowSize } from '@vueuse/core';

import { debounce } from 'lodash';

import List from '@/components/list';
import Dialog from '@/components/dialog';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import UserInfo from '@/components/userInfo';
import { copyColumn, avatarCopyColumn, timeColumn, opColumn, switchColumn } from '@/utils/tableColumns';

import useDict from '@/hooks/useDict';
import useState from '@/hooks/useState';
import UploadImage from '@/components/uploadImage';
import { hasPermission } from '@/plugins/permission';

import {
  getSubappPageList_api,
  modifySubappStatus_api,
  deleteSubapp_api,
  moveSubapp_api,
  createSubapp_api,
  modifySubappInfo_api,
  getAppList_api,
  getEndpointList_api,
} from '@/api';

const allScopes = ref([]);

onMounted(async () => {
  getAppList();
  allScopes.value = await useDict('AccessScope');
});

const { width } = useWindowSize(); // 监听窗口大小
let page = ref(1);
let size = ref(10);
let total = ref(0);
let keyword = ref(null);
let appId = ref(null); // 应用id
let endpointId = ref(null); // 终端id
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('appId', '应用ID'),
    avatarCopyColumn({ colKey: 'appName', title: '应用名称', iconKey: 'appIcon' }),
    copyColumn('endpointId', '终端ID'),
    avatarCopyColumn({ colKey: 'endpointName', title: '终端名称', iconKey: 'endpointIcon' }),
    copyColumn('subappId', '子应用ID'),
    avatarCopyColumn({ colKey: 'subappName', title: '子应用名称', iconKey: 'subappIcon' }),
    {
      colKey: 'scope', title: '准入范围', width: 90, cell: (h, { row }) => {
        const item = allScopes.value.find((item) => item.itemId == row.scope);
        return item?.itemName || '';
      }
    },
    switchColumn({
      api: (params) => {
        const headers = {};
        if (appId.value) {
          headers['app-id'] = appId.value;
        }
        if (endpointId.value) {
          headers['endpoint-id'] = endpointId.value;
        }
        return modifySubappStatus_api(params, headers);
      },
      idKeys: ['id'],
      label: '子应用',
      perm: 'subapp.modify_subapp_status',
      refresh: () => getSubappPage(),
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
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('subapp.modify_subapp_info') },
      { content: '上移', onClick: (row, rowIndex) => onMoveUp(row, rowIndex), visible: () => hasPermission('subapp.move_subapp') },
      { content: '下移', onClick: (row, rowIndex) => onMoveDown(row, rowIndex), visible: () => hasPermission('subapp.move_subapp') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('subapp.delete_subapp') },
    ], { width: 200 }),
  ],
  rowKey: 'id',
  loading: loading,
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getSubappPage();
  }
});
/**
 **************************************************** 用户详情
 */
let userInfoRef = ref(null);
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row) => {
  userInfoRef.value.open();
  setUserDetail(row.metadata.updateUser)
}
const onCloseUserDetail = () => {
  setUserDetail({});
}
// 子应用分页
const getSubappPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      appId: appId.value,
      endpointId: endpointId.value,
    }
    let headers = {};
    if (appId.value) {
      headers['app-id'] = appId.value;
    }
    if (endpointId.value) {
      headers['endpoint-id'] = endpointId.value;
    }
    let res = await getSubappPageList_api(params, headers);
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
  getSubappPage();
}
const onReset = () => {
  page.value = 1;
  keyword.value = null;  appId.value = appList.value[0].appId;
  endpointId.value = endpointList.value[0].endpointId;
  getSubappPage();
}

//创建
const rules = {
  subappId: [
    { required: true, message: '子应用ID必填', type: 'error', trigger: 'change' },
  ],
  subappName: [
    { required: true, message: '子应用名称必填', type: 'error', trigger: 'change' },
  ],
  subappIcon: [
    { required: true, message: '子应用图标必填', type: 'error', trigger: 'change' },
  ],
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  id: null, // 编辑
  subappId: null,
  subappName: null,
  subappIcon: null,
  enabled: null,
  scope: null,
})

const onCreate = () => {
  setVisible(true)
}

const onEdit = (row) => {
  setVisible(true)
  setType('edit')
  if (row.subappIcon) {
    setEditFileList([{
      name: row.subappIcon.split('/')[row.subappIcon.split('/').length - 1],
      url: row.subappIcon
    }])
  }
  form.value.id = row.id
  form.value.subappId = row.subappId
  form.value.subappName = row.subappName
  form.value.scope = row.scope || null
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}

const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    let params = {
      subappId: form.value.subappId,
      subappName: form.value.subappName,
      enabled: form.value.enabled,
      scope: form.value.scope,
    }
    if (fileList.value && fileList.value.length) {
      params['subappIcon'] = fileList.value[0].url;
    }
    let headers = {
      'app-id': appId.value,
      'endpoint-id': endpointId.value,
    };

    if (type.value == 'add') {
      let res = await createSubapp_api(params, headers)
      if (res.code === "Success") {
        MessagePlugin.success('创建成功');
        onClose();
        getSubappPage();
      }
    } else if (type.value == 'edit') {
      params['id'] = form.value.id;
      let res = await modifySubappInfo_api(params, headers)
      if (res.code === "Success") {
        MessagePlugin.success('修改成功');
        onClose();
        getSubappPage();
      }
    }
  }
}

const onClose = () => {
  setType('add');
  setForm({
    id: null, // 编辑
    subappId: null,
    subappName: null,
    subappIcon: null,
    enabled: null,
    websiteUrl: null,
      scope: null,
  })
  setEditFileList([])
  setVisible(false)
}



/**
 * 删除
 */
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
          id: row.id,
          clientId: row.clientId,
        }
        let headers = {};
        if (appId.value) {
          headers['app-id'] = appId.value;
        }
        if (endpointId.value) {
          headers['endpoint-id'] = endpointId.value;
        }
        let res = await deleteSubapp_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getSubappPage();
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

const handleMove = async (params) => {
  let headers = {};
  if (appId.value) {
    headers['app-id'] = appId.value;
  }
  if (endpointId.value) {
    headers['endpoint-id'] = endpointId.value;
  }
  let res = await moveSubapp_api(params, headers)
  if (res.code === "Success") {
    MessagePlugin.success('移动成功');
    getSubappPage()
  }
}

//移动
const onMoveUp = (row, rowIndex) => {
  if (rowIndex === 0) {
    MessagePlugin.error('无法移动');
    return
  }
  let queryParams = {
    moveId1: row.id,
    moveId2: list.value[rowIndex - 1].id
  }
  handleMove(queryParams)
}
const onMoveDown = (row, rowIndex) => {
  if (rowIndex === list.value.length - 1) {
    MessagePlugin.error('无法移动');
    return
  }
  let queryParams = {
    moveId1: row.id,
    moveId2: list.value[rowIndex + 1].id
  }
  handleMove(queryParams)
}



// 应用 列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let res = await getAppList_api({});
  if (res.code == 'Success') {
    appId.value = res?.data[0].appId
    setAppList(res?.data || []);
  }
}
// 终端列表
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: appId.value,
  };
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    endpointId.value = res?.data[0].endpointId
    setEndpointList(res?.data || []);
  }
}

watch(appId, () => {
  getEndpointList();
})

watch(() => endpointId.value, () => {
  if (endpointId.value) {
    initData();
  }
})

const initData = debounce(() => {
  if (appId.value && endpointId.value) {
    getSubappPage();
  }
})
</script>


<template>
  <div class="subapp__wrapper" v-allow="'subapp.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="appId" placeholder="请选择应用">
          <t-option :style="{ height: '40px', width: '100%' }" :label="item.appName" :value="item.appId"
            v-for="(item, index) in appList" :key="index">
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
        <t-select :scroll="{ type: 'virtual' }" filterable v-model="endpointId" placeholder="请选择终端">
          <t-option :style="{ height: '40px', width: '100%' }" :label="item.endpointName"
            :value="item.endpointId" v-for="(item, index) in endpointList" :key="index">
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
      <FilterItem label="关键字">
        <t-input v-model="keyword" placeholder="请输入关键字"></t-input>
      </FilterItem>
      <template #actions>
        <t-button @click="onCreate" v-allow="'subapp.create'">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"></List>
  </div>

  <!-- 创建 编辑 -->
  <Dialog :width="width < 750 ? '100%' : '30%'" top="10vh" @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="subappId" label="子应用ID">
            <t-input v-model="form.subappId"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="subappName" label="子应用名称">
            <t-input v-model="form.subappName"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="准入范围">
            <t-select clearable v-model="form.scope" placeholder="缺省为开放（随终端自动可用）">
              <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in allScopes"
                :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row v-if="type == 'add'">
        <t-col :span="12">
          <t-form-item label="是否启用">
            <t-radio-group v-model="form.enabled">
              <t-radio :value="true">启用</t-radio>
              <t-radio :value="false">禁用</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="图标">
            <UploadImage :disabled="!appId || !endpointId" :appId="appId" :endpointId="endpointId"
              type="public" picType="endpoint-icon" @change="onChangeFiles" :limit="1" :fileList="editFileList">
            </UploadImage>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.subapp__wrapper {
  header {
    box-sizing: border-box;
  }
}
</style>
