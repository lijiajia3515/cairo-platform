<template>
  <div v-allow="'menu.read'" class="menu__wrapper">
    <t-breadcrumb>
      <template #default>
        <t-breadcrumbItem>{{ name }}</t-breadcrumbItem>
        <template v-if="childIds.length">
          <t-breadcrumbItem v-for="(item, index) in childIds" :key="index">{{ item.name }}</t-breadcrumbItem>
        </template>
        <i @click="goBack" class="iconfont icon-fanhui backIcon"></i>
      </template>
      <template #separator> | </template>
    </t-breadcrumb>
    <div class="empty"></div>
    <header class="filter-header">
      <div class="filter-header__actions">
        <t-button v-allow="'menu.write'" @click="addChildDialog">添加</t-button>
      </div>
    </header>
    <div class="empty"></div>
    <t-table drag-sort="row-handler" @drag-sort="onDragSortChild" size="small" row-key="menuId"
      :data="state.childMenu.list" :columns="childColumns" table-layout="fixed">
    </t-table>
    <div class="empty"></div>
    <div class="list-card-pagination">
      <t-pagination v-model="state.childMenu.page" v-model:pageSize="state.childMenu.size"
        :total="state.childMenu.total" show-jumper show-page-size :pageSizeOptions="[10, 20, 50, 100]"
        @page-size-change="onChildPageSizeChange" @current-change="onChildCurrentChange" />
    </div>

    <!-- 添加子菜单 -->
    <t-dialog @close="onCloseAddChild" :close-on-overlay-click="false" attach="body" :on-confirm="onAddChildMenuFunc"
      v-model:visible="state.addChildShow">
      <template #header>
        添加子菜单
      </template>
      <t-form>
        <t-row>
          <t-col :span="12">
            <t-form-item label="名称">
              <t-input :maxlength="255" v-model="state.addChildform.name" placeholder="请输入名称"></t-input>
            </t-form-item>
            <t-form-item label="组件路径">
              <t-input :maxlength="255" v-model="state.addChildform.component" placeholder="例： system/user"></t-input>
            </t-form-item>
            <t-form-item label="地址">
              <t-input :maxlength="255" v-model="state.addChildform.path" placeholder="例： https://baidu.com"></t-input>
            </t-form-item>
            <t-form-item label="tags">
              <t-select v-model="state.addChildform.tags" multiple :max="2">
                <t-option label="新功能" value="new" />
                <t-option label="热门" value="hot" />
                <t-option label="旧版本" value="old" />
                <t-option label="废弃功能" value="deprecated" />
              </t-select>
            </t-form-item>
            <t-form-item label="icon">
              <UploadImage :appId="headerParams.appId" type="public" picType="menu" @change="onChangeChildFiles"
                :limit="1" :fileList="editChildFileList"></UploadImage>
              <!-- <t-input :maxlength="255" v-model="state.addChildform.icon" placeholder="阿里巴巴矢量图标库"></t-input> -->
            </t-form-item>
            <t-form-item label="菜单是否隐藏">
              <t-radio-group v-model="state.addChildform.hidden" :default-value="false" name="显示"
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
      <t-table size="small" row-key="id" :data="state.adminData.list" :columns="adminColumns" table-layout="fixed">
      </t-table>
      <div class="empty"></div>
      <!-- 分页 -->
      <t-pagination v-model="state.adminData.page" v-model:pageSize="state.adminData.size"
        :total="state.adminData.total" show-jumper @page-size-change="onPermissionPageSizeChange"
        @current-change="onPermissionCurrentChange" />
    </t-dialog>

    <!-- 编辑子菜单 -->
    <EditChild v-if="visibleEditChild" :visible="visibleEditChild" :appId="headerParams.appId"
      :endpointId="headerParams.endpointId" :subappId="headerParams.subappId"
      :subappVersion="headerParams.subappVersion" :menuId="formEditChild.menuId" :icon="formEditChild.icon"
      :tags="formEditChild.tags" :menuName="formEditChild.menuName" :component="formEditChild.component"
      :path="formEditChild.path" :hiddenMenu="formEditChild.hiddenMenu"
      @finish="onFinishEditChild" @close="onCloseEditChildMenu"></EditChild>
  </div>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>
<script setup lang="jsx">
import { ref, shallowRef, watch, reactive, onMounted } from 'vue';
import {
  cloneDeep,
  debounce,
} from 'lodash';

import { Input, Select, MessagePlugin, DialogPlugin } from 'tdesign-vue-next';
import { MoveIcon } from 'tdesign-icons-vue-next';

import useState from '@/hooks/useState';
import { opColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';
import EditChild from './editChild.vue';
import UserInfo from '@/components/userInfo';
import UploadImage from '@/components/uploadImage';

import {
  getMenuPageList_api, modifyMenu_api, createMenu_api,
  deleteMenu_api,
  moveMenu_api, getMenuTree_api,
  getPermissionPageList_api,
} from '@/api';

const props = defineProps({
  id: {
    type: String
  },
  name: {
    type: String
  },
  appId: {
    type: String
  },
  endpointId: {
    type: String
  },
  subappId: {
    type: String
  },
  subappVersion: {
    type: String
  }
});
const emit = defineEmits(['home'])

const [childIds, setChildIds] = useState([]);
watch(childIds, () => {
  state.childMenu.page = 1;
  state.childMenu.parentId = childIds.value.length > 0 ? childIds.value[childIds.value.length - 1].id : props.id;
  state.addChildform.parentId = childIds.value.length > 0 ? childIds.value[childIds.value.length - 1].id : props.id;

  getChildMenuList();
}, {
  deep: true
});

// 返回上一级
const goBack = () => {
  if (childIds.value.length) {
    let ids = cloneDeep(childIds.value);
    ids.splice(-1, 1); // 删除最后一项
    setChildIds(ids);
  } else { // 返回父级 首页
    emit('home')
  }
}

let state = reactive({
  childMenu: {
    parentId: props.id,
    show: false,
    page: 1,
    size: 10,
    total: 0,
    list: []
  },
  addChildShow: false,
  addChildform: { // 添加子菜单
    parentId: props.id,
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
    list: [],
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


let childColumns = shallowRef([
  {
    colKey: 'drag',
    title: '排序',
    cell: (h) => (
      <span>
        <MoveIcon />
      </span>
    ),
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
        // clearable: true,
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let menuName = context.newRowData.menuName;
        let menuId = context.newRowData.menuId;
        editMenu('menuName', menuName, menuId)
      },
    }
  },
  {
    colKey: 'component', title: '组件路径', ellipsis: true, minWidth: 180, edit: {
      props: {
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let component = context.newRowData.component;
        let menuId = context.newRowData.menuId;
        editMenu('component', component, menuId)
      },
    }
  },
  {
    colKey: 'path', title: '外部地址', ellipsis: true, minWidth: 180, edit: {
      props: {
        autofocus: true,
      },
      abortEditOnEvent: ['onEnter'],
      component: Input, onEdited: (context) => {
        let path = context.newRowData.path;
        let menuId = context.newRowData.menuId;
        editMenu('path', path, menuId)
      },
    }
  },
  {
    colKey: 'tags', title: 'tags', cell: (h, { row }) => {
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
    minWidth: 140,
  },

  {
    colKey: 'hiddenMenu',
    title: '是否隐藏',
    minWidth: 100,
    cell: (h, { col, row }) => <div>{row[col.colKey] == false ? '显示' : '隐藏'}</div>,
    edit: {
      props: ({ col, row, rowIndex, colIndex, editedRow }) => {
        return {
          options: [
            { label: '隐藏', value: true },
            { label: '显示', value: false },
          ]
        }
      },
      component: Select,
      onEdited: (context) => {
        onRadioChange(context.newRowData.hiddenMenu, context.newRowData.menuId)
      }
    }
  },
  {
    colKey: 'metadata.updateUser.nickname', title: '更新人', minWidth: 140, cell: (h, { row }) => {
      return (
        <t-space size="small">
          {
            row?.metadata?.updateUser?.accountAvatarUrl ?
              <t-avatar class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true}
                alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium"
                image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row)}
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
  { colKey: 'metadata.updateTime', title: '更新时间', ellipsis: true, minWidth: 170 },
  opColumn([
    {
      content: '子菜单',
      onClick: (row) => onPreviewChildMenu(row),
      visible: () => hasPermission('menu.read') && childIds.value.length < 1
    },
    { content: '编辑', onClick: (row) => onEditChildMenu(row), visible: () => hasPermission('menu.write') },
    { content: '功能权限', onClick: (row) => onPreviewPermission(row), visible: () => hasPermission('menu.read') },
    { content: '删除', theme: 'danger', onClick: (row) => onDeleteChildMenu(row), visible: () => hasPermission('menu.write') },
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


// 头部参数
const [headerParams] = useState({
  appId: props.appId,
  endpointId: props.endpointId,
  subappId: props.subappId,
  subappVersion: props.subappVersion,
})

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
  getChildMenuList()
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
 **************************************** 编辑子菜单
 */
const visibleEditChild = ref(false);
const [formEditChild, setFormEditChild] = useState({
  menuId: null,
  icon: null,
  menuName: null,
  component: null,
  tags: [],
  path: null,
  hiddenMenu: null,
})
/**
 * @param {Object} row
 * @param {String} row.menuId
 */
const onEditChildMenu = (row) => {
  setFormEditChild({
    menuId: row?.menuId,
    icon: row?.icon,
    menuName: row?.menuName,
    component: row?.component,
    tags: row?.tags,
    path: row?.path,
    hiddenMenu: row?.hiddenMenu
  });
  visibleEditChild.value = true;
}
const onCloseEditChildMenu = () => {
  visibleEditChild.value = false;
}
const onFinishEditChild = () => {
  getChildMenuList();
}

let getMenuTree = async () => {
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
 * 监听子菜单移动
 */
let onDragSortChild = (params) => {
  let { current, newData } = params;
  let parentId = state.childMenu.parentId;
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
    getChildMenuList();
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
    getChildMenuList();
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
 * 删除子菜单
 */
let onDeleteChildMenu = async (row) => {
  const confirmDia = DialogPlugin({
    header: '菜单删除',
    body: '你确定删除吗?',
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
        getChildMenuList()
        // let params = {
        //   parentId: state.childMenu.parentId,
        //   Page: 0,
        //   Size: state.childMenu.size,
        // }
        // let res = await getMenuPageList_api(params, headers);
        // if (res.code == 'Success') {
        //   state.childMenu.list = res.data.contents;
        //   state.childMenu.total = Number(res.data.total);
        // }
      }
    },
    onClose: ({ e, trigger }) => {
      confirmDia.hide();
    },
  });
}


/**
 * 添加子菜单弹窗
 */
let addChildDialog = () => {
  state.addChildShow = true;
}


// 添加父菜单 图标
let childFileList = ref([]);
let editChildFileList = ref([]);
const onChangeChildFiles = (files) => {
  childFileList.value = files;
}


/**
 * 添加子菜单
 */
let onAddChildMenuFunc = async () => {
  let { parentId, name, component, path, tags, hidden } = state.addChildform;
  let params = {
    parentId,
    menuName: name,
    component,
    path,
    tags,
    hiddenMenu: hidden,
  };
  if (childFileList.value && childFileList.value.length) {
    params['icon'] = childFileList.value[0].url;
  } else {
    params['icon'] = '';
  }
  let headers = {
    'app-id': headerParams.value.appId,
    'endpoint-id': headerParams.value.endpointId,
    'subapp-id': headerParams.value.subappId,
    'subapp-version': headerParams.value.subappVersion,
  }
  let res = await createMenu_api(params, headers);
  if (res.code == 'Success') {
    MessagePlugin.success('添加成功');
    onCloseAddChild();
    getChildMenuList();
  }
}

const onCloseAddChild = () => {
  state.addChildShow = false;
  state.addChildform.id = null;
  state.addChildform.name = '';
  state.addChildform.component = '';
  state.addChildform.path = '';
  state.addChildform.tags = '';
  state.addChildform.hidden = false;
  childFileList.value = [];
  editChildFileList.value = [];
}

let getMenuList = async () => {
  if (!(headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion)) {
    return; // 四要素不全不发请求，避免 subappId不能为空
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
    getChildMenuList();
    MessagePlugin.success('修改成功');
  }
}


let onPreviewChildMenu = async (row) => {
  let ids = childIds.value;
  ids.push({ Id: row.menuId, Name: row.menuName });
  setChildIds(ids);
  console.log(childIds.value, 'childIds.value====');
}
/**
 * 获取子菜单列表
 */
let getChildMenuList = async () => {
  if (!(headerParams.value.appId && headerParams.value.endpointId && headerParams.value.subappId && headerParams.value.subappVersion)) {
    return; // 四要素不全不发请求，避免 subappId不能为空
  }
  let { page, size } = state.childMenu;
  let params = {
    parentId: state.childMenu.parentId,
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
    state.childMenu.list = res.data.contents;
    state.childMenu.total = Number(res.data.total);
  }
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
 * 子菜单列表分页
 */
let onChildPageSizeChange = () => {
  getChildMenuList();
}
let onChildCurrentChange = () => {
  getChildMenuList();
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
