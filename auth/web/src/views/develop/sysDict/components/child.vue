<script setup lang="jsx">
import {
  onMounted,
  ref,
  watch,
  reactive
} from 'vue';
import {
  MessagePlugin,
  DialogPlugin,
  LoadingPlugin,
} from 'tdesign-vue-next';
import { cloneDeep } from 'lodash';

import useState from '@/hooks/useState';

import List from '@/components/list';
import Dialog from '@/components/dialog';
import AccountInfo from '@/components/accountInfo';
import UploadImage from '@/components/uploadImage';
import { timeColumn, avatarCopyColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

import {
  getSysDictItemPage_api,
  putSysDictItem_api,
  modifySysDictItem_api,
  modifySysDictItemStatus_api,
  deleteSysDictItem_api,
  getSysDictDetailInfo_api,
  moveSysDictItem_api
} from '@/api';
onMounted(() => {
  getSysDictItemPage();
});

const props = defineProps({
  id: {
    type: String
  },
  name: {
    type: String
  },
  appId: {
    type: String
  }
});

const emit = defineEmits(['home'])

const [currentId] = useState(props.id); // 父级Id
const [childIds, setChildIds] = useState([]); // 子项 层级 {id name}

let state = reactive({
  move_show: false,
  menuTrees: []
});

watch(childIds, () => {
  page.value = 1;
  getSysDictItemPage();
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




let appId = ref(props.appId);
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'itemId', title: '字典项id' },
    avatarCopyColumn({ colKey: 'itemName', title: '字典项名称', iconKey: 'icon' }),
    { colKey: 'remark', title: '备注' },
    {
      colKey: 'editable', title: '是否允许编辑', width: 120, cell: (h, { row }) => {
        let flag = row['editable'];
        return flag == true ? '允许' : (flag == false ? '禁止' : '')
      }
    },
    switchColumn({
      api: (params) => modifySysDictItemStatus_api(params, { 'app-id': appId.value }),
      idKeys: ['itemId'],
      extra: () => ({ dictId: currentId.value }),
      label: '字典项',
      perm: 'sys_dict.modify_status',
      refresh: () => getSysDictItemPage(),
    }),
    { colKey: 'depth', title: '层级' },
    {
      colKey: 'metadata.updateAccount.nickname', title: '更新人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateAccount?.avatarUrl ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} hideOnLoadFailed={true} alt={row?.metadata?.updateAccount?.nickname?.slice(0, 2)} size="medium" image={row?.metadata?.updateAccount?.avatarUrl} /> : (
                row?.metadata?.updateAccount?.nickname ? <t-avatar imageProps={{ lazy: true }} class="pick" onClick={() => onWatchUserInfo(row)} size="medium" >{row?.metadata?.updateAccount?.nickname?.slice(0, 2)}</t-avatar> : null
              )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row)} style={{ height: '100%', display: 'flex', alignItems: 'center' }}>{row?.metadata?.updateAccount?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.updateTime', '更新时间'),
    opColumn([
      { content: '子项', onClick: (row) => onWatchItem(row), visible: () => hasPermission('sys_dict.find') },
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('sys_dict.modify') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('sys_dict.delete') },
    ], { width: 200 })
  ],
  loading: loading,
  rowKey: 'dictId',
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getSysDictItemPage();
  }
});

/**
 **************************************************** 用户详情
 */
let accountInfoRef = ref(null);
const [accountDetail, setAccountDetail] = useState({});
const onWatchUserInfo = (row) => {
  accountInfoRef.value.open();
  setAccountDetail(row.metadata.updateAccount)
}
const onCloseAccountInfo = () => {
  setAccountDetail({});
}

/**
 * 子项分页
 */
const getSysDictItemPage = async () => {
  setLoading(true);
  try {
    let params = {
      dictId: currentId.value,
      parentItemId: childIds.value.length ? childIds.value[childIds.value.length - 1].id : '0',
      page: page.value - 1,
      size: size.value,
    };
    let header = {
      'app-id': appId.value
    }
    let res = await getSysDictItemPage_api(params, header);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res?.data?.total || 0);
    }
  } finally {
    setLoading(false);
  }
}



/**
 * 创建子项
 */
const rules = {
  itemId: [
    { required: true, message: '字典项ID必填', type: 'error', trigger: 'change' },
  ],
  itemName: [
    { required: true, message: '字典项名称必填', type: 'error', trigger: 'change' },
  ]
}
const formRef = ref(null);
const [visible, setVisible] = useState(false);
const [type, setType] = useState('add');
const [editFileList, setEditFileList] = useState([]); // 编辑 显示 icon
const [form, setForm] = useState({
  itemId: null,
  itemName: null,
  remark: null,
  beforeItemId: null,
  editable: true
})
const onCreate = () => {
  setVisible(true);
  setType('add');
  formRef.value.clearValidate();
}


const getMenuTrees = async () => {
  let params = { dictId: currentId.value }
  let header = {
    'app-id': appId.value
  }
  let res = await getSysDictDetailInfo_api(params, header)
  if (res.code === 'Success') {
    state.menuTrees = res.data.items
  }
}

const onMove = () => {
  state.move_show = true
  getMenuTrees()
}
const onCloseMenuMove = () => {
  state.move_show = false
  state.menuTrees = []
}

const handleDragEnd = async ({ node, e }) => {
  // console.log('移动', node, e)
  try {
    let newData = [];
    let parentId = null;
    let beforeId = '';
    let moveId = null;
    if (node['__tdesign_tree-node__']) {
      moveId = node['__tdesign_tree-node__'].data.itemId;
      if (node['__tdesign_tree-node__'].parent) {
        newData = node['__tdesign_tree-node__'].parent.children;
        parentId = node['__tdesign_tree-node__'].parent.data.itemId;
      } else { // 最外层，没有父级
        if (node['__tdesign_tree-node__'].tree) {
          newData = node['__tdesign_tree-node__'].tree.children;
        }
        parentId = '0';
      }
    } else {
      moveId = node.data.itemId;
      if (node.parent) {
        newData = node.parent.children;
        parentId = node.parent.data.itemId;
      } else { // 最外层，没有父级
        if (node.tree) {
          newData = node.tree.children;
        }
        parentId = '0';
      }
    }


    const index = newData.findIndex(item => item.data.itemId == moveId);
    if (index == (newData.length - 1)) { // 移动到最后一个
      // setMenuMove('', moveId, parentId);
    } else {
      beforeId = newData[index + 1].data.itemId; // 菜单移动完后，后面一个Id
      // setMenuMove(beforeId, moveId, parentId);
    }

    let params = {
      dictId: currentId.value,
      moveItemId: moveId,
      beforeItemId: beforeId,
      parentItemId: parentId
    };
    let header = {
      'app-id': appId.value
    }
    let res = await moveSysDictItem_api(params, header);
    if (res.code == 'Success') {
      MessagePlugin.success('移动成功');
      getSysDictItemPage()
    }
  } catch (err) {
    console.log(err);
  }
};



const onClose = () => {
  setVisible(false);
  setType('add');
  setForm({
    itemId: null,
    itemName: null,
    remark: null,
    beforeItemId: null,
    editable: true
  });
  setEditFileList([]);
  setFileList([]);
}

const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let { itemId, itemName, remark, beforeItemId, editable} = form.value;
      let header = {
        'app-id': appId.value
      }
      if (type.value == 'add') {
        let params = {
          dictId: currentId.value,
          parentItemId: childIds.value.length ? childIds.value[childIds.value.length - 1].id : '0',
          itemId, itemName, remark, beforeItemId
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        }
        let res = await putSysDictItem_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getSysDictItemPage();
        }
      } else {
        let params = {
          dictId: currentId.value, itemId, itemName, remark, editable
        };
        if (fileList.value && fileList.value.length) {
          params['icon'] = fileList.value[0].url;
        } else {
          params['icon'] = '';
        }
        let res = await modifySysDictItem_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑成功');
          onClose();
          getSysDictItemPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}


/**
 * 编辑
 * @param {Object} row
 * @param {String} row.itemId
 * @param {String} row.itemName
 * @param {String} row.remark
 * @param {String} row.icon
 * @param {Boolean} row.editable
 */
const onEdit = (row) => {
  setVisible(true);
  setType('edit');
  formRef.value.clearValidate();
  if (row.icon) {
    setEditFileList([{
      name: row.icon.split('/')[row.icon.split('/').length - 1],
      url: row.icon
    }])
  }
  setForm({
    itemId: row.itemId,
    itemName: row.itemName,
    remark: row.remark,
    editable: row.editable
  });
}

/**
 * 查看子项
 * @param {Object} row
 * @param {String} row.itemId Id
 */
const onWatchItem = (row) => {
  let ids = childIds.value;
  ids.push({ id: row.itemId, name: row.itemName });
  setChildIds(ids);
}

/**
 * 删除
 * @param {Object} row
 * @param {String} row.itemId
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
        let header = {
          'app-id': appId.value
        };
        let params = {
          dictId: currentId.value,
          itemId: row.itemId,
        };
        let res = await deleteSysDictItem_api(params, header);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getSysDictItemPage();
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
  <div v-allow="'sys_dict.find'" class="Item_page">
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
    <header>
      <t-row :gutter="10">
        <t-col :span="1">
          <t-button v-allow="'sys_dict.create'" @click="onCreate">创建</t-button>
        </t-col>
        <t-col :span="10"></t-col>
        <t-col :span="1">
          <t-button @click="onMove" v-if="childIds.length == 0">菜单移动</t-button>
        </t-col>
      </t-row>
    </header>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs" />
  </div>

  <!-- 移动按钮 -->
  <t-dialog :cancelBtn="null" :confirmBtn="null" :close-on-overlay-click="false" @close="onCloseMenuMove"
    :visible="state.move_show">
    <template #header>菜单移动</template>
    <!-- expand-all -->
    <t-tree :keys="{ value: 'itemId', label: 'itemName', children: 'SubItems' }" :data="state.menuTrees" activable hover
      transition draggable @drag-end="handleDragEnd" />
  </t-dialog>


  <!-- 添加 编辑 -->
  <Dialog @confirm="onSubmit" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '添加子项' : '编辑子项' }}</template>
    <t-form :rules="rules" :data="form" ref="formRef">
      <t-row>
        <t-col :span="11">
          <t-form-item name="itemId" label="字典项ID">
            <t-input :disabled="type == 'edit'" v-model="form.itemId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item name="itemName" label="字典项名称">
            <t-input v-model="form.itemName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="备注">
            <t-input v-model="form.remark"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="图标">
            <UploadImage :disabled="!form.itemId" key="child" :appId="appId" :dictId="id"
              :dictItemId="form.itemId" type="public" picType="sys-dict-item" @change="onChangeFiles"
              :limit="1" :fileList="editFileList"></UploadImage>
            <!-- <t-select v-model="form.icon" :scroll="{ type: 'virtual' }" placeholder="请选择图标">
              <t-option :label="item" :value="item" v-for="(item, index) in icons" :key="index">
                <div class="iconBox">
                  <i :class="['iconfont', item]"></i>
                </div>
              </t-option>
              <template #valueDisplay="{ value, onClose }">
                <i :class="['iconfont', value]"></i>
              </template>
            </t-select> -->
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="11">
          <t-form-item label="是否允许编辑">
            <t-select v-model="form.editable">
              <t-option label="允许" :value="true"></t-option>
              <t-option label="禁止" :value="false"></t-option>
            </t-select>
          </t-form-item>
        </t-col>
      </t-row>
    </t-form>
  </Dialog>
  <!-- 用户信息 userDetail -->
  <AccountInfo :data="accountDetail" ref="accountInfoRef" @close="onCloseAccountInfo"></AccountInfo>
</template>

<style lang="scss" scoped>
.Item_page {
  width: 100%;

  .backIcon {
    font-size: 18px;
    opacity: 0.8;
    cursor: pointer;

    &:hover {
      opacity: 1;
      transform: scale(1.2);
    }
  }
}
</style>
