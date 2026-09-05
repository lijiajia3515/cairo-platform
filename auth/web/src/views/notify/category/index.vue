<template>
  <div v-allow="'notify_category.read'">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="关键字">
        <t-input placeholder="关键字" v-model="search.keyword"></t-input>
      </FilterItem>
      <FilterItem label="应用">
        <t-select :scroll="{ type: 'virtual' }" filterable clearable v-model="appId" placeholder="应用">
          <t-option v-for="(item, index) in appList" :key="index" :value="item.appId" :label="item.appName">
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
      <FilterItem>
        <t-select v-model="search.categoryIds" multiple>
          <t-option label="启用" :value="true" />
        </t-select>
      </FilterItem>
      <FilterItem label="状态">
        <t-select placeholder="状态" v-model="search.enabled">
          <t-option label="启用" :value="true" />
          <t-option label="禁用" :value="false" />
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button @click="onCreate" v-allow="'notify_category.create'">创建</t-button>
      </template>
    </FilterBar>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"></List>
    <Dialog width="30%" :visible="dialogVisible" @confirm="onSubmit" @close="onClose">
      <template #title>{{ title }}</template>
      <t-form labelAlign="left" label-width="50px" :data="addForm" :rules="rules" ref="addFormRef">
        <t-form-item label="名称" name="categoryName">
          <t-input v-model="addForm.categoryName" placeholder="请输入" />
        </t-form-item>
        <t-form-item label="图标" name="categoryIcon">
          <UploadImage :appId="appId" type="public" picType="notify-category-icon" @change="onChangeFiles"
            :limit="1" :fileList="editFileList">
          </UploadImage>
        </t-form-item>
      </t-form>
    </Dialog>
  </div>
</template>

<script setup lang="jsx">
defineOptions({ name: 'manage-notify-category' })

import { ref, onMounted, watch } from 'vue';
import UploadImage from '@/components/uploadImage';
import FilterBar from '@/components/filterBar';
import FilterItem from '@/components/filterBar/item.vue';
import {
  getNotifyCategoryPageList_api,
  createNotifyCategory_api,
  modifyNotifyCategoryInfo_api,
  deleteNotifyCategory_api,
  modifyNotifyCategoryStatus_api,
  getAppList_api
} from '@/api';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin,
} from 'tdesign-vue-next';
import useState from '@/hooks/useState';
import List from '@/components/list';
import Dialog from '@/components/dialog';
import { opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

const search = ref({
  keyword: '',
  categoryIds: [],
  enabled: null,
})
const [appId, setAppId] = useState('')
const [title, setTitle] = useState('')
const [dialogVisible, setDialogVisible] = useState(false)
const [addForm, setAddForm] = useState({
  categoryId: '',
  categoryName: '',
  categoryIcon: ''
})
const [editFileList, setEditFileList] = useState([]);
const addFormRef = ref(null)

const rules = {
  categoryName: [{ required: true, message: '请输入', type: 'error', trigger: 'blur' }],
}

let page = ref(1);
let size = ref(10);
let total = ref(0);
const [loading, setLoading] = useState(false);
const [list, setList] = useState([]);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'categoryId', title: 'ID' },
    { colKey: 'categoryName', title: '名称' },
    {
      colKey: 'app', title: '应用', width: 120, cell: (h, { row }) => {
        return row?.app?.appName
      }
    },
    {
      colKey: 'categoryIcon', title: '图标', width: 80, cell: (h, { row }) => {
        return (
          <t-avatar image={row?.categoryIcon} />
        )
      }
    },
    switchColumn({
      api: (params) => modifyNotifyCategoryStatus_api(params, { 'app-id': appId.value }),
      idKeys: ['categoryId'],
      label: '通知分类',
      perm: 'notify_category.modify_status',
      refresh: () => getTableList(),
    }),
    opColumn([
      { content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('notify_category.modify_info') },
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('notify_category.delete') },
    ])
  ],
  loading: loading,
  rowKey: 'appId',
  pagination: {
    current: page,
    pageSize: size,
    total: total,
  },
  onPageChange: (pageInfo) => {
    page.value = pageInfo.current;
    size.value = pageInfo.pageSize;
    getTableList();
  }
});

onMounted(() => {
  getAppList()
})
watch(() => appId.value, () => {
  getTableList()
})

// 应用列表
const [appList, setAppList] = useState([]);
const getAppList = async () => {
  let params = {};
  let res = await getAppList_api(params);
  if (res.code == 'Success') {
    setAppList(res?.data || []);
    if (appList.value.length > 0) {
      setAppId(appList.value[0].appId);
    }
  }
}
const getTableList = async () => {
  setLoading(true);
  try {
    let params = {
      ...search.value,
      page: page.value - 1,
      size: size.value
    }
    let headers = {
      'app-id': appId.value
    };
    let res = await getNotifyCategoryPageList_api(params, headers)
    if (res.code == 'Success') {
      setList(res.data.contents || [])
      total.value = Number(res.data.total) || 0
    }
  } finally {
    setLoading(false);
  }
}
const onSearch = () => {
  page.value = 1
  getTableList()
}
const onReset = () => {
  search.value.keyword = ''
  search.value.categoryIds = []
  search.value.enabled = null
  page.value = 1
  getTableList()
}

const onCreate = () => {
  setTitle('创建')
  setDialogVisible(true)
  addFormRef.value.clearValidate();
}

const onEdit = (row) => {
  setTitle('编辑')
  setAddForm({
    categoryId: row.categoryId,
    categoryName: row.categoryName,
  })
  if (row.categoryIcon) {
    setEditFileList([{
      name: row.categoryIcon.split('/')[row.categoryIcon.split('/').length - 1],
      url: row.categoryIcon
    }])
  }
  setDialogVisible(true);
}
// 上传图标
const [fileList, setFileList] = useState([]);
const onChangeFiles = (files) => {
  setFileList(files)
}

const onClose = () => {
  setAddForm({
    categoryId: '',
    categoryName: '',
    categoryIcon: ''
  })
  setEditFileList([]);
  setDialogVisible(false)
}

const onSubmit = async () => {
  const validate = await addFormRef.value.validate();
  console.log(validate, 'validate====');
  if (validate == true) {
    let headers = {
      'app-id': appId.value
    };
    let params = {
      ...addForm.value
    }
    if (fileList.value && fileList.value.length) {
      params['categoryIcon'] = fileList.value[0].url;
    }
    const request = addForm.value?.categoryId ? modifyNotifyCategoryInfo_api : createNotifyCategory_api
    request(params, headers).then(res => {
      MessagePlugin.success(addForm.value?.categoryId ? '修改成功' : '创建成功');
      getTableList()
      onClose()
    })
  }
}
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
          categoryIds: [row.categoryId],
        }
        let headers = {
          'app-id': appId.value
        };
        let res = await deleteNotifyCategory_api(params, headers);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          getTableList();
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

<style lang="scss" scoped></style>
