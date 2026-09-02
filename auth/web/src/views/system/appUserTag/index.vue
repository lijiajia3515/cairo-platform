<script setup lang="jsx">
import { ref, onMounted } from 'vue';
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

import {
  getAppUserTagPage_api,
  createAppUserTag_api,
  modifyAppUserTagInfo_api,
  modifyAppUserTagStatus_api,
  deleteAppUserTag_api,
} from '@/api';

onMounted(() => {
  getUserTagPage();
});

let enabled = ref(null);
let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(true);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'tagId', title: 'ID', width: 140, ellipsis: true },
    { colKey: 'tagName', title: '名称' },
    { colKey: 'userCount', title: '数量' },
    switchColumn({
      api: modifyAppUserTagStatus_api,
      idKeys: ['tagId'],
      label: '用户标签',
      perm: 'user_tag.modify_user_tag_status',
      refresh: () => getUserTagPage(),
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
      {content: '编辑', onClick: (row) => onEdit(row), visible: () => hasPermission('user_tag.modify_user_tag_info')},
      {content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('user_tag.delete_user_tag')},
    ], {width: 200})
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
    getUserTagPage();
  }
});
// 用户标签分页
const getUserTagPage = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value,
      enabled: enabled.value,
    }
    const res = await getAppUserTagPage_api(params);
    if (res.code == 'Success') {
      setList(res?.data?.contents || []);
      total.value = Number(res.data.total) || 0
    }
  } finally {
    setLoading(false);
  }
}

const onSearch = () => {
  page.value = 1;
  getUserTagPage();
}

const onReset = () => {
  page.value = 1;
  enabled.value = null;
  getUserTagPage();
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
  tagId: [
    { required: true, message: '标签id必填', type: 'error', trigger: 'change' },
  ],
  tagName: [
    { required: true, message: '标签名称必填', type: 'error', trigger: 'change' },
  ],
}
let formRef = ref(null);
const [type, setType] = useState('add');
const [visible, setVisible] = useState(false);
const [form, setForm] = useState({
  tagId: null,
  tagName: null,
})
const onCreate = () => {
  setType('add');
  setVisible(true);
  formRef.value.clearValidate();
}
const onConfirm = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    LoadingPlugin(true);
    try {
      let params = {
        tagId: form.value.tagId,
        tagName: form.value.tagName
      };
      if (type.value == 'add') {
        let res = await createAppUserTag_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('创建成功');
          onClose();
          getUserTagPage();
        }
      } else {
        let res = await modifyAppUserTagInfo_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('编辑信息成功');
          onClose();
          getUserTagPage();
        }
      }
    } finally {
      LoadingPlugin(false);
    }
  }
}

// 编辑
const onEdit = (row) => {
  setType('edit');
  setForm({
    ...form.value,
    tagId: row.tagId,
    tagName: row.tagName,
  });
  setVisible(true);
}
const onClose = () => {
  setType('add');
  setForm({
    tagId: null,
    tagName: null,
  })
  setVisible(false);
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
          tagIds: [row.tagId]
        }
        let res = await deleteAppUserTag_api(params);
        if (res.code == 'Success') {
          MessagePlugin.success('删除成功');
          confirmDia.hide();
          if (list.value && list.value.length == 1 && page.value > 1) {
            page.value = page.value - 1;
          }
          getUserTagPage();
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
  <div v-allow="'user_tag.read'" class="user_tag__wrapper">
    <FilterBar @search="onSearch" @reset="onReset">
      <FilterItem label="状态">
        <t-select clearable v-model="enabled" placeholder="请选择状态">
          <t-option label="启用" :value="true"></t-option>
          <t-option label="禁用" :value="false"></t-option>
        </t-select>
      </FilterItem>
      <template #actions>
        <t-button v-allow="'user_tag.create_user_tag'" @click="onCreate">创建</t-button>
      </template>
    </FilterBar>
    <List @page-change="configs.onPageChange" :configs="configs"></List>
  </div>


  <!-- 创建 编辑 -->
  <Dialog @confirm="onConfirm" @close="onClose" :visible="visible">
    <template #title>{{ type == 'add' ? '创建' : '编辑' }}</template>
    <t-form ref="formRef" :data="form" :rules="rules">
      <t-row>
        <t-col :span="12">
          <t-form-item name="tagId" label="标签id">
            <t-input :disabled="type == 'edit'" v-model="form.tagId"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
        <t-col :span="12">
          <t-form-item name="tagName" label="标签名称">
            <t-input v-model="form.tagName"></t-input>
          </t-form-item>
        </t-col>
        <div class="empty"></div>
      </t-row>
    </t-form>
  </Dialog>

  <!-- 用户信息 userDetail -->
  <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
    :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
</template>

<style lang="scss" scoped>
.user_tag__wrapper {}
</style>
