<template>
  <template v-if="pageType == 'parent'">
    <div v-allow="'menu.read'" class="menu__wrapper">
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
              <t-option :style="{ background: item.enabled == true ? 'initial' : 'var(--td-bg-color-component-disabled)' }" :label="item.subappName"
                :value="item.subappId" v-for="(item, index) in subappList" :key="index"></t-option>
            </t-select>
          </FilterItem>
          <FilterItem label="版本">
            <t-select v-model="headerParams.subappVersion">
              <t-option :style="{ background: item.enabled == true ? 'initial' : 'var(--td-bg-color-component-disabled)' }"
                :label="item.subappVersion" :value="item.subappVersion" v-for="(item, index) in subappVersionList"
                :key="index"></t-option>
            </t-select>
          </FilterItem>
        </div>
        <div class="filter-header__actions">
          <t-button v-allow="'menu.read'" @click="onReLoad" ghost>刷新</t-button>
          <t-button v-allow="'menu.write'" @click="moveShowDialog" ghost>菜单移动</t-button>
          <t-button v-allow="'menu.write'" @click="addShowDialog">添加</t-button>
        </div>
      </header>
      <div class="empty"></div>
      <t-table table-layout="auto" drag-sort="row-handler" @drag-sort="onDragSort" size="small" row-key="menuId" :data="state.list"
        :columns="columns">
      </t-table>
      <div class="empty"></div>
      <!-- 分页 -->
      <t-pagination v-model="state.page" v-model:pageSize="state.size" :total="state.total" show-jumper show-page-size
        :pageSizeOptions="[10, 20, 50, 100]" @page-size-change="onPageSizeChange" @current-change="onCurrentChange" />
    </div>
  </template>

  <template v-if="pageType == 'child'">
    <Child @home="goHome" :id="id" :name="name" :appId="appId" :endpointId="endpointId" :subappId="subappId"
      :subappVersion="subappVersion" />
  </template>

  <!-- 添加父菜单 -->
  <t-dialog @close="onCloseAddParent" :close-on-overlay-click="false" attach="body" :on-confirm="onAddMenuFunc"
    v-model:visible="state.addShow">
    <template #header>
      添加菜单
    </template>
    <t-form>
      <t-row>
        <t-col :span="12">
          <t-form-item label="名称">
            <t-input :maxlength="255" v-model="state.addform.name" placeholder="请输入名称"></t-input>
          </t-form-item>
          <t-form-item label="组件路径">
            <t-input :maxlength="255" v-model="state.addform.component" placeholder="例： system/user"></t-input>
          </t-form-item>
          <t-form-item label="地址">
            <t-input :maxlength="255" v-model="state.addform.path" placeholder="例： https://baidu.com"></t-input>
          </t-form-item>
          <t-form-item label="tags">
            <t-select v-model="state.addform.tags" multiple :max="2">
              <t-option label="新功能" value="new" />
              <t-option label="热门" value="hot" />
              <t-option label="旧版本" value="old" />
              <t-option label="废弃功能" value="deprecated" />
            </t-select>
          </t-form-item>
          <t-form-item label="icon">
            <UploadImage :appId="headerParams.appId" type="public" picType="menu" @change="onChangeFiles" :limit="1"
              :fileList="editFileList"></UploadImage>
            <!-- <t-input :maxlength="255" v-model="state.addform.icon" placeholder="阿里巴巴矢量图标库"></t-input> -->
          </t-form-item>
          <t-form-item label="菜单是否隐藏">
            <t-radio-group v-model="state.addform.hidden" :default-value="false" name="显示"
              :options="itemOptions"></t-radio-group>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </t-dialog>

  <!-- 权限列表 -->
  <t-dialog @close="onClosePermissionDialog" :confirmBtn="null" :close-on-overlay-click="false" attach="body"
    width="50%" v-model:visible="state.previewShow">
    <template #header>
      权限列表
    </template>
    <div class="empty"></div>
    <t-table table-layout="auto" size="small" row-key="id" :data="state.adminData.list" :columns="adminColumns">
    </t-table>
    <div class="empty"></div>
    <!-- 分页 -->
    <t-pagination v-model="state.adminData.page" v-model:pageSize="state.adminData.size" :total="state.adminData.total"
      show-jumper @page-size-change="onPermissionPageSizeChange" @current-change="onPermissionCurrentChange" />
  </t-dialog>

  <!-- 移动菜单 -->
  <t-dialog :cancelBtn="null" :confirmBtn="null" :close-on-overlay-click="false" @close="onCloseMenuMove"
    :visible="state.move_show">
    <template #header>菜单移动</template>
    <!-- expand-all -->
    <t-tree :keys="{ value: 'menuId', label: 'menuName', children: 'menus' }" :data="state.menuTrees" activable hover
      transition draggable @drag-end="handleDragEnd" />
  </t-dialog>

  <!-- 编辑父菜单 -->
  <EditParent v-if="visibleEditParent" :visible="visibleEditParent" :appId="headerParams.appId"
    :endpointId="headerParams.endpointId" :subappId="headerParams.subappId"
    :subappVersion="headerParams.subappVersion" :menuId="formEditParent.menuId" :icon="formEditParent.icon"
    :menuName="formEditParent.menuName" :component="formEditParent.component" :path="formEditParent.path"
    :tags="formEditParent.tags" :hiddenMenu="formEditParent.hiddenMenu"
    @finish="onFinishEditParent" @close="onCloseEditParentMenu"></EditParent>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>
<script setup lang="jsx">
defineOptions({ name: 'manage-develop-menu' })

import { ref, shallowRef, watch, reactive, onMounted } from 'vue';
import { debounce } from 'lodash';

import { Input, MessagePlugin, DialogPlugin } from 'tdesign-vue-next';
import { MoveIcon } from 'tdesign-icons-vue-next';

import useState from '@/hooks/useState';
import FilterItem from '@/components/filterBar/item.vue';
import { opColumn, switchColumn, timeColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import EditParent from './components/editParent.vue';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';
import Child from './components/child.vue';
import {
  getMenuPageList_api, modifyMenu_api, createMenu_api,
  deleteMenu_api,
  moveMenu_api, getMenuTree_api,
  getAppList_api, getEndpointList_api, getSubappVersionList_api,
  getPermissionPageList_api,
  getSubappList_api
} from '@/api';

const pageType = ref('parent');

const [, setId] = useState(null); // 父级
const [name, setName] = useState(null); // 父级菜单名(子级视图面包屑用;此前只解构 setter 致 :name 恒空)
const [appId, setAppId] = useState(null);
const [endpointId, setEndpointId] = useState(null);
const [subappId, setSubappId] = useState(null);
const [subappVersion, setSubappVersion] = useState(null);

let state = reactive({
  page: 1,
  size: 10,
  total: 0,
  list: [],
  addShow: false,
  addform: { // 添加父菜单
    name: '',
    component: '',
    path: '',
    icon: '',
    tags: [],
    hidden: false
  },
  previewShow: false,
  adminData: { // 权限信息（预览用；增删改走 develop/permission 页）
    menuId: null,
    page: 1,
    size: 10,
    total: 0,
    list: []
  },
  nowEditRadio_row: {},

  move_show: false,
  menuTrees: []
});

let itemOptions = ref([
  {
    value: false,
    label: '显示',
  },
  {
    value: true,
    label: '隐藏',
  },
])

let columns = shallowRef([
  {
    colKey: 'drag',
    title: '',
    cell: (h) => {
      return (
        <span>
          <MoveIcon />
        </span>
      )
    },
    width: 46,
  },
  {
    colKey: 'icon', title: '图标', cell: (h, { row }) => {
      return (
        <div>
          {
            row['icon'] ? <img style={{ width: '16px', height: '16px' }} src={row['icon']} /> : null
          }
        </div>
      )
    },
    width: 60,
  },
  {
    colKey: 'menuName', title: '菜单名称', ellipsis: true, minWidth: 140, edit: {
      props: {
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let menuName = context.newRowData.menuName;
        let menuId = context.newRowData.menuId;
        editMenu('menuName', menuName, menuId, false)
      },
    }
  },
  {
    colKey: 'component', title: '组件地址', ellipsis: true, width: 170, edit: {
      props: {
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let component = context.newRowData.component;
        let menuId = context.newRowData.menuId;
        editMenu('component', component, menuId, false)
      },
    }
  },
  {
    colKey: 'path', title: '外部地址', ellipsis: true, width: 120, edit: {
      props: {
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let path = context.newRowData.path;
        let menuId = context.newRowData.menuId;
        editMenu('path', path, menuId, false)
      },
    }
  },
  {
    colKey: 'tags', title: 'tags', width: 90, cell: (h, { row }) => {
      return (
        <t-space size="small">
          {
            row['tags']?.includes('new') ? <span>新功能</span> : null
          }
          {
            row['tags']?.includes('hot') ? <span>热门</span> : null
          }
          {
            row['tags']?.includes('old') ? <span>旧版本</span> : null
          }
          {
            row['tags']?.includes('deprecated') ? <span>废弃功能</span> : null
          }
        </t-space>
      )
    },
    minWidth: 130,
  },
  // 可见性行内开关:开=隐藏(红)/关=显示(绿),确认后直改 modify_menu
  switchColumn({
    colKey: 'hiddenMenu', title: '可见性', width: 96,
    pairs: { true: { label: '隐藏', theme: 'danger' }, false: { label: '显示', theme: 'success' } },
    confirmOf: (value) => (value ? '隐藏' : '显示'),
    api: (params) => modifyMenu_api(params, {
        'app-id': headerParams.value.appId,
        'endpoint-id': headerParams.value.endpointId,
        'subapp-id': headerParams.value.subappId,
        'subapp-version': headerParams.value.subappVersion,
      }),
    idKeys: ['menuId'],
    label: '菜单',
    perm: 'menu.write',
    refresh: () => getMenuList(), // 延迟引用:columns 求值时 getMenuList 尚未声明(TDZ)
  }),
  {
    colKey: 'metadata.updateUser.nickname', title: '更新人', width: 110, cell: (h, { row }) => {
      return (
        <t-space size="small">
          {
            row?.metadata?.updateUser?.accountAvatarUrl ?
              <t-avatar class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true}
                alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="small"
                image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row)}
                  size="small">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
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
    { content: '子菜单', onClick: (row) => onPreviewChildMenu(row), visible: () => hasPermission('menu.read') },
    { content: '编辑', onClick: (row) => onEditdMenu(row), visible: () => hasPermission('menu.write') },
    { content: '功能权限', onClick: (row) => onPreviewPermission(row), visible: () => hasPermission('menu.read') },
    { content: '删除', theme: 'danger', onClick: (row) => onDeleteMenu(row), visible: () => hasPermission('menu.write') },
  ], { width: 200 }),
]);


let adminColumns = shallowRef([
  { colKey: 'permissionId', title: '权限id', ellipsis: true, minWidth: 190 },
  {
    colKey: 'permissionName', title: '权限名称', ellipsis: true, minWidth: 120,
  },

  {
    colKey: 'permissions', title: '权限值', ellipsis: true, minWidth: 200, cell: (h, { col, row }) => {
      let str = '';
      if (row[col.colKey]) {
        row[col.colKey].forEach((item, index) => {
          str += item + ', ';
        })
      }
      return (
        str
      )
    },
  },
]);

const goHome = () => {
  pageType.value = 'parent';
}

// 刷新页面
const onReLoad = () => {
  // reload();
  initData();
}


// 头部参数
const [headerParams, setHeaderParams] = useState({
  appId: null,
  endpointId: null,
  subappId: null,
  subappVersion: null,
})

watch(() => headerParams.value.appId, () => { // 切换应用：重置下游级联。否则新旧应用首个终端同名时 endpointId 值不变，后续 watch 不触发，子应用残留上一个应用的
  setHeaderParams({
    ...headerParams.value,
    endpointId: null,
    subappId: null,
    subappVersion: null,
  });
  getEndpointList();

});
watch(() => headerParams.value.endpointId, () => {
  if (headerParams.value.endpointId) {
    getSubappList();
  } else {
    setSubappList([]);
    setHeaderParams({
      ...headerParams.value,
      subappId: null,
      subappVersion: null,
    });
    state.list = [];
    state.total = 0;
    state.menuTrees = [];
  }
  ;
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
    state.list = [];
    state.total = 0;
    state.menuTrees = [];
  }
  ;
});

watch(() => headerParams.value.subappVersion, () => {
  if (headerParams.value.subappVersion) {
    initData();
  }
})

const initData = debounce(() => {
  if (headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion) {
    getMenuList();
    getMenuTree();
  }
})


onMounted(() => {
  getAppList();
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
 ************************************************************* 编辑 父菜单
 */
const visibleEditParent = ref(false);
const [formEditParent, setFormEditParent] = useState({
  menuId: null,
  icon: null,
  menuName: null,
  component: null,
  path: null,
  tags: null,
  hiddenMenu: null,
})
/**
 * 编辑 父菜单
 * @param {Object} row
 * @param {String} row.menuId
 */
const onEditdMenu = (row) => {
  setFormEditParent({
    menuId: row?.menuId,
    icon: row?.icon,
    menuName: row?.menuName,
    component: row?.component,
    path: row?.path,
    tags: row?.tags,
    hiddenMenu: row?.hiddenMenu
  });
  visibleEditParent.value = true;
}
const onCloseEditParentMenu = () => {
  visibleEditParent.value = false;
}
const onFinishEditParent = () => {
  getMenuList();
}

/**
 * **************************** 菜单移动 start **************************
 */

const handleDragEnd = async ({ node, e }) => {
  // console.log('移动', node, e)
  try {
    let newData = [];
    let parentId = null;
    let beforeId = '';
    let moveId = null;
    if (node['__tdesign_tree-node__']) {
      moveId = node['__tdesign_tree-node__'].data.menuId;
      if (node['__tdesign_tree-node__'].parent) {
        newData = node['__tdesign_tree-node__'].parent.children;
        parentId = node['__tdesign_tree-node__'].parent.data.menuId;
      } else { // 最外层，没有父级
        if (node['__tdesign_tree-node__'].tree) {
          newData = node['__tdesign_tree-node__'].tree.children;
        }
        parentId = '0';
      }
    } else {
      moveId = node.data.menuId;
      if (node.parent) {
        newData = node.parent.children;
        parentId = node.parent.data.menuId;
      } else { // 最外层，没有父级
        if (node.tree) {
          newData = node.tree.children;
        }
        parentId = '0';
      }
    }


    const index = newData.findIndex(item => item.data.menuId == moveId);
    if (index == (newData.length - 1)) { // 移动到最后一个
      // setMenuMove('', moveId, parentId);
    } else {
      beforeId = newData[index + 1].data.menuId; // 菜单移动完后，后面一个Id
      // setMenuMove(beforeId, moveId, parentId);
    }
    let headers = {
      'app-id': headerParams.value.appId,
      'endpoint-id': headerParams.value.endpointId,
      'subapp-id': headerParams.value.subappId,
      'subapp-version': headerParams.value.subappVersion,
    };
    let params = {
      moveId, beforeId, parentId
    };
    let res = await moveMenu_api(params, headers);
    if (res.code == 'Success') {
      MessagePlugin.success('移动成功');
      getMenuList();
    }
  } catch (err) {
    console.log(err);
  }
  // console.log(beforeId, moveId, parentId);
};

// 移动菜单 可跨级
let moveShowDialog = () => {
  state.move_show = true;
}

let onCloseMenuMove = () => {
  state.move_show = false;
}

let getMenuTree = async () => {
  if (!(headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion)) {
    return; // 四要素不全不发请求，避免 subappId不能为空
  }
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  }
  let res = await getMenuTree_api({}, headers);
  if (res.code == 'Success' && res.data && res.data.length) {
    state.menuTrees = res.data;
  }
}

/**
 * 监听父菜单移动事件
 */
let onDragSort = (params) => {
  let { current, newData } = params;
  let parentId = '0';
  let moveId = current.menuId;
  const index = newData.findIndex(item => item.menuId == moveId);
  if (index == (newData.length - 1)) { // 移动到最后一个
    setMenuMove('', moveId, parentId);
  } else {
    let beforeId = newData[index + 1].menuId; // 菜单移动完后，后面一个Id
    setMenuMove(beforeId, moveId, parentId);
  }
}

let setMenuMove = async (beforeId, moveId, parentId) => {
  let params = {
    moveId, beforeId, parentId
  };
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let res = await moveMenu_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('移动成功');
    getMenuList();
    getMenuTree();
  }
}


let onRadioChange = async (flag, menuId) => {
  let params = {
    menuId,
    hiddenMenu: flag
  };
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  }
  let res = await modifyMenu_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('编辑成功');
    getMenuList();
  }
}

/**
 * 获取菜单权限
 */
let onPreviewPermission = (row) => {
  state.previewShow = true;
  state.adminData.menuId = row.menuId;
  getPermissionMenu();
}

let getPermissionMenu = async () => {
  let { page, size, menuId } = state.adminData;
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let params = {
    page: page - 1,
    size,
    menuIds: [menuId]
  }
  let res = await getPermissionPageList_api(params, headers);
  if (res.code == 'Success') {
    state.adminData.list = res.data.contents;
    state.adminData.total = Number(res.data.total)
  }
}

/**
 * 删除父菜单
 */
let onDeleteMenu = (row) => {
  const confirmDia = DialogPlugin({
    header: '菜单删除',
    body: `是否删除菜单「${row.menuName}」?`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      let headers = {
        'app-id': headerParams.value.appId,
        'endpoint-id': headerParams.value.endpointId,
        'subapp-id': headerParams.value.subappId,
        'subapp-version': headerParams.value.subappVersion,
      }
      let res = await deleteMenu_api({ menuId: row.menuId }, headers);
      if (res.code == 'Success') {
        confirmDia.hide();
        MessagePlugin.success('删除成功');
        getMenuList();
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}

/**
 * 添加父菜单 弹窗
 */
let addShowDialog = () => {
  state.addShow = true;
}

// 添加父菜单 图标
let fileList = ref([]);
let editFileList = ref([]);
const onChangeFiles = (files) => {
  fileList.value = files;
}

/**
 * 添加父菜单
 */
let onAddMenuFunc = async () => {
  let { name, component, path, tags, hidden } = state.addform;
  let params = {
    parentId: "0",
    menuName: name,
    component,
    path,
    tags,
    hiddenMenu: hidden,
  };
  if (fileList.value && fileList.value.length) {
    params['icon'] = fileList.value[0].url;
  } else {
    params['icon'] = '';
  }
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  };
  let res = await createMenu_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('添加成功');
    state.addShow = false;
    state.addform.name = '';
    state.addform.component = '';
    state.addform.path = '';
    state.addform.tags = [];
    state.addform.hidden = false;
    getMenuList();
  }
}

const onCloseAddParent = () => {
  state.addform.name = '';
  state.addform.component = '';
  state.addform.path = '';
  state.addform.tags = [];
  state.addform.hidden = false;
  fileList.value = [];
  editFileList.value = [];
}

let getMenuList = async () => {
  if (!(headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion)) {
    return; // 四要素不全不发请求（分页等直调点），避免 subappId不能为空
  }
  let { page, size } = state;
  let params = {
    parentId: '0',
    page: page - 1,
    size,
  };
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  }
  let res = await getMenuPageList_api(params, headers);
  if (res.code == 'Success') {
    state.list = res.data.contents;
    state.total = Number(res.data.total);
  }
}


/**
 * 修改菜单
 * @param {*} attr 修改的属性
 * @param {*} value 修改的属性值
 * @param {*} id
 */
let editMenu = async (attr, value, menuId) => {
  let params = {
    [attr]: value,
    menuId
  };
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  }
  let res = await modifyMenu_api(params, headers);
  if (res.code == 'Success') {
    getMenuList();
    MessagePlugin.success('修改成功');
  }
}

let onPreviewChildMenu = async (row) => {
  setId(row.menuId);
  setName(row.menuName);
  setAppId(headerParams.value.appId)
  setEndpointId(headerParams.value.endpointId)
  setSubappId(headerParams.value.subappId)
  setSubappVersion(headerParams.value.subappVersion)
  pageType.value = 'child';
}

/**
 * 权限分页
 */
let onPermissionPageSizeChange = () => {
  getPermissionMenu();
}
let onPermissionCurrentChange = () => {
  getPermissionMenu();
}

/**
 * 菜单列表分页
 */
let onPageSizeChange = () => {
  getMenuList();
}
let onCurrentChange = () => {
  getMenuList();
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
    state.list = [];
    state.total = 0;
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
    state.list = [];
    state.total = 0;
  }
}


// 关闭权限列表弹窗
const onClosePermissionDialog = () => {
  state.adminData.page = 1;
  state.adminData.list = [];
}

</script>

<style lang="scss" scoped>
.menu__wrapper {
  width: 100%;
  background-color: var(--td-bg-color-container);
  color: var(--td-text-color-primary);
  box-sizing: border-box;
  padding: 18px 10px;
}

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


:global(.t-input--auto-width) {
  min-width: 106px;
}
</style>
