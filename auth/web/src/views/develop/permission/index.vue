<!-- 功能权限 -->
<script setup lang="jsx">
import {
  ref, onMounted,
  watch,
} from 'vue';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import {
  debounce,
} from 'lodash';

import useState from '@/hooks/useState';

import List from '@/components/list';
import Dialog from '@/components/dialog';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';
import FilterItem from '@/components/filterBar/item.vue';
import {timeColumn, avatarCopyColumn, statusTagColumn} from '@/utils/tableColumns';
import {hasPermission} from '@/plugins/permission';

import {
  getPermissionPageList_api,
  createPermission_api,
  modifyPermission_api,
  deletePermission_api,
  movePermission_api,
  getMenuTree_api,

  getAppList_api,
  getEndpointList_api,
  getSubappList_api,
  getSubappVersionList_api
} from '@/api';



onMounted(() => {
  getAppList();
});

let menuIds = ref([]); // 选中的菜单Id组
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);

const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'permissionId', title: '权限ID' },
    avatarCopyColumn({colKey: 'permissionName', title: '权限名称', iconKey: 'icon', copyKey: 'permissionId'}),
    {
      colKey: 'authorities', title: '权限集合', width: 300, cell: (h, { row }) => {
        let list = row['authorities'];
        let dom = null;
        let str = '';
        if (list && list.length) {
          dom = list.map(item => {
            str += item + ', ';
            return <p>{item}</p>
          })
        }
        return <t-tooltip content={dom}>
          {str}
        </t-tooltip>;
      }
    },
    {
      colKey: 'menuId', title: '菜单', cell: (h, { row }) => {
        return (
          <t-space size="small">
            <div style="display:flex;height:100%;align-items: center;">
              {
                row['menuIcon'] ? <img src={row['menuIcon']} style="width:18px;height:18px;marginRight:5px;" /> : null
              }
            </div>
            <div>
              {
                (row['menuNames'] || []).map((item, index) => {
                  return <span>{item + ((index + 1) == row['menuNames'].length ? '' : '/')}</span>;
                })
              }
            </div>
          </t-space>
        )
      }
    },
    {
      colKey: 'type', title: '类型', cell: (h, { row }) => {
        return row['type'] == 'read' ? '读' : row['type'] == 'write' ? '写' : (row['type'] == 'operator' ? '操作' : '')
      }
    },
    ...statusTagColumn('defaultPermission', '是否默认权限', { type: 'yesno', width: 110 }),
    ...statusTagColumn('hiddenPermission', '是否隐藏权限', { type: 'yesno', width: 110 }),
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
    {
      colKey: 'operation', title: '操作', width: 200, fixed: 'right', cell: (h, { row, rowIndex }) => {
        if (!hasPermission('permission.write')) return null;
        return (
          <t-space>
            <t-button disabled={rowIndex == 0} onClick={() => onMoveTop(row, rowIndex)} variant="text" theme="primary" size="small">上</t-button>
            <t-button disabled={rowIndex == (list.value.length - 1)} onClick={() => onMoveBottom(row, rowIndex)} variant="text" theme="primary" size="small">下</t-button>
            <t-button onClick={() => onEdit(row)} variant="text" theme="primary" size="small">编辑</t-button>
            <t-button onClick={() => onDelete(row)} variant="text" theme="danger" size="small">删除</t-button>
          </t-space>
        )
      }
    }
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
    getPermissionPage();
  },
});

// 刷新页面
const onReLoad = () => {
  getPermissionPage();
}

const setIconFunc = (createElement, node) => {
  let data = {};
  if (node.data) {
    data = node.data;
  } else if (node['__tdesign_tree-node__']) {
    data = node['__tdesign_tree-node__'].data;
  }

  return (
    <t-space>
      {
        data.icon ? <t-image src={data.icon} style={{ width: '20px', height: '20px' }} fit="cover" /> : null
      }
      <span>{data?.menuName}</span>
    </t-space>
  )
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
// 头部参数
const [headerParams, setHeaderParams] = useState({
  appId: null,
  endpointId: null,
  subappId: null,
  subappVersion: null,
});

watch(menuIds, debounce(() => {
  if (headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion) {
    page.value = 1;
    getPermissionPage();
  }
}), {
  deep: true
})

watch(() => headerParams.value.appId, () => { // 切换应用
  menuIds.value = [];
  setHeaderParams({
    ...headerParams.value,
    endpointId: null,
    subappId: null,
  });
  getEndpointList(); // 获取终端
});

watch(() => headerParams.value.endpointId, () => {
  menuIds.value = [];
  setHeaderParams({
    ...headerParams.value,
    subappId: null
  });
  if (headerParams.value.endpointId) {
    getSubappList();
  } else {
    setSubappList([]);
    setList([]);
    total.value = 0;
  }
});

watch(() => headerParams.value.subappId, () => {
  if (headerParams.value.subappId) {
    getSubappVersionList();
  } else {
    setSubappVersionList([]);
    setHeaderParams({
      ...headerParams.value,
      subappVersion: null,
    });
    setSubappList([]);
    setList([]);
    total.value = 0;
  }
  ;
});

watch(() => headerParams.value.subappVersion, () => {
  menuIds.value = [];
  if (headerParams.value.subappVersion) {
    initData();
  }
})



const initData = debounce(() => {
  if (headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion) {
    getPermissionPage();
    getMenuTree();
  }
})


/**
 * 列表
 */

const getPermissionPage = debounce(async () => {
  setLoading(true);
  try {
    let headers = {
      'app-id': headerParams.value.appId,
      'endpoint-id': headerParams.value.endpointId,
      'subapp-id': headerParams.value.subappId,
      'subapp-version': headerParams.value.subappVersion,
    };
    let params = {
      page: page.value - 1,
      size: size.value,
    };
    if (menuIds.value.length) {
      params['menuIds'] = menuIds.value;
    }
    let res = await getPermissionPageList_api(params, headers);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = res?.data?.total * 1 || 0;
    }
  } finally {
    setLoading(false);
  }
})


/**
 * 上移
 * @param {Object} row
 * @param {Number} rowIndex
 */
const onMoveTop = async (row, rowIndex) => {
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let params = {
    movePermissionId: row.permissionId,// 移动权限id
    swapPermissionId: list.value[rowIndex - 1].permissionId// 被移动权限id
  };
  let res = await movePermission_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('移动成功');
    getPermissionPage();
  }
}
/**
 * 下移
 * @param {Object} row
 * @param {Number} rowIndex
 */
const onMoveBottom = async (row, rowIndex) => {
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let params = {
    movePermissionId: row.permissionId,// 移动权限id
    swapPermissionId: list.value[rowIndex + 1].permissionId// 被移动权限id
  };
  let res = await movePermission_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('移动成功');
    getPermissionPage();
  }
}


/**
 * 添加
 */
const rules = {
  menuId: [
    { required: true, message: '菜单必选', type: 'error', trigger: 'change' },
  ],
  permissionId: [
    { required: true, message: '功能权限Id必填', type: 'error', trigger: 'change' },
  ],
  permissionName: [
    { required: true, message: '功能权限名称必填', type: 'error', trigger: 'change' },
  ],
  type: [
    { required: true, message: '类型必填', type: 'error', trigger: 'change' },
  ],
};
let formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [form, setForm] = useState({
  menuId: null,
  permissionId: null,
  permissionName: null,
  authorities: [],
  type: null,
  hiddenPermission: false,
  defaultPermission: false
})
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}
const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let { menuId, permissionId, permissionName, authorities, hiddenPermission, type, defaultPermission } = form.value;
      let headers = {
        'app-id': headerParams.value.appId,
        'endpoint-id': headerParams.value.endpointId,
        'subapp-id': headerParams.value.subappId,
        'subapp-version': headerParams.value.subappVersion,
      };
      if (type.value == 'add') {
        let params = {
          menuId, permissionId, permissionName, authorities, hiddenPermission, type, defaultPermission
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await createPermission_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getPermissionPage();
        }
      } else {
        let params = {
          permissionId, permissionName, authorities, hiddenPermission, type, defaultPermission
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifyPermission_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          onClose();
          getPermissionPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    menuId: null,
    permissionId: null,
    permissionName: null,
    authorities: [],
    type: null,
    hiddenPermission: false,
    defaultPermission: false,
  });
  setEditFileList([]);
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}



// 编辑
const onEdit = (row) => {
  setVisible(true);
  setType('edit');
  setForm({
    menuId: row.menuId,
    permissionId: row.permissionId,
    permissionName: row.permissionName,
    authorities: row.authorities || [],
    hiddenPermission: row.hiddenPermission,
    defaultPermission: row.defaultPermission,
    type: row.type
  });
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
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
          permissionIds: [row.permissionId]
        };
        let headers = {
          'app-id': headerParams.value.appId,
          'endpoint-id': headerParams.value.endpointId,
          'subapp-id': headerParams.value.subappId,
          'subapp-version': headerParams.value.subappVersion,
        };
        let res = await deletePermission_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getPermissionPage();
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


// 菜单列表
let [menuList, setMenuList] = useState([]);
const getMenuTree = async () => {
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let res = await getMenuTree_api({}, headers);
  if (res.code == 'Success') {
    setMenuList(res?.data);
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
 * 子应用列表
 */
const [subappList, setSubappList] = useState([]);
const getSubappList = async () => {
  let headers = {};
  headers['app-id'] = headerParams.value.appId;
  headers['endpoint-id'] = headerParams.value.endpointId;
  let res = await getSubappList_api({}, headers);
  if (res.code == 'Success' && res.data && headerParams.value.endpointId) { // 在途响应防乱序：终端已被清空则丢弃，避免复活脏 subappId
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
    setList([]);
    total.value = 0;
  }
}

/**
 * 子应用版本列表
 */
const [subappVersionList, setSubappVersionList] = useState([]);
const getSubappVersionList = async () => {
  let headers = {};
  headers['app-id'] = headerParams.value.appId;
  headers['endpoint-id'] = headerParams.value.endpointId;
  let res = await getSubappVersionList_api({ subappId: headerParams.value.subappId }, headers);
  if (res.code == 'Success' && res.data && headerParams.value.subappId) { // 在途响应防乱序：子应用已被清空则丢弃，避免复活脏 subappVersion
    setSubappVersionList(res?.data);
    setHeaderParams({
      ...headerParams.value,
      subappVersion: res?.data[0]?.subappVersion || null
    });
    initData();
  } else {
    setSubappVersionList([]);
    setHeaderParams({
      ...headerParams.value,
      subappVersion: null
    });
    setList([]);
    total.value = 0;
  }
}
</script>


<template>
  <div v-allow="'permission.read'" class="permission__wrapper">
    <header class="filter-header">
      <div class="filter-header__items">
        <FilterItem label="应用">
          <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerParams.appId">
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
          <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="headerParams.endpointId">
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
            <t-option :style="{ background: item.enabled == true ? 'initial' : '#ededed' }" :label="item.subappName"
              :value="item.subappId" v-for="(item, index) in subappList" :key="index"></t-option>
          </t-select>
        </FilterItem>
        <FilterItem label="子应用版本">
          <t-select v-model="headerParams.subappVersion">
            <t-option :style="{ background: item.enabled == true ? 'initial' : '#ededed' }"
              :label="item.subappVersion" :value="item.subappVersion" v-for="(item, index) in subappVersionList"
              :key="index"></t-option>
          </t-select>
        </FilterItem>
        <FilterItem label="菜单" wide>
          <t-tree-select :minCollapsedNum="3" multiple
            :treeProps="{ keys: { label: 'menuName', value: 'menuId', children: 'menus' }, label: setIconFunc }"
            v-model="menuIds" :data="menuList" />
        </FilterItem>
      </div>
      <div class="filter-header__actions">
        <t-button v-allow="'permission.read'" @click="onReLoad" ghost>刷新</t-button>
        <t-button v-allow="'permission.write'" @click="onCreate">添加</t-button>
      </div>
    </header>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>


  <Dialog width="30%" @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加' : '编辑' }}</template>
    <t-form :rules="rules" ref="formRef" :data="form">
      <t-row>
        <t-col :span="12">
          <t-form-item name="menuId" label="菜单">
            <t-tree-select :disabled="type == 'edit'"
              :tree-props="{ keys: { label: 'menuName', value: 'menuId', children: 'menus' } }" v-model="form.menuId"
              :data="menuList" />
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="permissionId" label="功能权限id">
            <t-input :disabled="type == 'edit'" v-model="form.permissionId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="permissionName" label="功能权限名称">
            <t-input v-model="form.permissionName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="权限集合">
            <t-tag-input v-model="form.authorities" placeholder="" :tag-props="{ theme: 'primary' }" />
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="type" label="类型">
            <t-select v-model="form.type">
              <t-option label="读" value="read" />
              <t-option label="写" value="write" />
              <t-option label="操作" value="operator" />
            </t-select>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="图标">
            <UploadImage :disabled="!headerParams.appId" :appId="headerParams.appId" type="public"
              picType="permission-icon" @change="onChangeFiles" :limit="1" :fileList="editFileList">
            </UploadImage>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="是否默认权限">
            <t-radio-group v-model="form.defaultPermission">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item label="是否隐藏">
            <t-radio-group v-model="form.hiddenPermission">
              <t-radio :value="true">是</t-radio>
              <t-radio :value="false">否</t-radio>
            </t-radio-group>
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
.permission__wrapper {
  // 筛选区对齐 FilterBar 间距/宽度:级联筛选由 watch 驱动,无查询/重置语义,不套 FilterBar
  .filter-header {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-start;
    gap: 12px 16px;
    padding-bottom: 12px;

    &__items {
      display: flex;
      flex-wrap: wrap;
      gap: 12px 16px;
      flex: 1 1 auto;
      min-width: 0;
    }

    &__actions {
      display: flex;
      gap: 8px;
      flex: none;
      margin-left: auto;
    }
  }
}
</style>
