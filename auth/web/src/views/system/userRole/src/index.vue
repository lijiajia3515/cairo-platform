<!-- 角色 -->
<script setup lang="jsx">
defineOptions({ name: 'manage-system-role' })

import {
  ref,
  onMounted,
  watch,
  nextTick,
} from 'vue';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin
} from 'tdesign-vue-next';

import useState from '@/hooks/useState';

import List from '@/components/list';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, opColumn, switchColumn} from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';

import PermissionComponent from './permission.vue';

import {
  getAppRolePageList_api,
  createAppRole_api,
  modifyAppRoleInfo_api,
  modifyAppRoleStatus_api,
  deleteAppRole_api,
  getSubappList_api,
  getAppRoleSubappVersion_api,
  modifyAppRolePermission_api,
  getAppRolePermission_api,
  getAppRoleList_api,
  getCurrentEndpointList_api, getCurrentClientList_api,
  deleteAppRolePermission_api
} from '@/api';
onMounted(() => {
  getRolePage();
})

let keyword = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);

const [loading, setLoading] = useState(false);
const [list, setList] = useState([]);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'roleName', title: '名称' },
    { colKey: 'remark', title: '备注' },
    { colKey: 'userNum', title: '用户数量' },
    switchColumn({
      api: modifyAppRoleStatus_api,
      idKeys: ['roleId'],
      label: '角色',
      perm: 'role.modify_status',
      refresh: () => getRolePage(),
    }),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true} alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row)} size="medium" >{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      {content: '修改信息', onClick: (row) => onEditInfo(row), visible: () => hasPermission('role.modify_info')},
      {content: '修改权限', onClick: (row) => onPermission(row), visible: () => hasPermission('role.modify_permission')},
      {content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('role.delete')},
    ], {width: 230})
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
    getRolePage();
  }
});
const getRolePage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      keyword: keyword.value,
    }
    let res = await getAppRolePageList_api(params);
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
  getRolePage();
}

const onReset = () => {
  keyword.value = null;
  page.value = 1;
  getRolePage();
}

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
 * 创建
 */
const rules = {
  roleName: [
    { required: true, message: '角色名称必填', type: 'error', trigger: 'change' },
  ]
}
const formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setform] = useState({
  roleId: null,
  roleName: null,
  remark: null
});
const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let { roleId, roleName, remark } = form.value;
      if (type.value == 'add') {
        let params = {
          roleName, remark
        };
        let res = await createAppRole_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getRolePage();
        }
      } else {
        let params = {
          roleId, roleName, remark
        };
        let res = await modifyAppRoleInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑信息成功');
          onClose();
          getRolePage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onClose = () => {
  setType('add');
  setVisible(false);
  setform({
    roleId: null,
    roleName: null,
    remark: null
  });
}




/**
 * 编辑
 */
const onEditInfo = (row) => {
  setVisible(true);
  setType('edit');
  setform({
    roleId: row.roleId,
    roleName: row.roleName,
    remark: row.remark
  });
  formRef.value.clearValidate();
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
          roleIds: [row.roleId]
        }
        let res = await deleteAppRole_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getRolePage();
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

/**
 * 编辑权限
 */
let permissionRef = ref(null);
const [visible_permission, setVisiblePermission] = useState(false);
const [formPermission, setFormPermission] = useState({
  roleId: null,
  endpointId: null, // 终端ID
  // clientId: null, // 客户端ID
  subappId: null, // 子应用ID
  subappVersion: null // 子应用版本
});

const [appId, setAppId] = useState(null);

const onConfirmPermission = async () => {
  LoadingPlugin(true);
  try {
    let { roleId, endpointId, subappId, subappVersion } = formPermission.value;
    let permissionIds = permissionRef?.value?.getCheck() || [];
    let params = {
      roleId, endpointId, permissionIds, subappId,
      subappVersion: JSON.parse(subappVersion).subappVersion
    };
    let res = await modifyAppRolePermission_api(params);
    if (res.code == 'Success') {
      MessagePlugin.success('修改权限成功');
      getPermissionList();
      getSubappVersionList();
      // onClosePermission();
    }
  } finally {
    LoadingPlugin(false);
    // 更新版本号
    let rolesRes = await getAppRoleList_api({ roleIds: [formPermission.value.roleId] });
    let roles = rolesRes?.data || [];
    setFormPermission({
      ...formPermission.value
    })
  }
}
const onPermission = (row) => {
  setVisiblePermission(true);
  setFormPermission({
    ...formPermission.value,
    roleId: row.roleId
  });

  getEndpointList();
}
const onClosePermission = () => {
  setVisiblePermission(false);
  setFormPermission({
    roleId: null,
    endpointId: null, // 终端id
    subappId: null, // 子应用
    subappVersion: null, // 子应用版本
    permissionIds: []
  });
  setTerminalList([]);
  setSubappList([])
  setSubappVersionList([])
  setPermissionList([]);
  getRolePage();
}



// 获取功能权限列表
const [permissionList, setPermissionList] = useState([]);
let loading_permission = ref(false);
const getPermissionList = async () => {
  loading_permission.value = true;
  try {
    let params = {
      roleId: formPermission.value.roleId,
      endpointId: formPermission.value.endpointId,
      subappId: formPermission.value.subappId,
      subappVersion: JSON.parse(formPermission.value.subappVersion).subappVersion,
    }
    let res = await getAppRolePermission_api(params);
    if (res.code == 'Success') {
      setPermissionList(res?.data || []);
    }
  } finally {
    loading_permission.value = false;
  }
}




/**
 * 终端列表
 */
const [terminalList, setTerminalList] = useState([]);
const getEndpointList = async () => {
  let params = {
  };
  let res = await getCurrentEndpointList_api(params);
  if (res.code == 'Success' && res.data) {
    setTerminalList(res?.data);
    setFormPermission({
      ...formPermission.value,
      endpointId: res?.data[0]?.endpointId || null
    });
    setAppId(res?.data[0]?.appId)
  } else {
    setTerminalList([]);
    setFormPermission({
      ...formPermission.value,
      endpointId: null,
    })
  }
}

/**
 * 子应用列表
 */
const [subappList, setSubappList] = useState([]);
const getSubappList = async () => {
  let headers = {
    'app-id': appId.value,
    'endpoint-id': formPermission.value.endpointId,
  }
  let res = await getSubappList_api({}, headers);
  if (res.code == 'Success' && res.data) {
    setSubappList(res?.data);
    formPermission.value.subappId = res?.data[0].subappId || null
  } else {
    setSubappList([]);
    formPermission.value.subappId = null
  }
}

const [subappVersionList, setSubappVersionList] = useState([]);
const getSubappVersionList = async () => {
  let params = {
    roleId: formPermission.value.roleId,
    endpointId: formPermission.value.endpointId,
    subappId: formPermission.value.subappId
  }
  let res = await getAppRoleSubappVersion_api(params);
  if (res.code == 'Success' && res.data) {
    setSubappVersionList(res?.data);
    formPermission.value.subappVersion = JSON.stringify(res?.data[0]) || null
    if (res?.data[0]?.enabled == true) {
      isShow.value = true
    } else {
      isShow.value = false
    }
  } else {
    setSubappVersionList([]);
    formPermission.value.subappVersion = null
  }
}

const isShow = ref(false)
const handleChange = (val) => {
  let data = JSON.parse(val)
  if (data.enabled == true) {
    isShow.value = true
  } else {
    isShow.value = false
  }
  console.log(val, 'value=====');
}

const onDeletePermission = () => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          roleId: formPermission.value.roleId,
          endpointId: formPermission.value.endpointId,
          subappId: formPermission.value.subappId,
          subappVersion: JSON.parse(formPermission.value.subappVersion).subappVersion
        }
        let res = await deleteAppRolePermission_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
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

watch(() => formPermission.value.endpointId, () => {
  if (formPermission.value.endpointId) {
    getSubappList();
  } else {
    setSubappList([]);
    formPermission.value.subappId = null
    formPermission.value.subappVersion = null
  }
})

watch(() => formPermission.value.subappId, () => {
  if (formPermission.value.subappId) {
    getSubappVersionList();
    permissionRef?.value?.getChecked([])
  } else {
    setSubappVersionList([]);
    formPermission.value.subappVersion = null
  }
})

watch(() => formPermission.value.subappVersion, () => {
  if (formPermission.value.subappVersion != null) {
    getPermissionList(); // 获取权限
    permissionRef?.value?.getChecked([])
  } else {
    setPermissionList([]);
  }
})

</script>

<template>
  <div v-allow="'role.read'" class="role_page">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input clearable v-model="keyword" placeholder="请输入角色名称"></t-input>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'role.create'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <!-- 创建 修改 信息 -->
  <Dialog @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form ref="formRef" :rules="rules" :data="form">
      <t-row>
        <t-col :span="11">
          <t-form-item name="roleName" label="名称">
            <t-input v-model="form.roleName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="备注">
            <t-input v-model="form.remark"></t-input>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 修改权限 -->
  <Dialog width="55%" top="10" :cancelBtn="null" :confirmBtn="null" @close="onClosePermission"
    :visible="visible_permission">
    <template #title>编辑权限</template>
    <t-form>
      <t-row>
        <t-col :span="11">
          <t-form-item label="终端">
            <t-select v-model="formPermission.endpointId">
              <t-option :label="item.endpointName" :value="item.endpointId" v-for="(item, index) in terminalList"
                :key="index"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="子应用">
            <t-tabs v-model="formPermission.subappId">
              <t-tab-panel v-for="(item, index) in subappList" :key="index" :value="item.subappId"
                :label="item.subappName">
              </t-tab-panel>
            </t-tabs>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="子应用版本">
            <t-tabs v-model="formPermission.subappVersion" @change="handleChange">
              <t-tab-panel v-for="(item, index) in subappVersionList" :key="index" :value="JSON.stringify(item)"
                :label="item.enabled ? item.subappVersion : item.subappVersion + '(未开通)'">
              </t-tab-panel>
            </t-tabs>

          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="功能权限集合">
            <t-loading size="small" :loading="loading_permission">
              <PermissionComponent ref="permissionRef" v-if="permissionList.length" :list="permissionList">
              </PermissionComponent>
            </t-loading>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
    <div class="empty"></div>
    <div style="text-align: right;">
      <t-space>
        <t-button theme="danger" v-if="isShow" @click="onDeletePermission" v-allow="'role.delete_permission'">删除</t-button>
        <t-button variant="outline" @click="onClosePermission">取消</t-button>
        <t-button theme="primary" variant="base" @click="onConfirmPermission">确定</t-button>
      </t-space>
    </div>
  </Dialog>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.role_page {}
</style>
