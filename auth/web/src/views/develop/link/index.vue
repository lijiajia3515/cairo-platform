<!-- 短链 -->
<template>
  <div class="list__wrapper" v-allow="'link.read'">
    <header>
      <t-button @click="onCreate" v-allow="'link.create_link'">创建</t-button>
    </header>
    <div class="empty"></div>
    <List @page-change="configs.onPageChange" :configs="configs"></List>

    <t-dialog :close-on-overlay-click="false" attach="body" @close="onClose" :on-confirm="onSubmit"
      v-model:visible="state.show" width='30%'>
      <template #header>添加</template>
      <t-form ref="formRef" :data="formData" :rules="rules">
        <t-form-item label="原链接" name="linkUrl">
          <t-input v-model="formData.linkUrl"></t-input>
        </t-form-item>
      </t-form>
    </t-dialog>

    <!-- 用户信息 userDetail -->
    <UserInfo @close="onCloseUserDetail" :userId="userDetail.userId" :nickname="userDetail.nickname"
      :accountAvatarUrl="userDetail.accountAvatarUrl" :joinTime="userDetail.joinTime" ref="userInfoRef"></UserInfo>
  </div>
</template>

<script setup lang="jsx">
defineOptions({ name: 'manage-develop-link' })

import { ref, reactive, onMounted } from 'vue';
import useState from '@/hooks/useState';
import {
  MessagePlugin,
  LoadingPlugin,
  DialogPlugin
} from 'tdesign-vue-next';
import {
  getLinkPageList_api,
  modifyLinkStatus_api,
  deleteLink_api,
  createLink_api
} from '@/api';
import List from '@/components/list';
import UserInfo from '@/components/userInfo';
import { ellipsisColumn, timeColumn, opColumn, switchColumn } from '@/utils/tableColumns';
import { hasPermission } from '@/plugins/permission';

let state = reactive({
  show: false
})
let formRef = ref(null)

let [formData, setFormData] = useState({
  linkUrl: ''
})

const rules = {
  linkUrl: [{ required: true, message: '原链接必填' },],
}
onMounted(() => {
  getTableList()
})


const onCreate = () => {
  state.show = true
}

let page = ref(1);
let size = ref(10);
let total = ref(0);
const [list, setList] = useState([]);
const [loading, setLoading] = useState(false);
const [configs, setConfigs] = useState({
  data: list,
  columns: [
    { colKey: 'linkId', title: '编码' },
    ellipsisColumn('shortUrl', '短链接', { width: 220 }),
    ellipsisColumn('linkUrl', '原链接', { width: 300 }),
    { colKey: 'accessCount', title: '访问次数' },
    timeColumn('lastAccessTime', '最后访问时间'),
    switchColumn({
      api: modifyLinkStatus_api,
      idKeys: ['linkId'],
      label: '友链',
      perm: 'link.modify_link_status',
      refresh: () => getTableList(),
    }),
    {
      colKey: 'metadata.createUser.nickname', title: '创建人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.createUser?.accountAvatarUrl ?
                <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Create')} hideOnLoadFailed={true}
                  alt={row?.metadata?.createUser?.nickname?.slice(0, 2)} size="medium"
                  image={row?.metadata?.createUser?.accountAvatarUrl} /> : (
                  row?.metadata?.createUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Create')}
                    size="medium">{row?.metadata?.createUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row, 'Create')} style={{
              height: '100%',
              display: 'flex',
              alignItems: 'center'
            }}>{row?.metadata?.createUser?.nickname || null}</div>
          </t-space>
        )
      }
    },
    timeColumn('metadata.createTime', '创建时间'),
    {
      colKey: 'metadata.updateUser.nickname', title: '更新人', width: 160, cell: (h, { row }) => {
        return (
          <t-space size="small">
            {
              row?.metadata?.updateUser?.accountAvatarUrl ?
                <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Update')} hideOnLoadFailed={true}
                  alt={row?.metadata?.updateUser?.nickname?.slice(0, 2)} size="medium"
                  image={row?.metadata?.updateUser?.accountAvatarUrl} /> : (
                  row?.metadata?.updateUser?.nickname ? <t-avatar class="pick" onClick={() => onWatchUserInfo(row, 'Update')}
                    size="medium">{row?.metadata?.updateUser?.nickname?.slice(0, 2)}</t-avatar> : null
                )
            }
            <div class="pick" onClick={() => onWatchUserInfo(row, 'Update')} style={{
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
      { content: '删除', theme: 'danger', onClick: (row) => onDelete(row), visible: () => hasPermission('link.delete_link') },
    ], { width: 160 }),
  ],
  rowKey: 'appId',
  loading: loading,
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


const getTableList = async () => {
  setLoading(true);
  try {
    let params = {
      page: page.value - 1,
      size: size.value
    }
    let res = await getLinkPageList_api(params)
    if (res.code === 'Success') {
      setList(res.data.contents || [])
      total.value = Number(res.data.total || 0)
    }
  } finally {
    setLoading(false);
  }
}


let userInfoRef = ref(null);
const [userDetail, setUserDetail] = useState({});
const onWatchUserInfo = (row, type) => {
  userInfoRef.value.open();
  if (type === 'Create') {
    setUserDetail(row.metadata.createUser)

  } else if (type === 'Update') {
    setUserDetail(row.metadata.updateUser)
  }
}

const onCloseUserDetail = () => {
  setUserDetail({});
}

const onSubmit = async () => {
  const validate = await formRef.value.validate();
  if (validate == true) {
    let params = {
      linkUrl: formData.value.linkUrl
    }
    let res = await createLink_api(params)
    if (res.code === "Success") {
      MessagePlugin.success('创建成功');
      onClose();
      getTableList();
    }
  }
}
const onClose = () => {
  setFormData({
    linkUrl: ''
  })
  state.show = false
}

const onDelete = async (row) => {
  const confirmDia = DialogPlugin({
    header: '删除',
    body: '是否继续操作?',
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async ({ e }) => {
      LoadingPlugin(true);
      try {
        let params = {
          linkId: row.linkId,
        }
        let res = await deleteLink_api(params);
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

<style lang="scss" scoped>
.list__wrapper {
  header {
    box-sizing: border-box;
  }
}
</style>
