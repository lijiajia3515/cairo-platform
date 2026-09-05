<!-- app应用版本发行 -->
<script setup lang="jsx">
import {
  ref, onMounted,
  nextTick, watch,
} from 'vue';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import Dialog from '@/components/dialog';
import UploadFile from '@/components/uploadFile';
import UserInfo from '@/components/userInfo';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import { timeColumn, avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';

import {
  getAppList_api,
  getEndpointList_api,
  getAppReleasePageList_api,
  createAppRelease_api,
  modifyAppReleaseInfo_api,
  deleteAppRelease_api,
  setAppReleaseLatestVersion_api
} from '@/api';

onMounted(() => {
  getAppVersionPage();

  nextTick(() => {
    getAppList();
  })
});

let appId = ref(null);
let endpointId = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);
let list = ref([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    avatarCopyColumn({colKey: 'appName', title: '应用', iconKey: 'appIcon', copyKey: 'appId'}),
    avatarCopyColumn({colKey: 'endpointName', title: '终端', iconKey: 'endpointIcon', copyKey: 'endpointId'}),
    { colKey: 'appVersion', title: '版本' },
    { colKey: 'title', title: '标题' },
    {
      colKey: 'remark', title: '描述/备注', width: 160, ellipsis: true, cell: (h, { row }) => {
        let remark = row['remark'] || '';
        return (
          <t-link onClick={() => onWacthDetailRemark(remark)}>{remark.length > 30 ? remark.substring(0, 30) + '...' : remark}</t-link>
        )
      }
    },
    {
      colKey: 'releaseVersion', title: '是否发行版本', cell: (h, { row }) => {
        let key = row['releaseVersion'];
        return key == true ? '发行版本' : (key == false ? '预览版本' : '');
      }
    },
    {
      colKey: 'force', title: '是否强制更新', cell: (h, { row }) => {
        let key = row['force'];
        return key == true ? '强制更新' : (key == false ? '非强制更新' : '');
      }
    },
    switchColumn({
      colKey: 'latestVersion',
      title: '是否为最新版本',
      api: setAppReleaseLatestVersion_api,
      idKeys: ['appId', 'endpointId', 'appVersion'],
      extra: { type: 'android' },
      pairs: { true: { label: '最新版本', theme: 'success' }, false: { label: '非最新版本', theme: 'default' } },
      confirmOf: (value) => value ? '设为最新版本' : '取消最新版本',
      perm: 'app_release.set_latest_version',
      refresh: () => getAppVersionPage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                row?.metadata?.updateUser?.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} size="medium" >{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '详情', onClick: (row) => onWatch(row), visible: () => hasPermission('app_release.read') },
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('app_release.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('app_release.delete') },
    ], { width: 280 }),
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
    getAppVersionPage();
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
 ************************************ app版本更新 分页 ****************************************
 */
const getAppVersionPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      type: 'android'
    };
    let headers = {};
    if (appId.value) {
      headers['app-id'] = appId.value;
    }
    if (endpointId.value) {
      headers['endpoint-id'] = endpointId.value;
    }
    let res = await getAppReleasePageList_api(params, headers);
    if (res.code == 'Success') {
      list.value = res?.data?.contents || [];
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}
const onSearch = () => {
  page.value = 1;
  getAppVersionPage();
}
const onReset = () => {
  page.value = 1;
  appId.value = null;
  endpointId.value = null;
  getAppVersionPage();
}

/**
 * 详情
 * @param {Object} row
 */
const [visibleDetail, setVisibleDetail] = useState(false);
const [detailData, setDetailData] = useState({});
const onWatch = (row) => {
  setVisibleDetail(true);
  setDetailData(row);
}
const onCloseWatch = () => {
  setVisibleDetail(false);
  setDetailData({});
}




/**
 ************************************** 添加 **********************************************
 */
const rules = {
  appId: [
    { required: true, message: '应用必选', type: 'error', trigger: 'change' },
  ],
  endpointId: [
    { required: true, message: '终端必选', type: 'error', trigger: 'change' },
  ],
  appVersion: [
    { required: true, message: '版本号必填', type: 'error', trigger: 'change' },
  ],
  latestVersion: [
    { required: true, message: '是否为最新版本必选', type: 'error', trigger: 'change' },
  ],
  title: [
    { required: true, message: '标题必选', type: 'error', trigger: 'change' },
  ],
  remark: [
    { required: true, message: '描述/备注必填', type: 'error', trigger: 'change' },
  ],
  releaseVersion: [
    { required: true, message: '是否发行版本必填', type: 'error', trigger: 'change' },
  ],
  force: [
    { required: true, message: '是否强制更新必填', type: 'error', trigger: 'change' },
  ]
}
let formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [androidApkList, setAndroidApkList] = useState([]);
const [editAndroidApkList, setEditAndroidApkList] = useState([]); // 编辑显示
const [form, setForm] = useState({
  appId: null,
  appVersion: null,
  endpointId: null,
  title: '',
  releaseVersion: null,
  latestVersion: null,
  remark: null,
  force: null,
  androidApkUrl: null,
  type: 'android'
})
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    let { appId, appVersion, endpointId, releaseVersion, latestVersion, title, remark, force, androidApkUrl, type } = form.value;
    if (type.value == 'add') {
      let params = {
        appId, appVersion, endpointId, releaseVersion, latestVersion, title, remark, force, androidApkUrl, type
      };
      if (androidApkList.value && androidApkList.value.length) {
        params['androidApkUrl'] = androidApkList?.value[0]?.url;
      }
      let res = await createAppRelease_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('创建成功');
        onClose();
        getAppVersionPage();
      }
    } else {
      let params = {
        appId, appVersion, endpointId, releaseVersion, title, remark, force, androidApkUrl, type
      };
      if (androidApkList.value && androidApkList.value.length) {
        params['androidApkUrl'] = androidApkList?.value[0]?.url;
      } else {
        params['androidApkUrl'] = '';
      }
      let res = await modifyAppReleaseInfo_api(params);
      if (res.code == 'Success') {
        MessagePlugin.success('编辑成功');
        onClose();
        getAppVersionPage();
      }
    }
  }
}
const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    appId: null,
    appVersion: null,
    endpointId: null,
    releaseVersion: null,
    latestVersion: null,
    title: null,
    remark: null,
    force: null,
    androidApkUrl: null,
    type: 'android'
  })
  setAndroidApkList([]);
  setEditAndroidApkList([]);
}

const onChangeFiles = (files) => {
  setAndroidApkList(files);
}


/**
 **************************** 编辑 ****************************
 * @param {Object} row
 */
const onEdit = (row) => {
  setVisible(true);
  setType('edit');
  setForm({
    appId: row?.appId,
    appVersion: row?.appVersion,
    endpointId: row?.endpointId,
    releaseVersion: row?.releaseVersion,
    latestVersion: row?.latestVersion,
    title: row?.title,
    remark: row?.remark,
    force: row?.force,
    androidApkUrl: row?.androidApkUrl,
    type: 'android'
  });
  if (row.androidApkUrl) {
    setEditAndroidApkList([{ name: row.androidApkUrl, url: row.androidApkUrl }])
  }
}



/**
 * 删除
 * @param {Object} row
 */
const onDelete = (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续删除?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          appId: row?.appId,
          endpointId: row?.endpointId,
          appVersion: row?.appVersion,
          type: 'android'
        }
        let res = await deleteAppRelease_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getAppVersionPage();
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

watch(() => form.value.appId, () => {
  if (form.value.appId) {
    getEndpointList();
  }
});

const [endpointListFilter, setEndpointListFilter] = useState([]);
watch(appId, async () => {
  if (appId.value) {
    let params = {
      appId: appId.value
    }
    let res = await getEndpointList_api(params);
    if (res.code == 'Success') {
      setEndpointListFilter(res?.data || []);
    }
  }
  endpointId.value = null;
})


/**
 * 应用列表
 */
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let res = await getAppList_api({});
  if (res.code == 'Success') {
    setAppList(res?.data || []);
  }
}

/**
 * 终端列表
 */
const [endpointList, setEndpointList] = useState([]);
const getEndpointList = async () => {
  let params = {
    appId: form.value.appId
  }
  let res = await getEndpointList_api(params);
  if (res.code == 'Success') {
    setEndpointList(res?.data || []);
  }
}

const onChangeApp = () => {
  setForm({
    ...form.value,
    endpointId: null,
  })
}



// 查看备注详情
const detailRemark = ref(null);
const visibleDetailRemark = ref(false);
const onWacthDetailRemark = (remark) => {
  visibleDetailRemark.value = true;
  detailRemark.value = remark;
}
const onCloseDetailRemark = () => {
  visibleDetailRemark.value = false;
  detailRemark.value = null;
}
</script>


<template>
  <div v-allow="'app_release.read'" class="appVersion__wrapper">
    <div class="empty"></div>
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="appId" placeholder="请选择应用">
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
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="endpointId" placeholder="请选择终端">
          <t-option :label="item.endpointName" :value="item.endpointId"
            v-for="(item, index) in endpointListFilter" :key="index" :style="{ height: '40px', width: '100%' }">
            <div style="display: flex;align-items: center;width: 100%;">
              <t-avatar :imageProps="{ lazy: true }" size="20px" :image="item.icon" shape="round"></t-avatar>
              <span style="display: inline-block;marginLeft:10px;">{{ item.endpointName }}</span>
            </div>
          </t-option>
          <template #valueDisplay="{ value }">
            <template v-if="value">
              <t-space>
                <t-avatar :imageProps="{ lazy: true }" size="20px"
                  :image="endpointListFilter.filter(item => item.endpointId == value)[0]?.icon"
                  shape="round"></t-avatar>
                {{ endpointListFilter.filter(item => item.endpointId == value)[0]?.endpointName }}
              </t-space>
            </template>
          </template>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'app_release.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <!-- 详情 -->
  <Dialog @close="onCloseWatch" @confirm="onCloseWatch" top="20" width="60%" :visible="visibleDetail">
    <template #title>详情</template>
    <t-row>
      <t-col :span="12">
        <t-space>
          <span> 应用名称：{{ detailData?.appName }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span>终端名称: {{ detailData?.endpointName }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span> 标题：{{ detailData?.title }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span> 描述/备注：{{ detailData?.remark }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span>是否发行版本: {{ detailData?.releaseVersion ? '是' : '否' }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span>是否强制更新: {{ detailData?.force ? '是' : '否' }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span>是否为最新版本: {{ detailData?.latestVersion ? '是' : '否' }}</span>
        </t-space>
      </t-col>
      <div class="empty"></div>
      <t-col :span="12">
        <t-space>
          <span>安卓安装包下载地址: {{ detailData?.androidApkUrl }}</span>
        </t-space>
      </t-col>
    </t-row>
  </Dialog>

  <!-- 添加 / 编辑 -->
  <Dialog @confirm="onSubmit" @close="onClose" top="20" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form ref="formRef" :data="form" :rules="rules" label-width="120px">
      <t-row>
        <t-col :span="12">
          <t-form-item name="appId" label="应用">
            <t-select :disabled="type == 'edit'" clearable v-model="form.appId" @change="onChangeApp">
              <t-option :label="item.appName" :value="item.appId" v-for="(item, index) in appList" :key="index"
                :style="{ background: item.enabled == true ? 'initial' : 'var(--td-bg-color-component-disabled)' }">
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
          <t-form-item name="endpointId" label="终端">
            <t-select :disabled="type == 'edit'" clearable v-model="form.endpointId">
              <t-option :label="item.endpointName" :value="item.endpointId"
                v-for="(item, index) in endpointList" :key="index"
                :style="{ background: item.enabled == true ? 'initial' : 'var(--td-bg-color-component-disabled)' }">
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
          <t-form-item name="appVersion" label="版本号">
            <t-input :disabled="type == 'edit'" v-model="form.appVersion"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="title" label="标题">
            <t-input v-model="form.title"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="remark" label="描述/备注">
            <t-textarea v-model="form.remark" :autosize="{ minRows: 3, maxRows: 10 }" placeholder="" />
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item label="安卓安装包地址">
            <UploadFile :disabled="(!form.appId || !form.endpointId || !form.appVersion) ? true : false"
              tips="请上传apk文件" :appId="form.appId" :endpointId="form.endpointId" :appVersion="form.appVersion"
              type="public" picType="app-release" @change="onChangeFiles" :limit="1" :fileList="editAndroidApkList">
            </UploadFile>
            <!-- <t-textarea v-model="form.androidApkUrl" placeholder="请输入地址" /> -->
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <t-row>
        <t-col :span="12">
          <t-form-item name="releaseVersion" label="是否发行版本">
            <t-radio-group :allowUncheck="true" v-model="form.releaseVersion">
              <t-radio :value="true">发行版本</t-radio>
              <t-radio :value="false">预览版本</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
      <div class="empty"></div>
      <template v-if="type == 'add'">
        <t-row>
          <t-col :span="12">
            <t-form-item name="latestVersion" label="是否最新版本">
              <t-radio-group :allowUncheck="true" v-model="form.latestVersion">
                <t-radio :value="true">是</t-radio>
                <t-radio :value="false">否</t-radio>
              </t-radio-group>
            </t-form-item>
          </t-col>
        </t-row>
        <div class="empty"></div>
      </template>
      <t-row>
        <t-col :span="12">
          <t-form-item name="force" label="是否强制更新">
            <t-radio-group :allowUncheck="true" v-model="form.force">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
    <div class="empty"></div>
  </Dialog>



  <!-- 备注 详情 -->
  <Dialog @close="onCloseDetailRemark" :visible="visibleDetailRemark">
    <template #title>备注</template>
    <t-textarea v-model="detailRemark" readonly :autosize="{ minRows: 3, maxRows: 10 }" placeholder="" />
  </Dialog>


  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.appVersion__wrapper {
  padding: 0 20px;
}
</style>
