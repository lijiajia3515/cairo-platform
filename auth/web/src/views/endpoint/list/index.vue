<!-- 终端 -->
<script setup lang="jsx">
import {ref, reactive, onMounted, nextTick} from 'vue';
import {useRouter} from 'vue-router';
import {useWindowSize} from '@vueuse/core';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';


import useState from '@/hooks/useState';
import useDict from '@/hooks/useDict';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, copyColumn, avatarCopyColumn, opColumn, switchColumn} from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';

import {
  getEndpointPageList_api,
  createEndpoint_api,
  getAppList_api,
  modifyEndpointInfo_api,
  modifyEndpointStatus_api,
  deleteEndpoint_api,
} from '@/api';

const endpointTypes = ref([]);
const endpointScopes = ref([]);

onMounted(() => {
  getEndpointPage();

  nextTick(async () => {
    getAppList();

    // 获取字典
    endpointTypes.value = await useDict('EndpointType');
    endpointScopes.value = await useDict('AccessScope');
  })
});

const {width} = useWindowSize(); // 监听窗口大小(Dialog 宽度自适应)

let keyword = ref(null); // 关键字(终端名称)
let appId = ref(null);
let enabled = ref(null); // 状态
let typeIds = ref([]);
let scopeIds = ref([]);
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    copyColumn('appId', '应用ID'),
    avatarCopyColumn({colKey: 'appName', title: '应用名称', iconKey: 'appIcon'}),
    copyColumn('endpointId', '终端ID'),
    avatarCopyColumn({colKey: 'endpointName', title: '终端名称', iconKey: 'icon'}),
    {
      colKey: 'type', title: '类型', width: 100, cell: (h, {row}) => {
        let arr = endpointTypes.value?.filter(item => item.itemId == row['type']);
        return arr[0]?.itemName || null;
      }
    },
    {
      colKey: 'scope', title: '范围', width: 100, cell: (h, {row}) => {
        let arr = endpointScopes.value?.filter(item => item.itemId == row['scope']);
        return arr[0]?.itemName || null;
      }
    },
    switchColumn({
      api: modifyEndpointStatus_api,
      idKeys: ['id'],
      label: '终端',
      perm: 'endpoint.modify_status',
      refresh: () => getEndpointPage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 140, cell: (h, {row}) => {
        return (
            <t-space size="small">
              {
                row?.metadata?.updateUser?.accountAvatarUrl ?
                    <t-avatar imageProps={{lazy: true}} class="pick" onClick={() => onWatchUserInfo(row)}
                              hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)}
                              size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl}/> : (
                        row?.metadata?.updateUser?.nickname ?
                            <t-avatar imageProps={{lazy: true}} class="pick" onClick={() => onWatchUserInfo(row)}
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
      {content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('endpoint.modify')},
      {content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('endpoint.delete')},
    ], {width: 160}),
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
    getEndpointPage();
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


/**
 ****************************************** 终端列表
 */
const getEndpointPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
      enabled: enabled.value,
      typeIds: typeIds.value,
      scopeIds: scopeIds.value,
    }
    if (appId.value) {
      params['appId'] = appId.value;
    }
    let res = await getEndpointPageList_api(params);
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
  getEndpointPage();
}
const onReset = () => {
  page.value = 1;
  keyword.value = null;
  appId.value = null;
  enabled.value = null;
  typeIds.value = [];
  scopeIds.value = [];
  getEndpointPage();
}


/**
 * 应用列表
 */
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {
    // Enabled: true
  }
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}


/**
 * 创建
 */
const rules = {
  appId: [
    {required: true, message: '应用必选', type: 'error', trigger: 'change'},
  ],
  endpointId: [
    {required: true, message: '终端id必填', type: 'error', trigger: 'change'},
  ],
  endpointName: [
    {required: true, message: '终端名称必填', type: 'error', trigger: 'change'},
  ],
  type: [
    {required: true, message: '类型必填', type: 'error', trigger: 'change'},
  ],
  scope: [
    {required: true, message: '范围必填', type: 'error', trigger: 'change'},
  ]
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  id: null, // 编辑
  appId: null,
  endpointId: null,
  endpointName: null,
  type: null,
  scope: null,
  enabled: null,
  websiteUrl: null,
})
const onCreate = () => {
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}
const onEdit = (row) => {
  setType('edit');
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
  }
  setForm({
    ...form.value,
    id: row?.id,
    appId: row?.appId,
    endpointId: row?.endpointId,
    endpointName: row?.endpointName,
    scope: row?.scope,
    type: row?.type,
    websiteUrl: row?.websiteUrl,
  })
  setVisible(true);
  formRef.value.clearValidate();
}
const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      if (type.value == 'add') {
        let params = {
          appId: form.value.appId,
          endpointId: form.value.endpointId,
          endpointName: form.value.endpointName,
          type: form.value.type,
          scope: form.value.scope,
          enabled: form.value.enabled,
          websiteUrl: form.value.websiteUrl,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await createEndpoint_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getEndpointPage();
        }
      } else {
        let params = {
          id: form.value.id,
          appId: form.value.appId,
          endpointId: form.value.endpointId,
          endpointName: form.value.endpointName,
          type: form.value.type,
          scope: form.value.scope,
          websiteUrl: form.value.websiteUrl,
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifyEndpointInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('修改成功');
          onClose();
          getEndpointPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
const onClose = () => {
  setType('add');
  setForm({
    appId: null,
    endpointId: null,
    endpointName: null,
    type: null,
    scope: null,
    enabled: null,
    websiteUrl: null,
  });
  setEditFileList([]);
  setVisible(false);
}


// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}


// 删除
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({e}) => {
      LoadingPlugin(true);
      try {
        let params = {
          id: row.id,
        }
        let res = await deleteEndpoint_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getEndpointPage();
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
</script>


<template>
  <div v-allow="'endpoint.find'" class="list__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="appId">
          <t-option :style="{ height: '40px', width: '100%' }" v-for="(item, index) in appList" :key="index"
            :label="item.appName" :value="item.appId">
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
        <t-select multiple clearable filterable v-model="typeIds">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointTypes"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="范围">
        <t-select multiple clearable filterable v-model="scopeIds">
          <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointScopes"
            :key="index"></t-option>
        </t-select>
      </FilterItem>
      <FilterItem label="终端名称">
        <t-input clearable placeholder="请输入终端名称" v-model="keyword"></t-input>
      </FilterItem>
      <FilterItem label="状态">
        <t-select clearable v-model="enabled">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'endpoint.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>

    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <!-- 创建 编辑 -->
  <Dialog :width="width < 750 ? '100%' : '30%'" top="10vh" @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
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
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="endpointId" label="终端id">
            <t-input v-model="form.endpointId"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="endpointName" label="终端名称">
            <t-input v-model="form.endpointName"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="type" label="类型">
            <t-select v-model="form.type">
              <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointTypes"
                :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="scope" label="范围">
            <t-select v-model="form.scope">
              <t-option :label="item.itemName" :value="item.itemId" v-for="(item, index) in endpointScopes"
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
          <t-form-item label="网址">
            <t-textarea v-model="form.websiteUrl" placeholder="" name="description"
              :autosize="{ minRows: 2, maxRows: 3 }" />
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="图标">
            <UploadImage :disabled="!form.appId || !form.endpointId" :appId="form.appId"
              :endpointId="form.endpointId" type="public" picType="endpoint-icon" @change="onChangeFiles"
              :limit="1" :fileList="editFileList">
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
.list__wrapper {}
</style>
